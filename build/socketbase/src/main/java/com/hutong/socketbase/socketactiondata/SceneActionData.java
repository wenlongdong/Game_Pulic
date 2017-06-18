package com.hutong.socketbase.socketactiondata;

import io.netty.channel.Channel;

public class SceneActionData extends SocketActionData {

	private int lineId = 0;
	
	public SceneActionData(int lineId, long playerId, int serverId, Channel channel, int code, byte[] bytes){
		super(playerId, serverId, channel, code, bytes);
		this.lineId = lineId;
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
