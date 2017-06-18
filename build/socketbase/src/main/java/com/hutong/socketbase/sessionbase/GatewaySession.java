package com.hutong.socketbase.sessionbase;

import io.netty.channel.Channel;

import java.io.Serializable;

/**
 * @author Amyn
 * @description ?
 * 
 */
public abstract class GatewaySession implements Serializable {


	private static final long serialVersionUID = 1L;

	private int serverId;
	
	private long playerId;

	private Channel clientChannel;//与客户端连接的channel  一般不会为null
	
	private String sceneServerId = "";//与scene端绑定的sceneServerId
	
	private int lineId = 0;//选择的线Id
	
	public GatewaySession(int serverId, long playerId, Channel clientChannel, String sceneServerId, int lineId) {

		this.serverId = serverId;
		this.playerId = playerId;
		this.clientChannel = clientChannel;
		this.sceneServerId = sceneServerId;
		this.lineId = lineId;
	}

	@Override
	public String toString(){
		return "serverId:" + serverId + "; playerId:" + playerId + ";clientChannel:" + clientChannel + ";sceneServerId:" + sceneServerId + ";lineId:" + lineId;
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

	/**
	 * @return the clientChannel
	 */
	public Channel getClientChannel() {
		return clientChannel;
	}

	/**
	 * @param clientChannel the clientChannel to set
	 */
	public void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
	}

	public String getSceneServerId() {
		return sceneServerId;
	}

	public void setSceneServerId(String sceneServerId) {
		this.sceneServerId = sceneServerId;
	}

	public int getLineId() {
		return lineId;
	}

	public void setLineId(int lineId) {
		this.lineId = lineId;
	}


}
