package io.nms.central.microservice.digitaltwin;

import java.util.List;
import io.nms.central.microservice.digitaltwin.model.graph.NetworkState;
import io.nms.central.microservice.common.functional.Functional;
import io.nms.central.microservice.digitaltwin.model.dt.CreationReport;
import io.nms.central.microservice.digitaltwin.model.dt.VerificationReport;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.AclRule;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.AclTable;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Arp;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Bgp;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Device;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.RouteHop;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Vlan;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.VlanMember;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.IpRoute;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Link;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.NetInterface;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Network;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.Path;
import io.nms.central.microservice.digitaltwin.model.ipnetApi.PathHop;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * A service interface managing Digital Twin.
 */
@VertxGen
@ProxyGen
public interface DigitalTwinService {

	/**
	 * The name of the event bus service.
	 */
	String SERVICE_NAME = "digitaltwin-eb-service";

	/**
	 * The address on which the service is published.
	 */
	String SERVICE_ADDRESS = "service.digitaltwin";

	String FROTNEND_ADDRESS = "mvs.to.frontend";

	String EVENT_ADDRESS = "digitaltwin.event";

	@Fluent	
	DigitalTwinService initializePersistence(Handler<AsyncResult<Void>> resultHandler);

	/* Operations on running network */
	@Fluent	
	DigitalTwinService processNetworkRunningState(NetworkState netState, Handler<AsyncResult<CreationReport>> resultHandler);
	@Fluent	
	DigitalTwinService runningVerifyNetwork(Handler<AsyncResult<VerificationReport>> resultHandler);
	@Fluent
	DigitalTwinService runningGetNetworkConfig(Handler<AsyncResult<JsonObject>> resultHandler);
	@Fluent	
	DigitalTwinService runningGetNetwork(Handler<AsyncResult<Network>> resultHandler);
	@Fluent	
	DigitalTwinService runningGetDevice(String deviceName, Handler<AsyncResult<Device>> resultHandler);
	@Fluent
	DigitalTwinService runningGetDeviceInterfaces(String deviceName, Handler<AsyncResult<List<NetInterface>>> resultHandler);
	@Fluent	
	DigitalTwinService runningGetInterface(String deviceName, String itfName, Handler<AsyncResult<NetInterface>> resultHandler);
	@Fluent	
	DigitalTwinService runningGetDeviceBgps(String deviceName, Handler<AsyncResult<List<Bgp>>> resultHandler);
	@Fluent	
	DigitalTwinService runningGetBgp(String deviceName, String itfAddr, Handler<AsyncResult<Bgp>> resultHandler);
	@Fluent
	DigitalTwinService runningGetDeviceVlans(String deviceName, Handler<AsyncResult<List<Vlan>>> resultHandler);
	@Fluent
	DigitalTwinService runningGetVlanMembers(String deviceName,String vid,  Handler<AsyncResult<List<VlanMember>>> resultHandler);
	@Fluent
	DigitalTwinService runningGetDeviceIpRoutes(String deviceName, Handler<AsyncResult<List<IpRoute>>> resultHandler);
	@Fluent	
	DigitalTwinService runningGetDeviceIpRoutesTo(String deviceName, String to, Handler<AsyncResult<List<IpRoute>>> resultHandler);
	@Fluent	
	DigitalTwinService runningGetDeviceArps(String deviceName, Handler<AsyncResult<List<Arp>>> resultHandler);
	@Fluent
	DigitalTwinService runningGetDeviceAclTables(String deviceName, Handler<AsyncResult<List<AclTable>>> resultHandler);

	@Fluent
	DigitalTwinService runningGetDeviceConfig(String deviceName, Handler<AsyncResult<JsonObject>> resultHandler);

	/* Operations on view network */
	@Fluent	
	DigitalTwinService createView(String viewId, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService createView2(String viewId, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService deleteView(String viewId, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService viewVerify(String viewId, Handler<AsyncResult<VerificationReport>> resultHandler);
	@Fluent
	DigitalTwinService viewGetNetworkConfig(String viewId, Handler<AsyncResult<JsonObject>> resultHandler);

	// view network
	@Fluent	
	DigitalTwinService viewGetNetwork(String viewId, Handler<AsyncResult<Network>> resultHandler);

	// view device
	@Fluent	
	DigitalTwinService viewGetDevice(String viewId, String deviceName, Handler<AsyncResult<Device>> resultHandler);
	@Fluent	
	DigitalTwinService viewUpsertDevice(String viewId, String deviceName, Device device, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService viewDeleteDevice(String viewId, String deviceName, Handler<AsyncResult<Void>> resultHandler);

	// view interface
	@Fluent	
	DigitalTwinService viewGetDeviceInterfaces(String viewId, String deviceName, Handler<AsyncResult<List<NetInterface>>> resultHandler);
	@Fluent	
	DigitalTwinService viewGetInterface(String viewId, String deviceName, String itfName, Handler<AsyncResult<NetInterface>> resultHandler);
	@Fluent	
	DigitalTwinService viewCreateInterface(String viewId, String deviceName, String itfName, NetInterface netItf, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService viewUpdateInterface(String viewId, String deviceName, String itfName, NetInterface netItf, Handler<AsyncResult<Void>> resultHandler);
	@Fluent
	DigitalTwinService viewDeleteInterface(String viewId, String deviceName, String itfName, Handler<AsyncResult<Void>> resultHandler);

	// view link
	@Fluent	
	DigitalTwinService viewCreateLink(String viewId, String linkName, Link link, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService viewDeleteLink(String viewId, String linkName, Handler<AsyncResult<Void>> resultHandler);

	// view bgp
	@Fluent	
	DigitalTwinService viewGetDeviceBgps(String viewId, String deviceName, Handler<AsyncResult<List<Bgp>>> resultHandler);
	@Fluent	
	DigitalTwinService viewGetBgp(String viewId, String deviceName, String itfAddr, Handler<AsyncResult<Bgp>> resultHandler);
	@Fluent	
	DigitalTwinService viewUpsertBgp(String viewId, String deviceName, String itfAddr, Bgp bgp, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService viewDeleteBgp(String viewId, String deviceName, String itfAddr, Handler<AsyncResult<Void>> resultHandler);

	// view vlan
	@Fluent	
	DigitalTwinService viewGetDeviceVlans(String viewId, String deviceName, Handler<AsyncResult<List<Vlan>>> resultHandler);
	@Fluent	
	DigitalTwinService viewCreateVlan(String viewId, String deviceName, String vid, Vlan vlan, Handler<AsyncResult<Void>> resultHandler);
	@Fluent	
	DigitalTwinService viewDeleteVlan(String viewId, String deviceName, String vid, Handler<AsyncResult<Void>> resultHandler);
	
	// view vlan-member
	@Fluent
	DigitalTwinService viewGetVlanMembers(String viewId, String deviceName,String vid,  Handler<AsyncResult<List<VlanMember>>> resultHandler);
	@Fluent
	DigitalTwinService viewAddVlanMember(String viewId, String deviceName, String vid, String itfName, VlanMember vlanMember, Handler<AsyncResult<Void>> resultHandler);
	@Fluent
	DigitalTwinService viewRemoveVlanMember(String viewId, String deviceName, String vid, String itfName, Handler<AsyncResult<Void>> resultHandler);

	@Fluent
	DigitalTwinService viewGetDeviceConfig(String viewId, String deviceName, Handler<AsyncResult<JsonObject>> resultHandler);

	// Path search
	@Fluent	
	DigitalTwinService runningFindPathByHostnames(String from, String to, Handler<AsyncResult<List<Path>>> resultHandler);
	@Fluent	
	DigitalTwinService runningFindPathByIpAddrs(String from, String to, Handler<AsyncResult<List<Path>>> resultHandler);
	@Fluent
	DigitalTwinService runningGetIpRoutesOfPath(List<PathHop> path, Handler<AsyncResult<List<RouteHop>>> resultHandler);
}