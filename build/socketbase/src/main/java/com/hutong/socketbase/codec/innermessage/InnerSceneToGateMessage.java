package com.hutong.socketbase.codec.innermessage;

import java.util.ArrayList;
import java.util.List;

import com.hutong.socketbase.socketactiondata.SceneActionData;


/**
 * @author Amyn
 * @description ?
 * 
 */
public class InnerSceneToGateMessage {

	private List<SceneActionData> sceneActionDataList = new ArrayList<SceneActionData>();
	
	public void addSceneActionData(SceneActionData sceneActionData){
		
		sceneActionDataList.add(sceneActionData);
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer(256);
		
		for(SceneActionData sceneActionData : sceneActionDataList){
			
			sb.append("opCode : " + sceneActionData.getCode());
			sb.append("; playerId : " + sceneActionData.getPlayerId());
			sb.append("; serverId : " + sceneActionData.getServerId());
			sb.append("; lineId : " + sceneActionData.getLineId());
		}
		
		return sb.toString();
	}
	
	/**
	 * @return the sceneActionDataList
	 */
	public List<SceneActionData> getSceneActionDataList() {
		return sceneActionDataList;
	}

	/**
	 * @param sceneActionDataList the sceneActionDataList to set
	 */
	public void setSceneActionDataList(List<SceneActionData> sceneActionDataList) {
		this.sceneActionDataList = sceneActionDataList;
	}
}
