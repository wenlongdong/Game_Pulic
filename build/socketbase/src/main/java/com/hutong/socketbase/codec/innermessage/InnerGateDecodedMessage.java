package com.hutong.socketbase.codec.innermessage;

import java.util.ArrayList;
import java.util.List;

public class InnerGateDecodedMessage {

	private List<FromSceneMessageItem> fromSceneMessageList = new ArrayList<InnerGateDecodedMessage.FromSceneMessageItem>();
	
	public void addFromSceneMessageItem(int code, long playerId, int serverId, byte[] bytes){
		fromSceneMessageList.add(new FromSceneMessageItem(code, playerId, serverId, bytes));
	}
	
	
	public static class FromSceneMessageItem{
		
		private int code = 0;
		private long playerId = 0;
		private int serverId = 0;
		
		private byte[] bytes = new byte[0];
		
		public FromSceneMessageItem(int code, long playerId, int serverId, byte[] bytes){
			this.code = code;
			this.playerId = playerId;
			this.serverId = serverId;
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

	public List<FromSceneMessageItem> getFromSceneMessageList() {
		return fromSceneMessageList;
	}

	public void setFromSceneMessageList(List<FromSceneMessageItem> fromSceneMessageList) {
		this.fromSceneMessageList = fromSceneMessageList;
	}
}
