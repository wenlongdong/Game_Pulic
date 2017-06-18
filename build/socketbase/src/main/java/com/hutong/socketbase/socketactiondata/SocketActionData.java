/**
 * 
 */
package com.hutong.socketbase.socketactiondata;

import java.util.Arrays;

import io.netty.channel.Channel;

/**
 * @author DWL
 *
 */
public class SocketActionData {

	private long playerId;
	
	private int serverId;
	
	private Channel channel;
	
	private int code;
	
	private byte[] bytes;

	public SocketActionData(long playerId, int serverId, Channel channel, int code, byte[] bytes) {

		this.playerId = playerId;
		this.serverId = serverId;
		this.channel = channel;
		this.code = code;
		this.bytes = bytes;
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

	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SocketActionData [playerId=" + playerId + ", serverId="
				+ serverId + ", channel=" + channel + ", code=" + code
				+ ", bytes=" + Arrays.toString(bytes) + "]";
	}
}
