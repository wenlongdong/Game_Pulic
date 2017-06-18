package com.hutong.socketbase.codec.innermessage;

import java.util.ArrayList;
import java.util.List;

public class InnerSceneDecodedMessage {
	
	private List<InnerGateMessageItem> innerGateMessageItemList = new ArrayList<InnerGateMessageItem>();
	
	public void addInnerGateMessageItem(int code, long playerId, int serverId, int lineId, byte[] bytes){
		innerGateMessageItemList.add(new InnerGateMessageItem(code, playerId, serverId, lineId, bytes));
	}

	/**
	 * @return the innerGateMessageItemList
	 */
	public List<InnerGateMessageItem> getInnerGateMessageItemList() {
		return innerGateMessageItemList;
	}

	/**
	 * @param innerGateMessageItemList the innerGateMessageItemList to set
	 */
	public void setInnerGateMessageItemList(
			List<InnerGateMessageItem> innerGateMessageItemList) {
		this.innerGateMessageItemList = innerGateMessageItemList;
	}
}
