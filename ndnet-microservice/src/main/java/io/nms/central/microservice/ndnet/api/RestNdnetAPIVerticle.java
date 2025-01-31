package io.nms.central.microservice.ndnet.api;

import io.nms.central.microservice.common.RestAPIVerticle;
import io.nms.central.microservice.ndnet.NdnetService;
import io.nms.central.microservice.ndnet.model.ConfigObj;
import io.nms.central.microservice.topology.TopologyService;
import io.nms.central.microservice.topology.model.Prefix;
import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.nms.central.microservice.common.functional.Functional;


/**
 * This verticle exposes a HTTP endpoint to ndnet with REST APIs.
 *
 * @author Amar Abane
 */
public class RestNdnetAPIVerticle extends RestAPIVerticle {

	private static final Logger logger = LoggerFactory.getLogger(RestNdnetAPIVerticle.class);

	public static final String SERVICE_NAME = "ndnet-rest-api";
	
	private static final String API_VERSION = "/v";

	private static final String API_PA = "/pa";
	private static final String API_ONE_PA = "/pa/:name";

	private static final String API_CANDIDATE_CONFIG = "/intended-config";
	private static final String API_ALL_CANDIDATE_CONFIG = "/intended-config/all";
	private static final String API_ONE_CANDIDATE_CONFIG = "/intended-config/:nodeId";

	private static final String API_RUNNING_CONFIG = "/running-config";
	private static final String API_ALL_RUNNING_CONFIG = "/running-config/all";
	private static final String API_ONE_RUNNING_CONFIG = "/running-config/:nodeId";

	private final NdnetService service;

	public RestNdnetAPIVerticle(NdnetService service) {
		this.service = service;
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		super.start();
		final Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.get(API_VERSION).handler(this::apiVersion);
		
		// Prefix Annoucement API
		router.post(API_PA).handler(this::checkAgentRole).handler(this::apiAddPrefixAnnouncement);
		router.delete(API_ONE_PA).handler(this::checkAgentRole).handler(this::apiDeletePrefixAnnouncement);
		
		// Confguration API
		router.get(API_ALL_CANDIDATE_CONFIG).handler(this::checkAdminRole).handler(this::apiGetAllCandidateConfigs);
		router.get(API_ONE_CANDIDATE_CONFIG).handler(this::checkAdminRole).handler(this::apiGetUserCandidateConfig);
		router.get(API_CANDIDATE_CONFIG).handler(this::checkAgentRole).handler(this::apiGetAgentCandidateConfig);		
		// router.delete(API_ONE_CANDIDATE_CONFIG).handler(this::checkAdminRole).handler(this::apiDeleteCandidateConfig);
		
		router.get(API_ALL_RUNNING_CONFIG).handler(this::checkAdminRole).handler(this::apiGetAllRunningConfigs);
		router.get(API_ONE_RUNNING_CONFIG).handler(this::checkAdminRole).handler(this::apiGetUserRunningConfig);
		router.get(API_RUNNING_CONFIG).handler(this::checkAgentRole).handler(this::apiGetAgentRunningConfig);
		router.put(API_RUNNING_CONFIG).handler(this::checkAgentRole).handler(this::apiPutAgentRunningConfig);
		router.patch(API_RUNNING_CONFIG).handler(this::checkAgentRole).handler(this::apiPatchAgentRunningConfig);
		// router.delete(API_ONE_RUNNING_CONFIG).handler(this::checkAdminRole).handler(this::apiDeleteRunningConfig);

		// get HTTP host and port from configuration, or use default value
		String host = config().getString("ndnet.http.address", "0.0.0.0");
		int port = config().getInteger("ndnet.http.port", 8088);

		// create HTTP server and publish REST service
		createHttpServer(router, host, port)
				.compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port))
				.onComplete(future);
	}

	private void apiVersion(RoutingContext context) { 
		context.response()
		.end(new JsonObject()
				.put("name", SERVICE_NAME)
				.put("version", "v1").encodePrettily());
	}

	// Prefix Announcement API
	private void apiAddPrefixAnnouncement(RoutingContext context) {
		Prefix prefix;
		try {
			prefix = Json.decodeValue(context.getBodyAsString(), Prefix.class);
		} catch (DecodeException e) {
			badRequest(context, e);
			return;
		}
		if (prefix.getName() == null) {
			badRequest(context, new Throwable("name is missing"));
			return;
		}
		if (!Functional.isBase64(prefix.getName())) {
			badRequest(context, new Throwable("name must be a base64 string"));
			return;
		}

		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		prefix.setOriginId(principal.getInteger("nodeId"));

		ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx)
				.setAddress(TopologyService.SERVICE_ADDRESS);
  		TopologyService service = builder.build(TopologyService.class);
  
  		service.addPrefix(prefix, res -> {
			if (res.succeeded()) {
				resultVoidHandler(context, 201).handle(Future.succeededFuture());
			} else {
				internalError(context, res.cause());
			}
		});
	}
	private void apiDeletePrefixAnnouncement(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		int originId = principal.getInteger("nodeId");
		String name = context.request().getParam("name");

		ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx)
				.setAddress(TopologyService.SERVICE_ADDRESS);
  		TopologyService service = builder.build(TopologyService.class);
  
  		service.deletePrefixByName(originId, name, deleteResultHandler(context));
	}

	// Configuration API
	/* Candidate (intended) config */
	private void apiGetAllCandidateConfigs(RoutingContext context) {
		service.getAllCandidateConfigs(resultHandler(context, Json::encodePrettily));
	}
	private void apiGetUserCandidateConfig(RoutingContext context) {
		int nodeId = Integer.valueOf(context.request().getParam("nodeId"));
		getCandidateConfig(nodeId, context);
	}
	private void apiGetAgentCandidateConfig(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		int nodeId = principal.getInteger("nodeId");
		getCandidateConfig(nodeId, context);
	}
	private void getCandidateConfig(int nodeId, RoutingContext context) {
		service.getCandidateConfig(nodeId, res -> {
			if (res.succeeded()) {
				ConfigObj cg = res.result();
				if (cg == null) {
					notFound(context);
				} else {
					String ifNoneMatch = context.request().headers().get(HttpHeaders.IF_NONE_MATCH);
					String etag = String.valueOf(cg.hashCode());
					if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
						notChanged(context);
					} else {
						context.response()
								.setStatusCode(200)
								.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
								.putHeader(HttpHeaders.ETAG, etag)
								.end(cg.getConfig().toString());
					}
				}
			} else {
				internalError(context, res.cause());
			}
		});
	}
	/* private void apiDeleteCandidateConfig(RoutingContext context) {
		int nodeId = Integer.valueOf(context.request().getParam("nodeId"));
		service.removeCandidateConfig(nodeId, deleteResultHandler(context));
	} */
	
	/* Running config */
	private void apiGetAllRunningConfigs(RoutingContext context) {
		service.getAllRunningConfigs(resultHandler(context, Json::encodePrettily));
	}
	private void apiGetUserRunningConfig(RoutingContext context) {
		int nodeId = Integer.valueOf(context.request().getParam("nodeId"));
		service.getRunningConfig(nodeId, resultHandlerNonEmpty(context));
	}
	private void apiGetAgentRunningConfig(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		int nodeId = principal.getInteger("nodeId");
		service.getRunningConfig(nodeId, resultHandlerNonEmpty(context));
	}
	private void apiPutAgentRunningConfig(RoutingContext context) {
		ConfigObj config;
		try {
			config = Json.decodeValue(context.getBodyAsString(), ConfigObj.class);
		} catch (DecodeException e) {
			badRequest(context, new Throwable("wrong or missing request body"));
			return;
		}
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		int nodeId = principal.getInteger("nodeId");
		service.upsertRunningConfig(nodeId, config, resultVoidHandler(context, 201));
	}
	private void apiPatchAgentRunningConfig(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		int nodeId = principal.getInteger("nodeId");
		try {
			Object patch = Json.decodeValue(context.getBodyAsString());
			if (patch instanceof JsonArray) {
				service.updateRunningConfig(nodeId, (JsonArray) patch, resultVoidHandler(context, 201));
			} else {
				badRequest(context, new IllegalStateException("json patch must be an array"));
			}
		} catch (DecodeException e) {
			badRequest(context, new Throwable("wrong or missing request body"));
		}
	}
	/* private void apiDeleteRunningConfig(RoutingContext context) {
		int nodeId = Integer.valueOf(context.request().getParam("nodeId"));
		service.removeRunningConfig(nodeId, deleteResultHandler(context));
	} */
}
