package com.hutong.socketbase.load.report;

import com.hutong.socketbase.define.SceneGateDefine;



/**
 * @author Amyn
 * @description ?
 * 
 */
public class GatewayInfo {

	/** gateway唯一标识 */
	private String gatewayId;
	/** ip地址 */
	private String ip;
	/** 端口 */
	private int port;
	/** 负载数据刷新时间 */
	private long refreshTime;
	/** IO线程数量*/
	private int ioThreadsNum = 4;
	/** 当前的渠道数量*/
	private int channelsNum = 0;


	public boolean isTimeOut(){
		long deltaTime = System.currentTimeMillis() - refreshTime;
		if(deltaTime > SceneGateDefine.INNER_ALL_GATES_INFOS_TIME_OUT * 1000){
			return true;
		} else {
			return false;
		}
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
	 * @return the refreshTime
	 */
	public long getRefreshTime() {
		return refreshTime;
	}


	/**
	 * @param refreshTime the refreshTime to set
	 */
	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}


	/**
	 * @return the ioThreadsNum
	 */
	public int getIoThreadsNum() {
		return ioThreadsNum;
	}


	/**
	 * @param ioThreadsNum the ioThreadsNum to set
	 */
	public void setIoThreadsNum(int ioThreadsNum) {
		this.ioThreadsNum = ioThreadsNum;
	}

	/**
	 * @return the channelsNum
	 */
	public int getChannelsNum() {
		return channelsNum;
	}


	/**
	 * @param channelsNum the channelsNum to set
	 */
	public void setChannelsNum(int channelsNum) {
		this.channelsNum = channelsNum;
	}
}
