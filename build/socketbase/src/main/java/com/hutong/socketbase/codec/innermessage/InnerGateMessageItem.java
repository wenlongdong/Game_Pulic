package com.hutong.socketbase.codec.innermessage;

public class InnerGateMessageItem {

	private int code;
	private long playerId;
	private int serverId;
	private int lineId;
	private byte[] bytes;
			
	public InnerGateMessageItem(int code, long playerId, int serverId, int lineId, byte[] bytes) {
		this.code = code;
		this.playerId = playerId;
		this.serverId = serverId;
		this.lineId = lineId;
		this.bytes = bytes;
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
}
