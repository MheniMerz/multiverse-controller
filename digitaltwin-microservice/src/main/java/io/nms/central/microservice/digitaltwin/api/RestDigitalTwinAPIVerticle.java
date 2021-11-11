package io.nms.central.microservice.digitaltwin.api;

import io.nms.central.microservice.common.RestAPIVerticle;
import io.nms.central.microservice.common.functional.JSONUtils;
import io.nms.central.microservice.digitaltwin.DigitalTwinService;
import io.nms.central.microservice.digitaltwin.model.dt.DtQuery;
import io.nms.central.microservice.digitaltwin.model.graph.NetConfigCollection;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Bgp;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Configuration;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Device;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.NetInterface;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * This verticle exposes REST API endpoints to process Digital-Twin service operation
 */
public class RestDigitalTwinAPIVerticle extends RestAPIVerticle {

	private static final Logger logger = LoggerFactory.getLogger(RestDigitalTwinAPIVerticle.class);

	public static final String SERVICE_NAME = "digitaltwin-rest-api";

	private static final String API_VERSION = "/v";
	
	private static final String API_RUNNING = "/running";
	private static final String API_RUNNING_REPORT = "/running/report";
	private static final String API_RUNNING_QUERY = "/running/query";
	private static final String API_RUNNING_NETWORK = "/running/network";
	private static final String API_RUNNING_INTERFACES = "/running/device/:deviceName/interfaces";
	private static final String API_RUNNING_BGPS = "/running/device/:deviceName/bgps";
	
	private static final String API_ONE_VIEW = "/view/:viewId";
	private static final String API_VIEW_CONFIG_VERIFY = "/view/:viewId/config/verify";
	private static final String API_VIEW_CONFIG_GENERATE = "/view/:viewId/config/generate";
	private static final String API_VIEW_NETWORK = "/view/:viewId/network";
	private static final String API_VIEW_ONE_DEVICE = "/view/:viewId/device/:deviceName";
	private static final String API_VIEW_INTERFACES = "/view/:viewId/device/:deviceName/interfaces";
	private static final String API_VIEW_ONE_INTERFACE = "/view/:viewId/device/:deviceName/interface/:itfName";
	private static final String API_VIEW_BGPS = "/view/:viewId/device/:deviceName/bgps";
	private static final String API_VIEW_ONE_BGP = "/view/:viewId/device/:deviceName/bgp/:itfAddr";
	

	private final DigitalTwinService service;

	public RestDigitalTwinAPIVerticle(DigitalTwinService service) {
		this.service = service;
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		super.start();
		final Router router = Router.router(vertx);
		
		// body handler
		router.route().handler(BodyHandler.create());
		
		// version
		router.get(API_VERSION).handler(this::apiVersion);
		
		/* Operations on running network */
		router.post(API_RUNNING).handler(this::checkAdminRole).handler(this::apiRunningProcessNetworkConfig);
		router.post(API_RUNNING_REPORT).handler(this::checkAdminRole).handler(this::apiRunningVerifyNetworkConfig);
		router.post(API_RUNNING_QUERY).handler(this::checkAdminRole).handler(this::apiRunningQueryNetwork);
		router.get(API_RUNNING_NETWORK).handler(this::checkAdminRole).handler(this::apiRunningGetNetwork);
		router.get(API_RUNNING_INTERFACES).handler(this::checkAdminRole).handler(this::apiRunningGetDeviceInterfaces);
		router.get(API_RUNNING_BGPS).handler(this::checkAdminRole).handler(this::apiRunningGetDeviceBgps);
		
		/* Operations on view network */
		router.post(API_ONE_VIEW).handler(this::checkAdminRole).handler(this::apiCreateView);
		router.delete(API_ONE_VIEW).handler(this::checkAdminRole).handler(this::apiDeleteView);
		
		router.post(API_VIEW_CONFIG_VERIFY).handler(this::checkAdminRole).handler(this::apiViewVerifyConfig);
		router.post(API_VIEW_CONFIG_GENERATE).handler(this::checkAdminRole).handler(this::apiViewGenerateNetworkConfig);
		
		router.get(API_VIEW_NETWORK).handler(this::checkAdminRole).handler(this::apiViewGetNetwork);
		
		// router.get(API_VIEW_ONE_DEVICE).handler(this::checkAdminRole).handler(this::apiViewGetDevice);
		router.put(API_VIEW_ONE_DEVICE).handler(this::checkAdminRole).handler(this::apiViewUpdateDevice);
		
		router.get(API_VIEW_INTERFACES).handler(this::checkAdminRole).handler(this::apiViewGetDeviceInterfaces);
		// router.get(API_VIEW_ONE_INTERFACE).handler(this::checkAdminRole).handler(this::apiViewGetInterface);
		router.put(API_VIEW_ONE_INTERFACE).handler(this::checkAdminRole).handler(this::apiViewUpdateInterface);
		
		router.get(API_VIEW_BGPS).handler(this::checkAdminRole).handler(this::apiViewGetDeviceBgps);
		// router.get(API_VIEW_ONE_BGP).handler(this::checkAdminRole).handler(this::apiViewGetBgp);
		// router.post(API_VIEW_ONE_BGP).handler(this::checkAdminRole).handler(this::apiViewCreateBgp);
		router.put(API_VIEW_ONE_BGP).handler(this::checkAdminRole).handler(this::apiViewUpdateBgp);
		router.delete(API_VIEW_ONE_BGP).handler(this::checkAdminRole).handler(this::apiViewDeleteBgp);

		// get HTTP host and port from configuration, or use default value
		String host = config().getString("digitaltwin.http.address", "0.0.0.0");
		int port = config().getInteger("digitaltwin.http.port", 8084);

		// create HTTP server and publish REST service
		createHttpServer(router, host, port)
				.compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port))
				.onComplete(future);
	}
	
	/* Operations on running network */
	private void apiRunningProcessNetworkConfig(RoutingContext context) {
		try {
			final NetConfigCollection netConfig 
					= JSONUtils.json2PojoE(context.getBodyAsString(), NetConfigCollection.class);
			service.runningProcessNetworkConfig(netConfig, resultHandlerNonEmpty(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	private void apiRunningVerifyNetworkConfig(RoutingContext context) {
		service.runningVerifyNetworkConfig(resultHandlerNonEmpty(context));
	}
	private void apiRunningQueryNetwork(RoutingContext context) {
		try {
			final DtQuery query 
					= JSONUtils.json2PojoE(context.getBodyAsString(), DtQuery.class);
			service.runningQueryNetwork(query, resultHandlerNonEmpty(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	private void apiRunningGetNetwork(RoutingContext context) {
		service.runningGetNetwork(resultHandlerNonEmpty(context));
	}
	private void apiRunningGetDeviceInterfaces(RoutingContext context) {
		String deviceName = context.request().getParam("deviceName");
		logger.info("device name: " + deviceName);
		service.runningGetDeviceInterfaces(deviceName, resultHandler(context, Json::encodePrettily));
	}
	private void apiRunningGetDeviceBgps(RoutingContext context) {
		String deviceName = context.request().getParam("deviceName");
		service.runningGetDeviceBgps(deviceName, resultHandler(context, Json::encodePrettily));
	}
	
	/* Operations on view network */
	// view
	private void apiCreateView(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		service.createView(viewId, createResultHandler(context));
	}
	private void apiDeleteView(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		service.deleteView(viewId, deleteResultHandler(context));
	}
	
	// view config
	private void apiViewVerifyConfig(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		try {
			final Configuration config 
					= JSONUtils.json2PojoE(context.getBodyAsString(), Configuration.class);
			service.viewVerifyConfig(viewId, config, resultHandlerNonEmpty(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	private void apiViewGenerateNetworkConfig(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		try {
			final Configuration config 
					= JSONUtils.json2PojoE(context.getBodyAsString(), Configuration.class);
			service.viewVerifyConfig(viewId, config, resultHandlerNonEmpty(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	
	// view network
	private void apiViewGetNetwork(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		service.viewGetNetwork(viewId, resultHandlerNonEmpty(context));
	}
	
	// view device
	/* private void apiViewGetDevice(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		service.viewGetDevice(viewId, deviceName, resultHandlerNonEmpty(context));
	} */
	private void apiViewUpdateDevice(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		try {
			final Device device 
					= JSONUtils.json2PojoE(context.getBodyAsString(), Device.class);
			service.viewUpdateDevice(viewId, deviceName, device, updateResultHandler(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	
	// view interface
	private void apiViewGetDeviceInterfaces(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		service.viewGetDeviceInterfaces(viewId, deviceName, resultHandler(context, Json::encodePrettily));
	}
	/* private void apiViewGetInterface(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		String itfName = context.request().getParam("itfName");
		service.viewGetInterface(viewId, deviceName, itfName, resultHandlerNonEmpty(context));
	} */
	private void apiViewUpdateInterface(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		String itfName = context.request().getParam("itfName");
		try {
			final NetInterface netItf
					= JSONUtils.json2PojoE(context.getBodyAsString(), NetInterface.class);
			service.viewUpdateInterface(viewId, deviceName, itfName, netItf, updateResultHandler(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	
	// view bgp
	private void apiViewGetDeviceBgps(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		service.viewGetDeviceBgps(viewId, deviceName, resultHandler(context, Json::encodePrettily));
	}
	/* private void apiViewGetBgp(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		String itfAddr = context.request().getParam("itfAddr");
		service.viewGetBgp(viewId, deviceName, itfAddr, resultHandlerNonEmpty(context));
	} */
	/* private void apiViewCreateBgp(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		String itfAddr = context.request().getParam("itfAddr");
		try {
			final Bgp bgp 
					= JSONUtils.json2PojoE(context.getBodyAsString(), Bgp.class);
			service.viewCreateBgp(viewId, deviceName, itfAddr, bgp, createResultHandler(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	} */
	private void apiViewUpdateBgp(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		String itfAddr = context.request().getParam("itfAddr");
		try {
			final Bgp bgp 
					= JSONUtils.json2PojoE(context.getBodyAsString(), Bgp.class);
			service.viewUpdateBgp(viewId, deviceName, itfAddr, bgp, updateResultHandler(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	private void apiViewDeleteBgp(RoutingContext context) {
		String viewId = context.request().getParam("viewId");
		String deviceName = context.request().getParam("deviceName");
		String itfAddr = context.request().getParam("itfAddr");
		service.viewDeleteBgp(viewId, deviceName, itfAddr, deleteResultHandler(context));
	}
	
	/* API version */
	private void apiVersion(RoutingContext context) {
		context.response().end(new JsonObject()
				.put("name", SERVICE_NAME)
				.put("version", "v1").encodePrettily());
	}

	/* eventbus messages */
	private void notifyDigitalTwinChange() {
		// TODO: add type of change
		vertx.eventBus().publish(DigitalTwinService.EVENT_ADDRESS, new JsonObject());
	}

	private void notifyFrontend() {
		vertx.eventBus().publish(DigitalTwinService.FROTNEND_ADDRESS, new JsonObject());
	}
}
