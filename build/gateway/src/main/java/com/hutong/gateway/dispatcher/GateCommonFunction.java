package com.hutong.gateway.dispatcher;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.gateway.GatewayConfig;
import com.hutong.gateway.gatewayserver.GatewayServerData;
import com.hutong.gateway.interf.GatewayIntercept;
import com.hutong.gateway.loadblance.SceneLoadBlance;
import com.hutong.socketbase.codec.clientmessage.ClientRequestMessageIf;
import com.hutong.socketbase.codec.innermessage.InnerGateMessageItem;
import com.hutong.socketbase.define.PlayerUniqueKey;
import com.hutong.socketbase.message.InnerOpDefineDefault;
import com.hutong.socketbase.message.GSMessage.GSPlayerDisconnect;
import com.hutong.socketbase.sessionbase.GatewaySession;
import com.hutong.socketbase.socketactiondata.SocketActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Service
public class GateCommonFunction {

	@Autowired
	private GatewayServerData<GatewaySession> gatewayServerData;
	
	@Autowired
	private GatewayRequestMsgQueue gatewayRequestMsgQueue;
	
	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("gatewayInterceptImpl")
	private GatewayIntercept<ClientRequestMessageIf> gatewayIntercept;
	
	@Autowired
	private GatewayConfig gatewayConfig;
	
	@Autowired
	private SceneLoadBlance sceneLoadBlance;
	
	
	
	
	public void write2Scene(SocketActionData socketActionData) throws Exception {
		
		GatewaySession gatewaySession = gatewayServerData.getGatewaySessionBy(socketActionData.getServerId(), socketActionData.getPlayerId());
		if(gatewaySession != null && StringUtils.isNotEmpty(gatewaySession.getSceneServerId())){//说明玩家的sceneChannel不为空，可以向响应的scene发送数据
				
			InnerGateMessageItem innerGateMessageItem = new InnerGateMessageItem(socketActionData.getCode(), socketActionData.getPlayerId(),
					socketActionData.getServerId(), gatewaySession.getLineId(), socketActionData.getBytes());
			
			gatewayRequestMsgQueue.addGatewayMessageData(gatewaySession.getSceneServerId(), innerGateMessageItem);
		} else {
			
			String sceneServerId = "";
			if(gatewaySession != null){
				sceneServerId = gatewaySession.getSceneServerId();
			}
			PubQueueLogUtil.logWarn(" could not find scenechannel, sceneServerId is " + sceneServerId + " opCode is " + socketActionData.getCode() 
					+ "; Player is " + socketActionData.getServerId() + "-" + socketActionData.getPlayerId());
		}
	}
	
	//不需要玩家的session  直接根据参数发往特定的scene
	public void write2Scene(String sceneServerId, int lineId, int serverId, long playerId, int code, byte[] bytes){
		
		InnerGateMessageItem innerGateMessageItem = new InnerGateMessageItem(code, playerId, serverId, lineId, bytes);
		gatewayRequestMsgQueue.addGatewayMessageData(sceneServerId, innerGateMessageItem);
	}
	
	
	//释放gatewaysession
	public GatewaySession freeGatewaySession(PlayerUniqueKey playerUniqueKey){
		
		GatewaySession gatewaySession = null;
		PubQueueLogUtil.logWarn("channelInactive removeSessionByChannel");
		try{
			gatewaySession = gatewayServerData.removeGatewaySession(playerUniqueKey);
		} catch (Exception e) {
			PubQueueLogUtil.logError("channelInactive removeSessionByChannel throw exception", e);
		}
		
		if(gatewaySession == null){
			PubQueueLogUtil.logWarn("channelInactive gatewaySession is null");
		} else {
		
			try {
				sceneLoadBlance.incLinePlayerNum(gatewaySession.getSceneServerId(), gatewaySession.getLineId(), -1);
				PubQueueLogUtil.logWarn("channelInactive incLinePlayerNum");
			} catch (Exception e) {
				PubQueueLogUtil.logError("channelInactive incLinePlayerNum throw exception", e);
			}
			
			PubQueueLogUtil.logWarn("channelInactive write2Scene");
			try {
				GSPlayerDisconnect.Builder gsPlayerDisconnect = GSPlayerDisconnect.newBuilder();
				gsPlayerDisconnect.setPlayerId(gatewaySession.getPlayerId());
				gsPlayerDisconnect.setServerId(gatewaySession.getServerId());
				gsPlayerDisconnect.setGatewayId(gatewayConfig.getGatewayId());
	
				write2Scene(gatewaySession.getSceneServerId(), gatewaySession.getLineId(), gatewaySession.getServerId(),
						gatewaySession.getPlayerId(), InnerOpDefineDefault.GSPlayerDisconnect, gsPlayerDisconnect.build().toByteArray());
			} catch (Exception e) {
				PubQueueLogUtil.logError("channelInactive write2Scene throw exception", e);
			}
			
			PubQueueLogUtil.logWarn("channelInactive onPlayerDisconnect");
			try{
				if(gatewayIntercept != null){
					gatewayIntercept.onPlayerDisconnect(gatewaySession.getServerId(), gatewaySession.getPlayerId());
				}
			} catch (Exception e) {
				PubQueueLogUtil.logError("channelInactive onPlayerDisconnect throw exception", e);
			}
		}
		
		return gatewaySession;
	}
}
