package com.hutong.gateway.gatewayserver;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.socketbase.commonutil.CommonFunc;
import com.hutong.socketbase.define.PlayerUniqueKey;
import com.hutong.socketbase.sessionbase.GatewaySession;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Component
public class GatewayServerData <T extends GatewaySession> {
	
	private AttributeKey<PlayerUniqueKey> PLAYER_UNIQUE_KEY = AttributeKey.valueOf("PLAYER_UNIQUE_KEY");
	
	//playerUniqueKey -> GatewaySession
	private final ConcurrentMap<PlayerUniqueKey, T> playerUniqueKeyMapSession = new ConcurrentHashMap<PlayerUniqueKey, T>();
	
	public Channel getClientChannelBy(int serverId, long playerId) {
		
		T gatewaySession = playerUniqueKeyMapSession.get(CommonFunc.getPlayerUniqueKey(serverId, playerId));
		if(null != gatewaySession){
			return gatewaySession.getClientChannel();
		}
		return null;
	}
	
	public T getGatewaySessionBy(int serverId, long playerId) {
		T gatewaySession = getGatewaySessionBy(CommonFunc.getPlayerUniqueKey(serverId, playerId));
		return gatewaySession;
	}
	
	public T getGatewaySessionBy(PlayerUniqueKey playerUniqueKey) {
		if(null == playerUniqueKey){
			return null;
		}
		T gatewaySession = playerUniqueKeyMapSession.get(playerUniqueKey);
		return gatewaySession;
	}
	
	public List<T> getAllGatewaySessions(){
		List<T> tList = new ArrayList<T>();
		for(T t : playerUniqueKeyMapSession.values()){
			tList.add(t);
		}
		
		return tList;
	}

	public boolean addGatewaySession(T gatewaySession) {
		
		PubQueueLogUtil.logWarn("addGatewaySession lineId" + gatewaySession.getLineId() + ", serverId " 
				+ gatewaySession.getServerId() + ", playerId " + gatewaySession.getPlayerId());
		
		PlayerUniqueKey playerUniqueKey = CommonFunc.getPlayerUniqueKey(gatewaySession.getServerId(), gatewaySession.getPlayerId());
		
		gatewaySession.getClientChannel().attr(PLAYER_UNIQUE_KEY).set(playerUniqueKey);
		playerUniqueKeyMapSession.put(playerUniqueKey, gatewaySession);
		return true;
	}
	
	public PlayerUniqueKey getPlayerUniqueKeyByChannel(Channel channel){
		return channel.attr(PLAYER_UNIQUE_KEY).get();
	}

	public int getChannelNums() {		
		return playerUniqueKeyMapSession.size();
	}

	public GatewaySession removeGatewaySession(PlayerUniqueKey playerUniqueKey) {
		
		if(null != playerUniqueKey){
			T gatewaySession = playerUniqueKeyMapSession.remove(playerUniqueKey);
			if(gatewaySession != null) {
				gatewaySession.getClientChannel().attr(PLAYER_UNIQUE_KEY).remove();
				PubQueueLogUtil.logWarn("removeSessionByChannel lineId" + gatewaySession.getLineId() + ", serverId " 
					+ gatewaySession.getServerId() + ", playerId " + gatewaySession.getPlayerId());
			}
			
			return gatewaySession;
		}
		
		return null;
	}
}
