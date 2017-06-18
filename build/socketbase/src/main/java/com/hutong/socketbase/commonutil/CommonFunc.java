/**
 * 
 */
package com.hutong.socketbase.commonutil;

import com.hutong.socketbase.define.PlayerUniqueKey;
import com.hutong.socketbase.define.SceneGateDefine;

/**
 * @author DWL
 *
 */
public class CommonFunc {

	// ////////////////////////////////////////////////////
	public static PlayerUniqueKey getPlayerUniqueKey(int serverId, long playerId) {
		return new PlayerUniqueKey(serverId, playerId);
	}

	public static String getAllSceneBrifeInfo(String nameSpace){
		return nameSpace + ":" + SceneGateDefine.INNER_ALL_SCENES_BRIFE_INFOS;
	}
	
	public static String getSceneTypeRedisName(String nameSpace, int sceneType){
		return nameSpace + ":SCENE_LOAD_INFO:" + sceneType;
	}
	
	
	
	public static String getAllGatewayInfo(String nameSpace){
		return nameSpace + ":" + SceneGateDefine.INNER_ALL_GATES_INFOS;
	}
	
	public static String getGatewayRedisName(String nameSpace, String gatewayId){
		return nameSpace + ":GATEWAY_TO_SCENE_LINE_INFO:" + gatewayId;
	}
	
	public static String generateGate2SceneLineId(String sceneServerId, int lineId){
		return sceneServerId + "->" + lineId;
	}
}
