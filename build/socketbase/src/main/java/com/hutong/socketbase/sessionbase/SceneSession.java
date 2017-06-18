package com.hutong.socketbase.sessionbase;

import java.io.Serializable;

/**
 * @author Amyn
 * @description ?
 * 
 */
public abstract class SceneSession implements Serializable {


	private static final long serialVersionUID = 1L;

	private int serverId;
	
	private long playerId;

	private String gatewayId;
	
	private int lineId;
	
	public SceneSession(int serverId, long playerId, String gatewayId, int lineId) {

		this.serverId = serverId;
		this.playerId = playerId;
		this.gatewayId = gatewayId;
		this.lineId = lineId;
	}


	/**
	 * @return the serverId
	 */
	public int getServerId() {
		return serverId;
	}


	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}


	/**
	 * @return the playerId
	 */
	public long getPlayerId() {
		return playerId;
	}


	/**
	 * @param playerId the playerId to set
	 */
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}


	public String getGatewayId() {
		return gatewayId;
	}


	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}


	/**
	 * @return the lineId
	 */
	public int getLineId() {
		return lineId;
	}


	/**
	 * @param lineId the lineId to set
	 */
	public void setLineId(int lineId) {
		this.lineId = lineId;
	}
}
