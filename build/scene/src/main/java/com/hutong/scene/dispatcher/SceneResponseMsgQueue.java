package com.hutong.scene.dispatcher;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.framework.util.SwapableQueuePair;
import com.hutong.scene.interf.SceneIntercept;
import com.hutong.scene.server.GatewayInfo;
import com.hutong.scene.server.SceneServerData;
import com.hutong.socketbase.codec.innermessage.InnerSceneToGateMessage;
import com.hutong.socketbase.socketactiondata.SceneActionData;

@Service
public class SceneResponseMsgQueue {
	
	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("sceneInterceptImpl")
	private SceneIntercept sceneIntercept;
	
	//向Gateway推送消息队列  gatewayId->
	private Map<String, List<ScenePushMsgQueueInfo>> gatewayIdMapPushMsgQueue = new ConcurrentHashMap<>();
	
	
	public List<ScenePushMsgQueueInfo> getScenePushMsgQueueInfoList(String gatewayId) {
		return gatewayIdMapPushMsgQueue.get(gatewayId);
	}
	
	public Set<String> getAllGatewayIds(){
		return gatewayIdMapPushMsgQueue.keySet();
	}
	
	
	public static class ScenePushMsgQueueInfo{
		
		String gatewayId;
		Channel scene2GatewayChannel;
		
		SwapableQueuePair<SceneActionData> msgQueuePair;
		ScheduledThreadPoolExecutor schedulePushDataThread;
		ScheduledFuture<?> future;
		
		ScenePushMsgQueueInfo(String gatewayId, Channel scene2GatewayChannel){
			this.gatewayId = gatewayId;
			this.scene2GatewayChannel = scene2GatewayChannel;
			this.msgQueuePair = new SwapableQueuePair<>();
			this.schedulePushDataThread = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(gatewayId + "-ResponseMsgThread-Group"));
		}
		
		void start(SceneResponseMsgQueue sceneResponseMsgQueue){
			if(this.future == null){  
				this.future = schedulePushDataThread.scheduleWithFixedDelay(new SceneResponseThread(sceneResponseMsgQueue, this.gatewayId, this.scene2GatewayChannel, this.msgQueuePair),  0, 5, TimeUnit.MILLISECONDS);
			} else {
				PubQueueLogUtil.logError("ScenePushMsgQueueInfo future start not null " + gatewayId);
			}
		}
		
		void stop(){
			if(this.future != null){
				this.future.cancel(false);
				this.future = null;
			} else {
				PubQueueLogUtil.logError("ScenePushMsgQueueInfo future stop not null " + gatewayId);
			}
			
			if(this.schedulePushDataThread != null){
				this.schedulePushDataThread.shutdown();
				this.schedulePushDataThread = null;
			} else {
				PubQueueLogUtil.logError("ScenePushMsgQueueInfo schedulePushDataThread stop not null " + gatewayId);
			}
		}
		
		public void closeChannel(){
			scene2GatewayChannel.close();
		}
	}
	
	
	//添加
	public void addSceneActionData(String gatewayId, SceneActionData sceneActionData){
		List<ScenePushMsgQueueInfo> scenePushMsgQueueInfoList = gatewayIdMapPushMsgQueue.get(gatewayId);
		if(scenePushMsgQueueInfoList != null && !scenePushMsgQueueInfoList.isEmpty()){
			
			if(sceneActionData.getBytes() == null){
				sceneActionData.setBytes(new byte[0]);
			}
			
			//根据渠道数 取模  获取相应的channel
			long playerId = sceneActionData.getPlayerId();
			if(playerId < 0){
				PubQueueLogUtil.logError("addSceneActionData playerId is " + playerId);
				playerId = 0;
			}
			
			int queueSize = scenePushMsgQueueInfoList.size();
			int modId = (int)(playerId%queueSize);
			
			ScenePushMsgQueueInfo scenePushMsgQueueInfo = scenePushMsgQueueInfoList.get(modId);
			
			if(scenePushMsgQueueInfo != null){
				scenePushMsgQueueInfo.msgQueuePair.second().offer(sceneActionData);
			} else {
				PubQueueLogUtil.logError("could not found gatewayId : " + gatewayId);
			}
		} else {
			PubQueueLogUtil.logError("could not find scenePushMsgQueueInfoList (or empty), gatewayId is " + gatewayId + ", " + sceneActionData.getServerId() + "-" 
					+ sceneActionData.getPlayerId() + " pbcode: " + sceneActionData.getCode());
		}
	}
	
	//添加gateway的同时  初始化线程数据
	public synchronized boolean addSceneQueue(String gatewayId, Channel gatewayChannel){
		
		PubQueueLogUtil.logWarn("add GatewayInfo gatewayId is " + gatewayId + ", gatewayChannel: " + gatewayChannel);
		
		//先添加gateway信息
		GatewayInfo gatewayInfo = new GatewayInfo(gatewayId);
		gatewayChannel.attr(SceneServerData.GATEWAY_INFO_KEY).set(gatewayInfo);
		
		//在添加队列信息
		List<ScenePushMsgQueueInfo> scenePushMsgQueueInfoList = gatewayIdMapPushMsgQueue.get(gatewayId);
		if(scenePushMsgQueueInfoList == null){
			scenePushMsgQueueInfoList = new ArrayList<ScenePushMsgQueueInfo>();
			gatewayIdMapPushMsgQueue.put(gatewayId, scenePushMsgQueueInfoList);
		}
		
		ScenePushMsgQueueInfo scenePushMsgQueueInfo = new ScenePushMsgQueueInfo(gatewayId, gatewayChannel);
		scenePushMsgQueueInfoList.add(scenePushMsgQueueInfo);
		
		scenePushMsgQueueInfo.start(this);
		
		return true;
	}
	
	public synchronized void removeSceneQueue(String gatewayId, Channel removedChannel){
		
		PubQueueLogUtil.logInfo("removeGatewayQueue " + ", removedChannel:" + removedChannel);
		
		List<ScenePushMsgQueueInfo> scenePushMsgQueueInfoList = getScenePushMsgQueueInfoList(gatewayId);
		if(scenePushMsgQueueInfoList != null){
			Iterator<ScenePushMsgQueueInfo> iter = scenePushMsgQueueInfoList.iterator();
			while (iter.hasNext()) {
				ScenePushMsgQueueInfo scenePushMsgQueueInfo = iter.next();
				if(scenePushMsgQueueInfo.scene2GatewayChannel == removedChannel){
					scenePushMsgQueueInfo.stop();
					iter.remove();
				}
			}
		}
		
		if(scenePushMsgQueueInfoList == null || scenePushMsgQueueInfoList.isEmpty()){
			PubQueueLogUtil.logInfo("removeGatewayQueue total gatewayId " + gatewayId);
			gatewayIdMapPushMsgQueue.remove(gatewayId);
		}
	}
	
	private InnerSceneToGateMessage mergeResponseData(Queue<SceneActionData> msgQueue){
		
		InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();

		int curPacketSize = 0;
		
		SceneActionData sceneActionData = msgQueue.poll();
		while(sceneActionData != null){
			
			//这里加上20的原因是：  如果发送多个空的包  则也计算入体积
			int byteLen = sceneActionData.getBytes() == null ? 0 : sceneActionData.getBytes().length;
			curPacketSize += byteLen + 20;
			innerSceneToGateMessage.addSceneActionData(sceneActionData);
			
			//1400这个数字是网上查到的 
			if(curPacketSize > 1400){
				break;
			}
			sceneActionData = msgQueue.poll();
		}
		
		return innerSceneToGateMessage;
	}
	
	private void sceneResponse(String gatewayId, Channel scene2GatewayChannel, Queue<SceneActionData> msgQueue){
		
		InnerSceneToGateMessage innerSceneToGateMessage = mergeResponseData(msgQueue);
		
		while (!innerSceneToGateMessage.getSceneActionDataList().isEmpty()) {
			
			try {
				if(scene2GatewayChannel == null){
					PubQueueLogUtil.logWarn("could not find gateway channel, gatewayId is " + gatewayId + ", channel is " + scene2GatewayChannel);
				} else {
					if(scene2GatewayChannel.isWritable()){
						
						scene2GatewayChannel.writeAndFlush(innerSceneToGateMessage);
						
						for(SceneActionData sceneActionData : innerSceneToGateMessage.getSceneActionDataList()){
							if(sceneIntercept != null && sceneActionData.getCode() >= 10000){
								sceneIntercept.sceneResponseData(sceneActionData);
							}
						}
					} else {
						if(sceneIntercept != null){
							sceneIntercept.sceneToGatewayWritableFalse(gatewayId, innerSceneToGateMessage.getSceneActionDataList());
						} else {
							PubQueueLogUtil.logError("isWritable is false, scene->gateway, gatewayId : " + gatewayId + ", channel is " + scene2GatewayChannel
									+ ", drop Msg innerSceneToGateMessage " + innerSceneToGateMessage.toString());
						}
					}
				}
			} catch (Exception e) {
				PubQueueLogUtil.logError(" sceneResponse ", e);
			}
			
			innerSceneToGateMessage = mergeResponseData(msgQueue);
		}
	}
	
	
	public static class SceneResponseThread implements Runnable {
		
		private SceneResponseMsgQueue sceneResponseMsgQueue;
		
		private String gatewayId;
		
		private Channel scene2GatewayChannel;
		
		private SwapableQueuePair<SceneActionData> msgQueuePair;
		
		public SceneResponseThread(SceneResponseMsgQueue sceneResponseMsgQueue, String gatewayId, Channel scene2GatewayChannel, SwapableQueuePair<SceneActionData> msgQueuePair){
			this.sceneResponseMsgQueue = sceneResponseMsgQueue;
			this.gatewayId = gatewayId;
			this.scene2GatewayChannel = scene2GatewayChannel;
			this.msgQueuePair = msgQueuePair;
		}
		
		@Override
        public void run() {
			try {
				msgQueuePair.swap();
				sceneResponseMsgQueue.sceneResponse(gatewayId, scene2GatewayChannel, msgQueuePair.first());
			} catch (Exception e) {
				PubQueueLogUtil.logError("SceneResponseThread throw exception", e);
			}
		}
	}
}
