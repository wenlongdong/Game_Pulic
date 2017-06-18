/**
 * 
 */
package com.hutong.scene.server;


import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author DWL
 * 记录一些gateway的信息
 */
public class GatewayInfo {

	private String gatewayId = "";
ConcurrentLinkedDeque
	public GatewayInfo(String gatewayId) {
		this.gatewayId = gatewayId;
	}
	
	/**
	 * @return the gatewayId
	 */
	public String getGatewayId() {
		return gatewayId;
	}


	/**
	 * @param gatewayId the gatewayId to set
	 */
	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	@Override
	public String toString() {
		return "GatewayInfo gatewayId=" + gatewayId;
	}
}
