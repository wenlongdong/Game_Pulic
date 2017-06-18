package com.hutong.socketbase.load.report;

import java.util.ArrayList;
import java.util.List;

import com.hutong.socketbase.define.SceneGateDefine;


//所有scene的简要信息 1.主要用于 遍历所有的scene     2.gateway建立通往scene的通道
public class SceneBrifeInfo {

	/** scene类型标识 */
	private int sceneType;
	
	/** scene唯一标识 */
	private String sceneServerId;
	
	/** ip地址 */
	private String ip;
	
	/** 端口 */
	private int port;
	
	/** 当前的玩家数量*/
	private int playersNum = 0;
	
	/** 当前scene 支持的线*/
	private List<String> sceneLineIdList = new ArrayList<String>();
	
	/** 上一次刷新的时间*/
	private long refreshTime = 0;
	
	public boolean isTimeOut(){
		long deltaTime = System.currentTimeMillis() - refreshTime;
		if(Math.abs(deltaTime) > SceneGateDefine.INNER_ALL_SCENES_BRIFE_INFOS_TIME_OUT * 1000){
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString(){
		
		return "sceneType is " + sceneType + ", sceneServerId is " + sceneServerId + ", ip is " + ip + ", port is " + port + ", sceneLineIdList is "
				+ sceneLineIdList + ", refreshTime is " + refreshTime;
		
	}

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
	 * @return the playersNum
	 */
	public int getPlayersNum() {
		return playersNum;
	}

	/**
	 * @param playersNum the playersNum to set
	 */
	public void setPlayersNum(int playersNum) {
		this.playersNum = playersNum;
	}

	/**
	 * @return the sceneLineIdList
	 */
	public List<String> getSceneLineIdList() {
		return sceneLineIdList;
	}

	/**
	 * @param sceneLineIdList the sceneLineIdList to set
	 */
	public void setSceneLineIdList(List<String> sceneLineIdList) {
		this.sceneLineIdList = sceneLineIdList;
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
}
