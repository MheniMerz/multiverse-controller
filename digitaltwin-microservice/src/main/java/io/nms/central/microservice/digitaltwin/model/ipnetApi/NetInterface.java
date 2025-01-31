package io.nms.central.microservice.digitaltwin.model.ipnetApi;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.nms.central.microservice.common.functional.JsonUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class NetInterface extends Configurable {
	
	public enum InterfaceType {
		Bridge("Bridge"),
		Vlan("Vlan"),
		Other("Other");
		private String value;
		private InterfaceType(String value) { this.value = value; }
		public String getValue() { return this.value; }
	};
	
	public enum InterfaceStatus {
		up("up"),
		down("down"),
		undefined("undefined");
		private String value;
		private InterfaceStatus(String value) { this.value = value; }
		public String getValue() { return this.value; }
	};
	
	public enum VlanMode {
		tagged("tagged"),
		untagged("untagged"),
		undefined("undefined");
		private String value;
		private VlanMode(String value) { this.value = value; }
		public String getValue() { return this.value; }
	};

	// In LTP
	private String name;
	private InterfaceType type;
	private InterfaceStatus adminStatus;
	private String index;
	private String speed;
	private String mtu;
	
	// In EtherCtp
	private String macAddr;
	private String vlan;
	private VlanMode mode;
	
	// In Ip4Ctp
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String ipAddr; 		// CIDR
	
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String svi;

	public NetInterface() {}
	public NetInterface(JsonObject json) {
		JsonUtils.fromJson(json, this, NetInterface.class);
	}

	public VlanMode getMode() {
		return mode;
	}
	public void setMode(VlanMode mode) {
		this.mode = mode;
	}
	public String getSvi() {
		return svi;
	}
	public void setSvi(String svi) {
		this.svi = svi;
	}
	public String getVlan() {
		return vlan;
	}
	public void setVlan(String vlan) {
		this.vlan = vlan;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public InterfaceStatus getAdminStatus() {
		return adminStatus;
	}
	public void setAdminStatus(InterfaceStatus adminStatus) {
		this.adminStatus = adminStatus;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getMtu() {
		return mtu;
	}
	public void setMtu(String mtu) {
		this.mtu = mtu;
	}
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public String getMacAddr() {
		return macAddr;
	}
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	public String getIpAddr() {
		return ipAddr;
	}
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	public InterfaceType getType() {
		return type;
	}
	public void setType(InterfaceType type) {
		this.type = type;
	}

	/*-----------------------------------------------*/
	public JsonObject toJson() {
		return new JsonObject(JsonUtils.pojo2Json(this, false));
	}
	@Override
	public String toString() {
		return JsonUtils.pojo2Json(this, false);
	}
	@Override
	public boolean equals(Object obj) {
		return Objects.equals(toString(), ((NetInterface) obj).toString());
	}
	@Override
	public int hashCode() {
		return Objects.hash(name+macAddr);
	}
}

