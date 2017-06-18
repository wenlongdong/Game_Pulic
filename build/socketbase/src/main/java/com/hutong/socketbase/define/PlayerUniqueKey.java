package com.hutong.socketbase.define;

import java.io.Serializable;

/**
 * 玩家唯一标识
 * @author Administrator
 *
 */
public class PlayerUniqueKey implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1530668262488698439L;

	/**
	 * 服务器id
	 */
	private int serverId;
	
	/**
	 * 玩家id
	 */
	private long playerId;
	
	/**
	 * 一般仅用于redis对象存储使用
	 */
	public PlayerUniqueKey() {
	}

	public PlayerUniqueKey(int serverId, long playerId) {
		this.serverId = serverId;
		this.playerId = playerId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		result = prime * result + serverId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerUniqueKey other = (PlayerUniqueKey) obj;
		if (playerId != other.playerId)
			return false;
		if (serverId != other.serverId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlayerUniqueKey [serverId=" + serverId + ", playerId="
				+ playerId + "]";
	}
}
