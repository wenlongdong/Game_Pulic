/**
 * 
 */
package com.hutong.socketbase.codec.clientmessage;

import io.netty.channel.Channel;

/**
 * @author DWL
 *
 */
public abstract class ClientRequestMessageIf {

	private int code;
	
	private long playerId;
	
	private int serverId;
	
	private byte[] bytes;

	private Channel clientChannel;
	
	public ClientRequestMessageIf(int serverId, long playerId, int code, byte[] bytes, Channel clientChannel){
		this.serverId = serverId;
		this.playerId = playerId;
		this.code = code;
		this.bytes = bytes;
		this.setClientChannel(clientChannel);
	}
	
	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
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
	 * @return the bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * @param bytes the bytes to set
	 */
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public Channel getClientChannel() {
		return clientChannel;
	}

	public void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
	}
}
