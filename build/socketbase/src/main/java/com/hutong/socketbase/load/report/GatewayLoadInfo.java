package com.hutong.socketbase.load.report;

import com.hutong.socketbase.define.SceneGateDefine;

public class GatewayLoadInfo {

	private String sceneServerIdWithLineId = "";
	
	private int playerNum = 0;

	private long refreshTime = System.currentTimeMillis();
	
	public GatewayLoadInfo() {
		this.sceneServerIdWithLineId = "";
	}
	
	public GatewayLoadInfo(String sceneServerIdWithLineId) {
		this.sceneServerIdWithLineId = sceneServerIdWithLineId;
	}

	
	public boolean isTimeOut(){
		long deltaTime = System.currentTimeMillis() - refreshTime;
		if(Math.abs(deltaTime) > SceneGateDefine.INNER_ALL_GATES_LOADINFO_TIME_OUT * 1000){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return the sceneServerIdWithLineId
	 */
	public String getSceneServerIdWithLineId() {
		return sceneServerIdWithLineId;
	}

	/**
	 * @param sceneServerIdWithLineId the sceneServerIdWithLineId to set
	 */
	public void setSceneServerIdWithLineId(String sceneServerIdWithLineId) {
		this.sceneServerIdWithLineId = sceneServerIdWithLineId;
	}

	/**
	 * @return the playerNum
	 */
	public int getPlayerNum() {
		return playerNum;
	}

	/**
	 * @param playerNum the playerNum to set
	 */
	public void setPlayerNum(int playerNum) {
		this.playerNum = playerNum;
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
