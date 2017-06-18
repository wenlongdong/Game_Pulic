package com.hutong.socketbase.load.report;


//每一条线的信息
public class SceneLineInfo {
	
	private String sceneServerId = "";
	
	/** scene的分线Id*/
	private int sceneLineId = 0;
	
	/** 当前的玩家数量*/
	private int playersNum = 0;
	

	/**
	 * @return the sceneLineId
	 */
	public int getSceneLineId() {
		return sceneLineId;
	}

	/**
	 * @param sceneLineId the sceneLineId to set
	 */
	public void setSceneLineId(int sceneLineId) {
		this.sceneLineId = sceneLineId;
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

	@Override
	public String toString() {
		return "SceneLineInfo [sceneServerId=" + sceneServerId + ", sceneLineId="
				+ sceneLineId + ", playersNum=" + playersNum + "]";
	}

	public String getSceneServerId() {
		return sceneServerId;
	}

	public void setSceneServerId(String sceneServerId) {
		this.sceneServerId = sceneServerId;
	}
}
