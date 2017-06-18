package com.hutong.gateway.dispatcher.actions;

import io.netty.channel.Channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hutong.framework.base.dispatch.annocation.Action;
import com.hutong.framework.base.dispatch.annocation.ProtocolPB;
import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.gateway.GatewayConfig;
import com.hutong.gateway.dispatcher.GateCommonFunction;
import com.hutong.gateway.dispatcher.GatewayRequestMsgQueue;
import com.hutong.gateway.gatewayserver.GatewayServerData;
import com.hutong.gateway.redis.InnerRedisService;
import com.hutong.socketbase.codec.innermessage.InnerGateToSceneMessage;
import com.hutong.socketbase.message.GSMessage.GSPong;
import com.hutong.socketbase.message.GSMessage.SGPing;
import com.hutong.socketbase.message.GSMessage.SGPlayerDisconnect;
import com.hutong.socketbase.message.GSMessage.SGPong;
import com.hutong.socketbase.message.GSMessage.SGRegist;
import com.hutong.socketbase.message.GSMessage.SGSceneToSceneMsg;
import com.hutong.socketbase.message.GSMessage.SceneUniqeId;
import com.hutong.socketbase.message.InnerOpDefineDefault;
import com.hutong.socketbase.sessionbase.GatewaySession;
import com.hutong.socketbase.socketactiondata.SocketActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Action
@Service
public class SceneClientAction {

	@Autowired
	private InnerRedisService redisService;
	
	@Autowired
	private GatewayServerData<GatewaySession> gatewayServerData;
	
	@Autowired
	private GatewayConfig gatewayConfig;
	
	@Autowired
	private GateCommonFunction gateCommonFunction;
	
	@Autowired
	private GatewayRequestMsgQueue gatewayRequestMsgQueue;
	
	
	@ProtocolPB(value = InnerOpDefineDefault.GSRegistCode)
	public void gsRegist(SocketActionData socketActionData) throws Exception {
		
		InnerGateToSceneMessage innerGateToSceneMessage = new InnerGateToSceneMessage();
		innerGateToSceneMessage.addInnerGateMessageItem(socketActionData.getCode(), socketActionData.getPlayerId(), socketActionData.getServerId(), 0, socketActionData.getBytes());
		
		socketActionData.getChannel().writeAndFlush(innerGateToSceneMessage);
	}
	
	@ProtocolPB(value = InnerOpDefineDefault.SGRegistCode)
	public void sgRegist(SocketActionData socketActionData) throws Exception {
		
		SGRegist sgRegist = SGRegist.parseFrom(socketActionData.getBytes());
		
		if (InnerOpDefineDefault.SGSUCCESS == sgRegist.getResult()) {
			for(SceneUniqeId sceneUniqeId : sgRegist.getSceneUniqeIdList()){
				gatewayRequestMsgQueue.addGatewayQueue(sceneUniqeId.getSceneId(), socketActionData.getChannel());
			}
		}
	}
	
	
	@ProtocolPB(value = InnerOpDefineDefault.SGPlayerDisconnect)
	public void sgPlayerDisconnect(SocketActionData socketActionData) throws Exception {
		
		SGPlayerDisconnect sgPlayerDisconnect = SGPlayerDisconnect.parseFrom(socketActionData.getBytes());
		
		if (InnerOpDefineDefault.SGSUCCESS == sgPlayerDisconnect.getResult()) {
			PubQueueLogUtil.logInfo("playerDisconnect success! and playerId is " + sgPlayerDisconnect.getPlayerId() +
					" playerServerId is " + sgPlayerDisconnect.getServerId());
		}
	}
	
	//scene发送消息主动断开gateway方玩家的连接
	@ProtocolPB(value = InnerOpDefineDefault.SGDisconnectPlayer)
	public void sgDisconnectPlayer(SocketActionData socketActionData) throws Exception {
		
//		SGDisconnectPlayer sgDisconnectPlayer = SGDisconnectPlayer.parseFrom(socketActionData.getBytes());
		long playerId = socketActionData.getPlayerId();
		int serverId = socketActionData.getServerId();
		
		Channel clientChannel = gatewayServerData.getClientChannelBy(serverId, playerId);
		if(clientChannel != null){
			clientChannel.flush().close();
		}
	}
	
	
	@ProtocolPB(value = InnerOpDefineDefault.SGSceneToSceneMsg)
	public void sgSceneToSceneMsg(SocketActionData socketActionData) throws Exception {
		
		long toPlayerId = socketActionData.getPlayerId();
		int toServerId = socketActionData.getServerId();
		
		SGSceneToSceneMsg sgSceneToSceneMsg = SGSceneToSceneMsg.parseFrom(socketActionData.getBytes());
		
		int opCode = sgSceneToSceneMsg.getOpCode();
		byte[] bytes = sgSceneToSceneMsg.getMsgBytes().toByteArray();
		
		SocketActionData sendActionData = new SocketActionData(toPlayerId, toServerId, socketActionData.getChannel(), opCode, bytes);
		
		gateCommonFunction.write2Scene(sendActionData);
	}
	
	
	@ProtocolPB(value = InnerOpDefineDefault.SGPingCode)
	public void sgPingMsg(SocketActionData data) throws Exception {
		
		SGPing sgPing = SGPing.parseFrom(data.getBytes());
		PubQueueLogUtil.logDebug("SGPing MSG : sceneServerId is " + sgPing.getSceneUniqeId().getSceneId() + " and sceneTypeIs is " + sgPing.getSceneUniqeId().getSceneType());
		
		GSPong.Builder gsPong = GSPong.newBuilder();
		gsPong.setPongNum(InnerOpDefineDefault.SGSUCCESS);
		gsPong.setGatewayId(gatewayConfig.getGatewayId());
		
		InnerGateToSceneMessage innerGateToSceneMessage = new InnerGateToSceneMessage();
		innerGateToSceneMessage.addInnerGateMessageItem(InnerOpDefineDefault.GSPongCode, 0L, 0, 0, gsPong.build().toByteArray());
		
		data.getChannel().writeAndFlush(innerGateToSceneMessage);
	}

	
	@ProtocolPB(value = InnerOpDefineDefault.SGPongCode)
	public void sgPongMsg(SocketActionData data) throws Exception {
		
		SGPong sgPong = SGPong.parseFrom(data.getBytes());
		PubQueueLogUtil.logDebug("SGPong MSG : sceneServerId is " + sgPong.getSceneUniqeId().getSceneId() + " and sceneTypeIs is " + sgPong.getSceneUniqeId().getSceneType());
	}
}
