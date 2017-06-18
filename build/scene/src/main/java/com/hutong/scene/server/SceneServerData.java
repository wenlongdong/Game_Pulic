package com.hutong.scene.server;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.dispatcher.SceneResponseMsgQueue;
import com.hutong.socketbase.commonutil.CommonFunc;
import com.hutong.socketbase.define.PlayerUniqueKey;
import com.hutong.socketbase.sessionbase.SceneSession;

@Component
public class SceneServerData <T extends SceneSession>{
	
	public static AttributeKey<GatewayInfo> GATEWAY_INFO_KEY = AttributeKey.valueOf("GATEWAY_INFO");
	
	//lineId -> playerNum  线->对应的玩家数量
	private Map<Integer, AtomicInteger> linePlayerSessionNum = new ConcurrentHashMap<Integer, AtomicInteger>();
	
	//puk -> sceneSession
	private final ConcurrentMap<PlayerUniqueKey, T> scenePlayerSessionMap = new ConcurrentHashMap<PlayerUniqueKey, T>();
	
	//记录gateway的信息   gatewayId -> gatewayInfo
//	private final ConcurrentMap<String, GatewayInfo> gateInfoMap = new ConcurrentHashMap<String, GatewayInfo>();
	
	@Autowired
	private SceneResponseMsgQueue sceneResponseMsgQueue;
	
	/**
	 * 获取所有线上的玩家数量
	 * @return
	 */
	public int getTotalPlayerNum(){
		return scenePlayerSessionMap.size();
	}
	
	/**
	 * 获取某一个线上的玩家数	
	 * @param lineId
	 * @return
	 */
	public int getLinePlayerNum(int lineId){
		
		AtomicInteger playerSessionNum = linePlayerSessionNum.get(lineId);
		if(playerSessionNum == null){
			return 0;
		} else {
			return playerSessionNum.get();
		}
	}
	
	/**
	 * 增加某一个线上的玩家数
	 * @param lineId
	 */
	private void incSceneSessionNum(int lineId) {
		AtomicInteger playerSessionNum = linePlayerSessionNum.get(lineId);
		if(playerSessionNum == null){
			synchronized (this){
				playerSessionNum = linePlayerSessionNum.get(lineId);
				if(playerSessionNum == null){
					playerSessionNum = new AtomicInteger(0);
					linePlayerSessionNum.put(lineId, playerSessionNum);
				}
			}
		}
		playerSessionNum.incrementAndGet();
	}
	
	/**
	 * 减少某一个线上的玩家数
	 * @param lineId
	 */
	private void decSceneSessionNum(int lineId) {
		AtomicInteger playerSessionNum = linePlayerSessionNum.get(lineId);
		if(playerSessionNum != null){
			playerSessionNum.decrementAndGet();
		}
	}
	
	/**
	 * 如果运行的时间足够长，则有可能出现连锁错误  在这里矫正一下
	 * @param linePlayerSessionNum
	 */
	private void replaceLinePlayerSessionNum(Map<Integer, AtomicInteger> linePlayerSessionNum){
		synchronized (this){
			this.linePlayerSessionNum = linePlayerSessionNum;
		}
	}
	
	/**
	 * 矫正每个线上的人数
	 */
	public void correctLinePlayerNum(){
		
		Map<Integer, AtomicInteger> tmpLinePlayerSessionNum = new ConcurrentHashMap<Integer, AtomicInteger>();
		
		for(PlayerUniqueKey puk : scenePlayerSessionMap.keySet()){
			
			SceneSession sceneSession = scenePlayerSessionMap.get(puk);
			
			AtomicInteger playerSessionNum = tmpLinePlayerSessionNum.get(sceneSession.getLineId());
			if(playerSessionNum == null){
				playerSessionNum = new AtomicInteger(0);
				tmpLinePlayerSessionNum.put(sceneSession.getLineId(), playerSessionNum);
			}
			
			playerSessionNum.incrementAndGet();
		}
		
		replaceLinePlayerSessionNum(tmpLinePlayerSessionNum);
	}

	public T getSceneSession(int serverId, long playerId){
		return scenePlayerSessionMap.get(CommonFunc.getPlayerUniqueKey(serverId, playerId));
	}
	
	public void addSceneSession(int lineId, int serverId, long playerId, T session) {
		
		PubQueueLogUtil.logWarn("addSceneSession lineId" + lineId + ", serverId " + serverId + ", playerId " + playerId);
		T sceneSession = scenePlayerSessionMap.put(CommonFunc.getPlayerUniqueKey(serverId, playerId), session);
		if(null == sceneSession){
			incSceneSessionNum(lineId);
		}
	}

	public void removeSceneSession(int serverId, long playerId) {
		PubQueueLogUtil.logWarn("removeSceneSession serverId " + serverId + ", playerId " + playerId);
		T sceneSession = scenePlayerSessionMap.remove(CommonFunc.getPlayerUniqueKey(serverId, playerId));
		if(sceneSession != null){
			decSceneSessionNum(sceneSession.getLineId());
		}
	}
	
	public List<T> getAllSceneSessions(){
		
		List<T> tList = new ArrayList<T>();
		for(T t : scenePlayerSessionMap.values()){
			tList.add(t);
		}
		
		return tList;
	}
	
	
	
	public Set<String> getAllGatewayIds(){
		return sceneResponseMsgQueue.getAllGatewayIds();
	}
	
	
	public GatewayInfo getGatewayInfoByChannel(Channel channel){
		return channel.attr(GATEWAY_INFO_KEY).get();
	}
}
