package com.hutong.socketbase.codec.innermessage;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Amyn
 * @description ?
 * 
 */
public class InnerGateToSceneMessage {
	
	private List<InnerGateMessageItem> innerGateMessageItemList = new ArrayList<InnerGateMessageItem>();
	
	public void addInnerGateMessageItem(int code, long playerId, int serverId, int lineId, byte[] bytes){
		innerGateMessageItemList.add(new InnerGateMessageItem(code, playerId, serverId, lineId, bytes));
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer(256);
		
		for(InnerGateMessageItem innerGateMessageItem : innerGateMessageItemList){
			
			sb.append("opCode : " + innerGateMessageItem.getCode());
			sb.append("; playerId : " + innerGateMessageItem.getPlayerId());
			sb.append("; serverId : " + innerGateMessageItem.getServerId());
			sb.append("; lineId : " + innerGateMessageItem.getLineId());
		}
		
		return sb.toString();
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
