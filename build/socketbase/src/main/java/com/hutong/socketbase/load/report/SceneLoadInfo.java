package com.hutong.socketbase.load.report;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class SceneLoadInfo {

	/** scene类型标识 */
	private int sceneType;
	/** scene唯一标识 */
	private String sceneServerId;
	/** ip地址 */
	private String ip;
	/** 端口 */
	private int port;
	/** 总人数*/
	private int totalPlayerNum;
	/** 负载数据刷新时间 */
	private long refreshTime;
	/** IO线程数量*/
	private int ioThreadsNum = 4;
	/** Work线程数量*/
	private int workThreadsNum = 4;
	/** 当前scene中每个线的详细信息*/
	private Map<Integer, SceneLineInfo> lineMapSceneLineInfo = new HashMap<>();
	
	
	/**
	 * @return the sceneType
	 */
	public int getSceneType() {
		return sceneType;
	}

	/**
	 * @param sceneType the sceneType to set
	 */
	public void setSceneType(int sceneType) {
		this.sceneType = sceneType;
	}

	/**
	 * @return the sceneServerId
	 */
	public String getSceneServerId() {
		return sceneServerId;
	}

	/**
	 * @param sceneServerId the sceneServerId to set
	 */
	public void setSceneServerId(String sceneServerId) {
		this.sceneServerId = sceneServerId;
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
	 * @return the workThreadsNum
	 */
	public int getWorkThreadsNum() {
		return workThreadsNum;
	}

	/**
	 * @param workThreadsNum the workThreadsNum to set
	 */
	public void setWorkThreadsNum(int workThreadsNum) {
		this.workThreadsNum = workThreadsNum;
	}

	/**
	 * @return the lineMapSceneLineInfo
	 */
	public Map<Integer, SceneLineInfo> getLineMapSceneLineInfo() {
		return lineMapSceneLineInfo;
	}

	/**
	 * @param lineMapSceneLineInfo the lineMapSceneLineInfo to set
	 */
	public void setLineMapSceneLineInfo(
			Map<Integer, SceneLineInfo> lineMapSceneLineInfo) {
		this.lineMapSceneLineInfo = lineMapSceneLineInfo;
	}

	public int getTotalPlayerNum() {
		return totalPlayerNum;
	}

	public void setTotalPlayerNum(int totalPlayerNum) {
		this.totalPlayerNum = totalPlayerNum;
	}
}
