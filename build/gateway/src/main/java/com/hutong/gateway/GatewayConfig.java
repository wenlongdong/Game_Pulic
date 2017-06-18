package com.hutong.gateway;

import org.springframework.stereotype.Component;

@Component
public class GatewayConfig {
	
	// gateway唯一标识
	private String gatewayId = "";
	
	// gateway的名字空间
	private String namespace="";
	
	// 本地IP
	private String ip = "";
	
	// 端口
	private int port = -1;
	
	// gateway 框架用work线程池线程数
	private int workGroupThreadNum = -1;
	
	
	public void init(GatewayConfig gatewayConfig) throws Exception {
		
		if(gatewayConfig.getGatewayId() == null || gatewayConfig.getGatewayId().isEmpty() 
				|| gatewayConfig.getNamespace() == null || gatewayConfig.getNamespace().isEmpty() 
				|| gatewayConfig.getIp() == null || gatewayConfig.getIp().isEmpty()
				|| gatewayConfig.getPort() == -1 || gatewayConfig.getWorkGroupThreadNum() == -1){
			throw new Exception("GatewayConfig wrong! GatewayConfig is : " + gatewayConfig.toString());
		}
		
		this.gatewayId = gatewayConfig.getGatewayId();
		this.namespace = gatewayConfig.getNamespace();
		this.ip = gatewayConfig.getIp();
		this.port = gatewayConfig.getPort();
		this.workGroupThreadNum = gatewayConfig.getWorkGroupThreadNum();
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("gatewayId:" + gatewayId + "; namespace:" + namespace + "; ip:" + ip + "; port:" + port + "; workGroupThreadNum:" + workGroupThreadNum);
		return sb.toString();
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


	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}


	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}


	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}


	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}


	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}


	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}


	/**
	 * @return the workGroupThreadNum
	 */
	public int getWorkGroupThreadNum() {
		return workGroupThreadNum;
	}


	/**
	 * @param workGroupThreadNum the workGroupThreadNum to set
	 */
	public void setWorkGroupThreadNum(int workGroupThreadNum) {
		this.workGroupThreadNum = workGroupThreadNum;
	}
}
