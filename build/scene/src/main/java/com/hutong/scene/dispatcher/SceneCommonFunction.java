package com.hutong.scene.dispatcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;
import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.server.SceneServerData;
import com.hutong.socketbase.message.GSMessage.SGSceneToSceneMsg;
import com.hutong.socketbase.message.InnerOpDefineDefault;
import com.hutong.socketbase.sessionbase.SceneSession;
import com.hutong.socketbase.socketactiondata.SceneActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Service
public class SceneCommonFunction {

	@Autowired
	private SceneServerData<SceneSession> sceneServerData;

	@Autowired
	private SceneResponseMsgQueue sceneResponseMsgQueue;
	
	public void write2Gate(SceneActionData sceneActionData) {
		
		SceneSession sceneSession = sceneServerData.getSceneSession(sceneActionData.getServerId(), sceneActionData.getPlayerId());
		if(sceneSession != null && !sceneSession.getGatewayId().isEmpty()){
			sceneResponseMsgQueue.addSceneActionData(sceneSession.getGatewayId(), sceneActionData);
		} else {
			PubQueueLogUtil.logWarn("write2Gate could not find sceneSession " + sceneActionData.getServerId() + "-" + sceneActionData.getPlayerId() + " pbcode: " + 
					sceneActionData.getCode());
		}
	}
	
	//scene发送消息主动断开gateway方玩家的连接  
	public void disconnectPlayer(int serverId, long playerId, String gatewayId){
		sendSceneToPlayerMsg(serverId, playerId, gatewayId, InnerOpDefineDefault.SGDisconnectPlayer, null);
	}
	
	//场景之间互相发送消息
	public void sendSceneToSceneMsg(int toServerId, long toPlayerId, String gatewayId, int opCode, byte[] bytes){
		
		if(bytes == null){
			bytes = new byte[0];
		}
		
		SGSceneToSceneMsg.Builder sgSceneToSceneMsg = SGSceneToSceneMsg.newBuilder();
		sgSceneToSceneMsg.setToPlayerId(toPlayerId);
		sgSceneToSceneMsg.setToServerId(toServerId);
		sgSceneToSceneMsg.setOpCode(opCode);
		sgSceneToSceneMsg.setMsgBytes(ByteString.copyFrom(bytes));
		
		SceneActionData sceneActionData = new SceneActionData(-1, toPlayerId, toServerId, null, opCode, bytes);
		sceneResponseMsgQueue.addSceneActionData(gatewayId, sceneActionData);
	}
	
	//scene主动向某一个gateway的玩家发送消息
	public void sendSceneToPlayerMsg(int toServerId, long toPlyaerId, String gatewayId, int opCode, byte[] bytes){
		
		SceneActionData sceneActionData = new SceneActionData(-1, toPlyaerId, toServerId, null, opCode, bytes);
		sceneResponseMsgQueue.addSceneActionData(gatewayId, sceneActionData);
	}
	
	//向gateway广播消息  如某个玩家离开了场景，向其他gateway去通知，gateway收到通知后，可以选择正在排队的玩家进入
	//注意  这个方法需要gateway那边对应实现一个响应的函数来处理
	public void broadcastToGate(int opCode, byte[] bytes){
		
		SceneActionData sceneActionData = new SceneActionData(-1, 0, 0, null, opCode, bytes);
		
		for(String gatewayId : sceneResponseMsgQueue.getAllGatewayIds()){
			sceneResponseMsgQueue.addSceneActionData(gatewayId, sceneActionData);
		}
	}
	
	//随机向一个gateway发送消息   这个应用主要是用来向某一个gateway发送请求，获取结果而已  而并不关心具体是发送到哪一个gateway上
	public boolean randomToGate(int opCode, byte[] bytes){
		
		SceneActionData sceneActionData = new SceneActionData(-1, 0, 0, null, opCode, bytes);
		
		boolean isSend = false;
		for(String gatewayId : sceneResponseMsgQueue.getAllGatewayIds()){
			sceneResponseMsgQueue.addSceneActionData(gatewayId, sceneActionData);
			isSend = true;
			break;
		}
		
		return isSend;
	}
}
