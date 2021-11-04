package io.nms.central.microservice.topology.api;

import java.util.Base64;

import io.nms.central.microservice.common.RestAPIVerticle;
import io.nms.central.microservice.common.functional.JSONUtils;
import io.nms.central.microservice.topology.TopologyService;
import io.nms.central.microservice.topology.model.CrossConnect;
import io.nms.central.microservice.topology.model.Prefix;
import io.nms.central.microservice.topology.model.Vconnection;
import io.nms.central.microservice.topology.model.Vctp;
import io.nms.central.microservice.topology.model.Vlink;
import io.nms.central.microservice.topology.model.VlinkConn;
import io.nms.central.microservice.topology.model.Vltp;
import io.nms.central.microservice.topology.model.Vnode;
import io.nms.central.microservice.topology.model.Vsubnet;
import io.nms.central.microservice.topology.model.Vctp.ConnTypeEnum;
import io.nms.central.microservice.topology.model.Vnode.NodeTypeEnum;
import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * This verticle exposes a HTTP endpoint to process topology operations with
 * REST APIs.
 */
public class RestTopologyAPIVerticle extends RestAPIVerticle {

	private static final Logger logger = LoggerFactory.getLogger(RestTopologyAPIVerticle.class);

	public static final String SERVICE_NAME = "topology-rest-api";

	private static final String API_VERSION = "/v";

	private static final String API_ONE_SUBNET = "/subnet/:subnetId";
	private static final String API_ALL_SUBNETS = "/subnet";

	private static final String API_ONE_NODE = "/node/:nodeId";
	private static final String API_ALL_NODES = "/node";
	private static final String API_NODES_BY_TYPE = "/node/type/:type";
	private static final String API_NODES_BY_SUBNET = "/subnet/:subnetId/nodes";

	private static final String API_ONE_LTP = "/ltp/:ltpId";
	private static final String API_ALL_LTPS = "/ltp";
	private static final String API_LTPS_BY_NODE = "/node/:nodeId/ltps";

	private static final String API_ONE_CTP = "/ctp/:ctpId";
	private static final String API_ALL_CTPS = "/ctp";
	private static final String API_CTPS_BY_TYPE = "/ctp/type/:type";
	private static final String API_CTPS_BY_LTP = "/ltp/:ltpId/ctps";
	private static final String API_CTPS_BY_CTP = "/ctp/:ctpId/ctps";
	private static final String API_CTPS_BY_NODE = "/node/:nodeId/ctps";

	private static final String API_ONE_LINK = "/link/:linkId";
	private static final String API_ALL_LINKS = "/link";
	private static final String API_LINKS_BY_SUBNET = "/subnet/:subnetId/links";

	private static final String API_ONE_LINKCONN = "/lc/:lcId";
	private static final String API_ALL_LINKCONNS = "/lc";
	private static final String API_LINKCONNS_BY_LINK = "/link/:linkId/lcs";
	private static final String API_LINKCONNS_BY_SUBNET = "/subnet/:subnetId/lcs";

	private static final String API_ONE_CONNECTION = "/connection/:connectionId";
	private static final String API_ALL_CONNECTIONS = "/connection";
	private static final String API_CONNECTIONS_BY_TYPE = "/connection/type/:type";
	private static final String API_CONNECTIONS_BY_SUBNET = "/subnet/:subnetId/connections";

	private static final String API_PREFIX = "/prefix";
	private static final String API_ONE_PREFIX = "/prefix/:prefixId";
	private static final String API_PREFIXES_BY_SUBNET = "/subnet/:subnetId/prefixes";
	private static final String API_PREFIXES_BY_NODE = "/node/:nodeId/prefixes";

	private static final String API_ONE_CROSS_CONNECT = "/cross-connect/:id";
	private static final String API_ALL_CROSS_CONNECTS = "/cross-connect";
	private static final String API_CROSS_CONNECTS_BY_NODE = "/node/:id/cross-connects";

	private final TopologyService service;

	public RestTopologyAPIVerticle(TopologyService service) {
		this.service = service;
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		super.start();
		final Router router = Router.router(vertx);
		// body handler
		router.route().handler(BodyHandler.create());

		// API route handler
		router.get(API_VERSION).handler(this::apiVersion);

		router.post(API_ALL_SUBNETS).handler(this::checkAdminRole).handler(this::apiAddSubnet);
		router.get(API_ALL_SUBNETS).handler(this::checkAdminRole).handler(this::apiGetAllSubnets);
		router.get(API_ONE_SUBNET).handler(this::checkAdminRole).handler(this::apiGetSubnet);
		router.delete(API_ONE_SUBNET).handler(this::checkAdminRole).handler(this::apiDeleteSubnet);
		router.put(API_ONE_SUBNET).handler(this::checkAdminRole).handler(this::apiUpdateSubnet);

		router.post(API_ALL_NODES).handler(this::checkAdminRole).handler(this::apiAddNode);
		router.get(API_ALL_NODES).handler(this::checkAdminRole).handler(this::apiGetAllNodes);
		router.get(API_NODES_BY_SUBNET).handler(this::checkAdminRole).handler(this::apiGetNodesBySubnet);
		router.get(API_NODES_BY_TYPE).handler(this::checkAdminRole).handler(this::apiGetNodesByType);
		router.get(API_ONE_NODE).handler(this::checkAdminRole).handler(this::apiGetNode);
		router.delete(API_ONE_NODE).handler(this::checkAdminRole).handler(this::apiDeleteNode);
		router.put(API_ONE_NODE).handler(this::checkAdminRole).handler(this::apiUpdateNode);

		router.post(API_ALL_LTPS).handler(this::checkAdminRole).handler(this::apiAddLtp);
		router.get(API_ALL_LTPS).handler(this::checkAdminRole).handler(this::apiGetAllLtps);
		router.get(API_LTPS_BY_NODE).handler(this::checkAdminRole).handler(this::apiGetLtpsByNode);
		router.get(API_ONE_LTP).handler(this::checkAdminRole).handler(this::apiGetLtp);
		router.delete(API_ONE_LTP).handler(this::checkAdminRole).handler(this::apiDeleteLtp);
		router.put(API_ONE_LTP).handler(this::checkAdminRole).handler(this::apiUpdateLtp);

		router.post(API_ALL_CTPS).handler(this::checkAdminRole).handler(this::apiAddCtp);
		router.get(API_CTPS_BY_TYPE).handler(this::checkAdminRole).handler(this::apiGetCtpsByType);
		router.get(API_ALL_CTPS).handler(this::checkAdminRole).handler(this::apiGetAllCtps);
		router.get(API_CTPS_BY_CTP).handler(this::checkAdminRole).handler(this::apiGetCtpsByCtp);
		router.get(API_CTPS_BY_LTP).handler(this::checkAdminRole).handler(this::apiGetCtpsByLtp);
		router.get(API_CTPS_BY_NODE).handler(this::checkAdminRole).handler(this::apiGetCtpsByNode);
		router.get(API_ONE_CTP).handler(this::checkAdminRole).handler(this::apiGetCtp);
		router.delete(API_ONE_CTP).handler(this::checkAdminRole).handler(this::apiDeleteCtp);
		router.put(API_ONE_CTP).handler(this::checkAdminRole).handler(this::apiUpdateCtp);

		router.post(API_ALL_LINKS).handler(this::checkAdminRole).handler(this::apiAddLink);
		router.get(API_ALL_LINKS).handler(this::checkAdminRole).handler(this::apiGetAllLinks);
		router.get(API_LINKS_BY_SUBNET).handler(this::checkAdminRole).handler(this::apiGetLinksBySubnet);
		router.get(API_ONE_LINK).handler(this::checkAdminRole).handler(this::apiGetLink);
		router.delete(API_ONE_LINK).handler(this::checkAdminRole).handler(this::apiDeleteLink);
		router.put(API_ONE_LINK).handler(this::checkAdminRole).handler(this::apiUpdateLink);

		router.post(API_ALL_LINKCONNS).handler(this::checkAdminRole).handler(this::apiAddLinkConn);
		router.get(API_ALL_LINKCONNS).handler(this::checkAdminRole).handler(this::apiGetAllLinkConns);
		router.get(API_LINKCONNS_BY_LINK).handler(this::checkAdminRole).handler(this::apiGetLinkConnsByLink);
		router.get(API_LINKCONNS_BY_SUBNET).handler(this::checkAdminRole).handler(this::apiGetLinkConnsBySubnet);
		router.get(API_ONE_LINKCONN).handler(this::checkAdminRole).handler(this::apiGetLinkConn);
		router.delete(API_ONE_LINKCONN).handler(this::checkAdminRole).handler(this::apiDeleteLinkConn);
		router.put(API_ONE_LINKCONN).handler(this::checkAdminRole).handler(this::apiUpdateLinkConn);

		router.post(API_ALL_CONNECTIONS).handler(this::checkAdminRole).handler(this::apiAddConnection);
		router.get(API_ALL_CONNECTIONS).handler(this::checkAdminRole).handler(this::apiGetAllConnections);
		router.get(API_CONNECTIONS_BY_TYPE).handler(this::checkAdminRole).handler(this::apiGetConnectionsByType);
		router.get(API_CONNECTIONS_BY_SUBNET).handler(this::checkAdminRole).handler(this::apiGetConnectionsBySubnet);
		router.get(API_ONE_CONNECTION).handler(this::checkAdminRole).handler(this::apiGetConnection);
		router.delete(API_ONE_CONNECTION).handler(this::checkAdminRole).handler(this::apiDeleteConnection);
		router.put(API_ONE_CONNECTION).handler(this::checkAdminRole).handler(this::apiUpdateConnection);

		router.post(API_PREFIX).handler(this::checkAdminRole).handler(this::apiAddPrefix);
		router.get(API_PREFIX).handler(this::checkAdminRole).handler(this::apiGetAllPrefixes);
		router.get(API_PREFIXES_BY_SUBNET).handler(this::checkAdminRole).handler(this::apiGetPrefixesBySubnet);
		router.get(API_PREFIXES_BY_NODE).handler(this::checkAdminRole).handler(this::apiGetPrefixesByNode);
		router.get(API_ONE_PREFIX).handler(this::checkAdminRole).handler(this::apiGetPrefix);
		router.delete(API_ONE_PREFIX).handler(this::checkAdminRole).handler(this::apiDeletePrefix);
		
		router.post(API_ALL_CROSS_CONNECTS).handler(this::checkAdminRole).handler(this::apiAddCrossConnect);
		router.get(API_CROSS_CONNECTS_BY_NODE).handler(this::checkAdminRole).handler(this::apiGetCrossConnectsByNode);
		router.get(API_ONE_CROSS_CONNECT).handler(this::checkAdminRole).handler(this::apiGetCrossConnectById);
		router.delete(API_ONE_CROSS_CONNECT).handler(this::checkAdminRole).handler(this::apiDeleteCrossConnect);

		// get HTTP host and port from configuration, or use default value
		String host = config().getString("topology.http.address", "0.0.0.0");
		int port = config().getInteger("topology.http.port", 8085);

		// create HTTP server and publish REST service
		createHttpServer(router, host, port)
				.compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port))
				.onComplete(future);
	}

	private void apiVersion(RoutingContext context) {
		context.response().end(new JsonObject().put("name", SERVICE_NAME).put("version", "v1").encodePrettily());
	}

	// Subnet API
	private void apiAddSubnet(RoutingContext context) {
		try {
			final Vsubnet vsubnet = JSONUtils.json2PojoE(context.getBodyAsString(), Vsubnet.class);
			service.addVsubnet(vsubnet, createResultHandler(context, "/subnet"));
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			badRequest(context, e);
		}
	}

	private void apiGetSubnet(RoutingContext context) {
		String subnetId = context.request().getParam("subnetId");
		service.getVsubnet(subnetId, resultHandlerNonEmpty(context));
	}

	private void apiGetAllSubnets(RoutingContext context) {
		service.getAllVsubnets(resultHandler(context, Json::encodePrettily));
	}

	private void apiDeleteSubnet(RoutingContext context) {
		String subnetId = context.request().getParam("subnetId");
		service.deleteVsubnet(subnetId, deleteResultHandler(context));
	}

	private void apiUpdateSubnet(RoutingContext context) {
		String id = context.request().getParam("subnetId");
		final Vsubnet vsubnet = Json.decodeValue(context.getBodyAsString(), Vsubnet.class);
		service.updateVsubnet(id, vsubnet, resultVoidHandler(context, 200));
	}

	// Node API
	private void apiAddNode(RoutingContext context) {
		try {
			final Vnode vnode = JSONUtils.json2PojoE(context.getBodyAsString(), Vnode.class);
			service.addVnode(vnode, res -> {
				if (res.succeeded()) {
					notifyTopologyChange();
				}
				createResultHandler(context, "/node").handle(res);
			});
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			badRequest(context, e);
		}
	}

	private void apiGetNode(RoutingContext context) {
		String nodeId = context.request().getParam("nodeId");
		service.getVnode(nodeId, resultHandlerNonEmpty(context));
	}

	private void apiGetAllNodes(RoutingContext context) {
		service.getAllVnodes(resultHandler(context, Json::encodePrettily));
	}

	private void apiGetNodesBySubnet(RoutingContext context) {
		String subnetId = context.request().getParam("subnetId");
		service.getVnodesByVsubnet(subnetId, resultHandler(context, Json::encodePrettily));
	}

	private void apiGetNodesByType(RoutingContext context) {
		String type = context.request().getParam("type");		
		service.getVnodesByType(NodeTypeEnum.valueOf(type), resultHandler(context, Json::encodePrettily));		
	}

	private void apiDeleteNode(RoutingContext context) {
		String nodeId = context.request().getParam("nodeId");
		// service.deleteVnode(nodeId, deleteResultHandler(context));
		service.deleteVnode(nodeId, res -> {
			if (res.succeeded()) {
				notifyTopologyChange();
			}
			deleteResultHandler(context).handle(res);
		});
	}

	private void apiUpdateNode(RoutingContext context) {
		String id = context.request().getParam("nodeId");
		final Vnode vnode = Json.decodeValue(context.getBodyAsString(), Vnode.class);
		service.updateVnode(id, vnode, resultVoidHandler(context, 200));
	}

	// Ltp API
	private void apiAddLtp(RoutingContext context) {
		try {
			final Vltp vltp = JSONUtils.json2PojoE(context.getBodyAsString(), Vltp.class);
			service.addVltp(vltp, createResultHandler(context, "/ltp"));
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			badRequest(context, e);
		}		
	}

	private void apiGetLtp(RoutingContext context) {
		String ltpId = context.request().getParam("ltpId");
		service.getVltp(ltpId, resultHandlerNonEmpty(context));
	}

	private void apiGetLtpsByNode(RoutingContext context) {
		String nodeId = context.request().getParam("nodeId");
		service.getVltpsByVnode(nodeId, resultHandler(context, Json::encodePrettily));
	}

	private void apiGetAllLtps(RoutingContext context) {
		service.getAllVltps(resultHandler(context, Json::encodePrettily));
	}

	private void apiDeleteLtp(RoutingContext context) {
		String ltpId = context.request().getParam("ltpId");
		service.deleteVltp(ltpId, deleteResultHandler(context));
	}

	private void apiUpdateLtp(RoutingContext context) {
		String id = context.request().getParam("ltpId");
		final Vltp vltp = Json.decodeValue(context.getBodyAsString(), Vltp.class);
		service.updateVltp(id, vltp, resultVoidHandler(context, 200));
	}

	// Ctp API
	private void apiAddCtp(RoutingContext context) {
		try {
			final Vctp vctp = JSONUtils.json2PojoE(context.getBodyAsString(), Vctp.class);
			service.addVctp(vctp, res -> {
				if (res.succeeded()) {
					notifyTopologyChange();
				}
				createResultHandler(context, "/ctp").handle(res);
			});
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	private void apiGetCtp(RoutingContext context) {
		String ctpId = context.request().getParam("ctpId");
		service.getVctp(ctpId, resultHandlerNonEmpty(context));
	}
	private void apiGetCtpsByCtp(RoutingContext context) {
		String ltpId = context.request().getParam("ctpId");		
		service.getVctpsByVctp(ltpId, resultHandler(context, Json::encodePrettily));		
	}
	private void apiGetCtpsByLtp(RoutingContext context) {
		String ltpId = context.request().getParam("ltpId");		
		service.getVctpsByVltp(ltpId, resultHandler(context, Json::encodePrettily));		
	}
	private void apiGetCtpsByNode(RoutingContext context) {
		String nodeId = context.request().getParam("nodeId");		
		service.getVctpsByVnode(nodeId, resultHandler(context, Json::encodePrettily));		
	}
	private void apiGetCtpsByType(RoutingContext context) {
		String type = context.request().getParam("type");		
		service.getVctpsByType(ConnTypeEnum.valueOf(type), resultHandler(context, Json::encodePrettily));		
	}
	private void apiGetAllCtps(RoutingContext context) {		
		service.getAllVctps(resultHandler(context, Json::encodePrettily));
	}
	private void apiDeleteCtp(RoutingContext context) {
		String ctpId = context.request().getParam("ctpId");		
		// service.deleteVctp(ctpId, deleteResultHandler(context));
		service.deleteVctp(ctpId, res -> {
			if (res.succeeded()) {
				notifyTopologyChange();
			}
			deleteResultHandler(context).handle(res);
		});
	}
	private void apiUpdateCtp(RoutingContext context) {
		String id = context.request().getParam("ctpId");
		final Vctp vctp = Json.decodeValue(context.getBodyAsString(), Vctp.class);		
		service.updateVctp(id, vctp, resultVoidHandler(context, 200));
	}

	// Link API
	private void apiAddLink(RoutingContext context) {
		try {
			final Vlink vlink = JSONUtils.json2PojoE(context.getBodyAsString(), Vlink.class);
			service.addVlink(vlink, createResultHandler(context, "/link"));
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	private void apiGetLink(RoutingContext context) {
		String linkId = context.request().getParam("linkId");
		service.getVlink(linkId, resultHandlerNonEmpty(context));
	}
	private void apiGetAllLinks(RoutingContext context) {
		service.getAllVlinks(resultHandler(context, Json::encodePrettily));
	}
	private void apiGetLinksBySubnet(RoutingContext context) {	
		String subnetId = context.request().getParam("subnetId");		
		service.getVlinksByVsubnet(subnetId, resultHandler(context, Json::encodePrettily));
	}
	private void apiDeleteLink(RoutingContext context) {
		String linkId = context.request().getParam("linkId");		
		service.deleteVlink(linkId, deleteResultHandler(context));	
	}
	private void apiUpdateLink(RoutingContext context) {
		String id = context.request().getParam("linkId");
		final Vlink vlink = Json.decodeValue(context.getBodyAsString(), Vlink.class);		
		service.updateVlink(id, vlink, resultVoidHandler(context, 200));
	}

	// LinkConn API
	private void apiAddLinkConn(RoutingContext context) {
		try {
			final VlinkConn vlc = JSONUtils.json2PojoE(context.getBodyAsString(), VlinkConn.class);
			service.addVlinkConn(vlc, createResultHandler(context, "/lc"));
		} catch (Exception e) {
			badRequest(context, e);
		}
	}
	private void apiGetLinkConn(RoutingContext context) {
		String linkConnId = context.request().getParam("lcId");		
		service.getVlinkConn(linkConnId, resultHandlerNonEmpty(context));
	}
	private void apiGetAllLinkConns(RoutingContext context) {
		service.getAllVlinkConns(resultHandler(context, Json::encodePrettily));
	}
	private void apiGetLinkConnsByLink(RoutingContext context) {	
		String linkId = context.request().getParam("linkId");		
		service.getVlinkConnsByVlink(linkId, resultHandler(context, Json::encodePrettily));
	}
	private void apiGetLinkConnsBySubnet(RoutingContext context) {	
		String subnetId = context.request().getParam("subnetId");		
		service.getVlinkConnsByVsubnet(subnetId, resultHandler(context, Json::encodePrettily));
	}
	private void apiDeleteLinkConn(RoutingContext context) {
		String linkConnId = context.request().getParam("lcId");		
		service.deleteVlinkConn(linkConnId, deleteResultHandler(context));
	}
	private void apiUpdateLinkConn(RoutingContext context) {
		String id = context.request().getParam("lcId");
		final VlinkConn vlinkConn = Json.decodeValue(context.getBodyAsString(), VlinkConn.class);		
		service.updateVlinkConn(id, vlinkConn, resultVoidHandler(context, 200));
	}

	// connection API
	private void apiAddConnection(RoutingContext context) {
		try {
			final Vconnection vconn = JSONUtils.json2PojoE(context.getBodyAsString(), Vconnection.class);
			service.addVconnection(vconn, res -> {
				if (res.succeeded()) {
					notifyTopologyChange();
				}
				createResultHandler(context, "/connection").handle(res);
			});
		} catch (Exception e) {
			badRequest(context, e);
		}
	}
	private void apiGetConnection(RoutingContext context) {
		String connectionId = context.request().getParam("connectionId");		
		service.getVconnection(connectionId, resultHandlerNonEmpty(context));
	}
	private void apiGetAllConnections(RoutingContext context) {
		service.getAllVconnections(resultHandler(context, Json::encodePrettily));
	}
	private void apiGetConnectionsByType(RoutingContext context) {
		String type = context.request().getParam("type");		
		service.getVconnectionsByType(ConnTypeEnum.valueOf(type), resultHandler(context, Json::encodePrettily));		
	}
	private void apiGetConnectionsBySubnet(RoutingContext context) {	
		String subnetId = context.request().getParam("subnetId");		
		service.getVconnectionsByVsubnet(subnetId, resultHandler(context, Json::encodePrettily));
	}
	private void apiDeleteConnection(RoutingContext context) {
		String connectionId = context.request().getParam("connectionId");
		service.deleteVconnection(connectionId, res -> {
			if (res.succeeded()) {
				notifyTopologyChange();
			}
			deleteResultHandler(context).handle(res);
		});
	}
	private void apiUpdateConnection(RoutingContext context) {
		String id = context.request().getParam("connectionId");
		final Vconnection vconnection = Json.decodeValue(context.getBodyAsString(), Vconnection.class);		
		service.updateVconnection(id, vconnection, resultVoidHandler(context, 200));
	}

	// Prefix Announcement API
	private void apiAddPrefix(RoutingContext context) {
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
		if (!isBase64(prefix.getName())) {
			badRequest(context, new Throwable("name must be a base64 string"));
			return;
		}
		service.addPrefix(prefix, res -> {
			if (res.succeeded()) {
				notifyTopologyChange();
				notifyFrontend();
			}
			createResultHandler(context, "/prefix").handle(res);
		});
	}
	private void apiGetPrefix(RoutingContext context) {
		String prefixId = context.request().getParam("prefixId");			
		service.getPrefix(prefixId, resultHandlerNonEmpty(context));
	}
	private void apiGetAllPrefixes(RoutingContext context) {
		service.getAllPrefixes(resultHandler(context, Json::encodePrettily));
	}
	private void apiGetPrefixesBySubnet(RoutingContext context) {	
		String subnetId = context.request().getParam("subnetId");		
		service.getPrefixesByVsubnet(subnetId, resultHandler(context, Json::encodePrettily));
	}
	private void apiGetPrefixesByNode(RoutingContext context) {	
		String nodeId = context.request().getParam("nodeId");		
		service.getPrefixesByVnode(nodeId, resultHandler(context, Json::encodePrettily));
	}
	private void apiDeletePrefix(RoutingContext context) {
		String prefixId = context.request().getParam("prefixId");
		service.deletePrefix(prefixId, res -> {
			if (res.succeeded()) {
				notifyTopologyChange();
				notifyFrontend();
			}
			deleteResultHandler(context).handle(res);
		});
	}

	// CrossConnect API
	private void apiAddCrossConnect(RoutingContext context) {
		try {
			final CrossConnect crossConnect = JSONUtils.json2PojoE(context.getBodyAsString(), CrossConnect.class);
	
			service.addCrossConnect(crossConnect, res -> {
				if (res.succeeded()) {
					notifyTopologyChange();
				}
				createResultHandler(context, "/cross-connect").handle(res);
			});
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			badRequest(context, e);
		}
	}
	private void apiGetCrossConnectById(RoutingContext context) {
		String xcId = context.request().getParam("id");
		service.getCrossConnectById(xcId, resultHandlerNonEmpty(context));
	}
	private void apiGetCrossConnectsByNode(RoutingContext context) {
		String nodeId = context.request().getParam("id");
		service.getCrossConnectsByNode(nodeId, resultHandler(context, Json::encodePrettily));
	}
	private void apiDeleteCrossConnect(RoutingContext context) {
		String xcId = context.request().getParam("id");
		service.deletePrefix(xcId, res -> {
			if (res.succeeded()) {
				notifyTopologyChange();
				notifyFrontend();
			}
			deleteResultHandler(context).handle(res);
		});
	}

	private boolean isBase64(String str) {
		if (str.isEmpty()) {
			return false;
		}
		Base64.Decoder decoder = Base64.getDecoder();
		try {
			decoder.decode(str);
			return true;
		} catch(IllegalArgumentException iae) {
			return false;
		}
	}

	public void notifyTopologyChange() {
		// TODO: add type of change
		vertx.eventBus().publish(TopologyService.EVENT_ADDRESS, new JsonObject());
	}

	private void notifyFrontend() {
		vertx.eventBus().publish(TopologyService.FROTNEND_ADDRESS, new JsonObject());
	}
}
