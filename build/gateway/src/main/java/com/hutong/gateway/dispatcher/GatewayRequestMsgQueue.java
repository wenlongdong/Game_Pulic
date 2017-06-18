package com.hutong.gateway.dispatcher;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.framework.util.SwapableQueuePair;
import com.hutong.gateway.interf.GatewayIntercept;
import com.hutong.socketbase.codec.clientmessage.ClientRequestMessageIf;
import com.hutong.socketbase.codec.innermessage.InnerGateMessageItem;
import com.hutong.socketbase.codec.innermessage.InnerGateToSceneMessage;

@Service
public class GatewayRequestMsgQueue {
	
	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("gatewayInterceptImpl")
	private GatewayIntercept<ClientRequestMessageIf> gatewayIntercept;
	
	//向Scene发送请求的消息队列  sceneServerId -> list<GatewayRequestMsgQueueInfo>
	private Map<String, List<GatewayRequestMsgQueueInfo>> sceneServerIdMapPushMsgQueue = new ConcurrentHashMap<>();
	

	public List<GatewayRequestMsgQueueInfo> getGatewayRequestMsgQueueInfoList(String sceneServerId) {
		return sceneServerIdMapPushMsgQueue.get(sceneServerId);
	}



	public static class GatewayRequestMsgQueueInfo{
		
		String sceneServerId;
		Channel gateway2SceneChannel;
		
		SwapableQueuePair<InnerGateMessageItem> msgQueuePair;
		ScheduledThreadPoolExecutor scheduleRequestDataThread;
		ScheduledFuture<?> future;
		
		GatewayRequestMsgQueueInfo(String sceneServerId, Channel gateway2SceneChannel){
			this.sceneServerId = sceneServerId;
			this.gateway2SceneChannel = gateway2SceneChannel;
			this.msgQueuePair = new SwapableQueuePair<>();
			this.scheduleRequestDataThread = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(sceneServerId + "-RequestMsgThread-Group"));
		}
		
		void start(GatewayRequestMsgQueue gatewayRequestMsgQueue){
			if(this.future == null){
				this.future = scheduleRequestDataThread.scheduleWithFixedDelay(new GatewayRequestThread(gatewayRequestMsgQueue, this.sceneServerId, this.gateway2SceneChannel, this.msgQueuePair),  0, 5, TimeUnit.MILLISECONDS);
			} else {
				PubQueueLogUtil.logError("GatewayRequestMsgQueueInfo future start not null " + sceneServerId);
			}
		}
		
		void stop(){
			if(this.future != null){
				this.future.cancel(false);
				this.future = null;
			} else {
				PubQueueLogUtil.logError("GatewayRequestMsgQueueInfo future stop not null " + sceneServerId);
			}
			
			if(this.scheduleRequestDataThread != null){
				this.scheduleRequestDataThread.shutdown();
				this.scheduleRequestDataThread = null;
			} else {
				PubQueueLogUtil.logError("GatewayRequestMsgQueueInfo schedulePushDataThread stop not null " + sceneServerId);
			}
		}
		
		void closeChannel(){
			gateway2SceneChannel.close();
		}
	}
	
	
	//添加
	public void addGatewayMessageData(String sceneServerId, InnerGateMessageItem innerGateMessageItem){
		List<GatewayRequestMsgQueueInfo> gatewayRequestMsgQueueInfoList = sceneServerIdMapPushMsgQueue.get(sceneServerId);
		if(gatewayRequestMsgQueueInfoList != null && !gatewayRequestMsgQueueInfoList.isEmpty()){
			
			if(innerGateMessageItem.getBytes() == null){
				innerGateMessageItem.setBytes(new byte[0]);
			}
			
			//根据渠道数 取模  获取相应的channel
			long playerId = innerGateMessageItem.getPlayerId();
			if(playerId < 0){
				PubQueueLogUtil.logError("addGatewayMessageData playerId is " + playerId);
				playerId = 0;
			}
			
			int channelSize = gatewayRequestMsgQueueInfoList.size();
			int modId = (int)(playerId%channelSize);
			
			GatewayRequestMsgQueueInfo gatewayRequestMsgQueueInfo = gatewayRequestMsgQueueInfoList.get(modId);
			
			if(gatewayRequestMsgQueueInfo != null){
				gatewayRequestMsgQueueInfo.msgQueuePair.second().offer(innerGateMessageItem);
			} else {
				PubQueueLogUtil.logError("scenePushMsgQueueInfoList could not found GatewayRequestMsgQueueInfo, modId is " + modId + " sceneServerId : " + sceneServerId);
			}
		} else {
			PubQueueLogUtil.logError("scenePushMsgQueueInfoList is null (or empty) and sceneServerId : " + sceneServerId);
		}
	}
	
	//添加scene的同时  初始化线程数据
	public synchronized boolean addGatewayQueue(String sceneServerId, Channel gateway2SceneChannel){
		
		PubQueueLogUtil.logWarn("add SceneServer sceneServerId is " + sceneServerId + ", sceneChannel: " + gateway2SceneChannel);
		
		List<GatewayRequestMsgQueueInfo> gatewayRequestMsgQueueInfoList = sceneServerIdMapPushMsgQueue.get(sceneServerId);
		if(gatewayRequestMsgQueueInfoList == null){
			gatewayRequestMsgQueueInfoList = new ArrayList<GatewayRequestMsgQueueInfo>();
			sceneServerIdMapPushMsgQueue.put(sceneServerId, gatewayRequestMsgQueueInfoList);
		}
		
		GatewayRequestMsgQueueInfo scenePushMsgQueueInfo = new GatewayRequestMsgQueueInfo(sceneServerId, gateway2SceneChannel);
		gatewayRequestMsgQueueInfoList.add(scenePushMsgQueueInfo);
		
		scenePushMsgQueueInfo.start(this);
		
		return true;
	}
	
	public synchronized void removeGatewayQueue(String sceneServerId, Channel removedChannel){
		
		PubQueueLogUtil.logInfo("removeSceneQueue " + sceneServerId + ", removedChannel:" + removedChannel);
		
		List<GatewayRequestMsgQueueInfo> gatewayRequestMsgQueueInfoList = sceneServerIdMapPushMsgQueue.get(sceneServerId);
		if(gatewayRequestMsgQueueInfoList != null){
			Iterator<GatewayRequestMsgQueueInfo> iter = gatewayRequestMsgQueueInfoList.iterator();
			while (iter.hasNext()) {
				GatewayRequestMsgQueueInfo gatewayRequestMsgQueueInfo = iter.next();
				if(gatewayRequestMsgQueueInfo.gateway2SceneChannel == removedChannel){
					gatewayRequestMsgQueueInfo.stop();
					iter.remove();
				}
			}
		}
		
		if(gatewayRequestMsgQueueInfoList == null || gatewayRequestMsgQueueInfoList.isEmpty()){
			PubQueueLogUtil.logInfo("removeSceneQueue total scene " + sceneServerId);
			sceneServerIdMapPushMsgQueue.remove(sceneServerId);
		}
	}
	
	private InnerGateToSceneMessage mergeRequestData(Queue<InnerGateMessageItem> msgQueue){
		
		InnerGateToSceneMessage innerGateToSceneMessage = new InnerGateToSceneMessage();

		int curPacketSize = 0;
		
		InnerGateMessageItem innerGateMessageItem = msgQueue.poll();
		while(innerGateMessageItem != null){
			
			//这里加上20的原因是：  如果发送多个空的包  则也计算入体积
			int byteLen = innerGateMessageItem.getBytes() == null ? 0 : innerGateMessageItem.getBytes().length;
			curPacketSize += byteLen + 20;
			innerGateToSceneMessage.getInnerGateMessageItemList().add(innerGateMessageItem);
			
			//1400这个数字是网上查到的 
			if(curPacketSize > 1400){
				break;
			}
			innerGateMessageItem = msgQueue.poll();
		}
		
		return innerGateToSceneMessage;
	}
	
	
	private void requestScene(String sceneServerId, Channel gateway2SceneChannel, Queue<InnerGateMessageItem> msgQueue){
		
		InnerGateToSceneMessage innerGateToSceneMessage = mergeRequestData(msgQueue);
		
		while (!innerGateToSceneMessage.getInnerGateMessageItemList().isEmpty()) {
			
			try {
				if(gateway2SceneChannel == null){
					PubQueueLogUtil.logWarn("could not find sceneServerId " + sceneServerId + ", channel is " + gateway2SceneChannel);
				} else {
					if(gateway2SceneChannel.isWritable()){
						gateway2SceneChannel.writeAndFlush(innerGateToSceneMessage);
					} else {
						if(gatewayIntercept != null) {
							gatewayIntercept.gatewayToSceneWritableFalse(sceneServerId, innerGateToSceneMessage.getInnerGateMessageItemList());
						} else {
							PubQueueLogUtil.logWarn("isWritable is false, gateway->scene, sceneServerId : " + sceneServerId + ", channel is " + gateway2SceneChannel
									+ ", drop Msg innerGateToSceneMessage : " + innerGateToSceneMessage.toString());
						}
					}
				}
			} catch (Exception e) {
				PubQueueLogUtil.logError(" requestScene ", e);
			}
			
			innerGateToSceneMessage = mergeRequestData(msgQueue);
		}
	}
	
	
	public static class GatewayRequestThread implements Runnable {
		
		private GatewayRequestMsgQueue gatewayRequestMsgQueue;
		
		private String sceneServerId;
		
		private Channel gateway2SceneChannel;
		
		private SwapableQueuePair<InnerGateMessageItem> msgQueuePair;
		
		public GatewayRequestThread(GatewayRequestMsgQueue gatewayRequestMsgQueue, String sceneServerId, Channel gateway2SceneChannel, SwapableQueuePair<InnerGateMessageItem> msgQueuePair){
			this.gatewayRequestMsgQueue = gatewayRequestMsgQueue;
			this.sceneServerId = sceneServerId;
			this.gateway2SceneChannel = gateway2SceneChannel;
			this.msgQueuePair = msgQueuePair;
		}
		
		@Override
        public void run() {
			try {
				msgQueuePair.swap();
				gatewayRequestMsgQueue.requestScene(sceneServerId, gateway2SceneChannel, msgQueuePair.first());
			} catch (Exception e) {
				PubQueueLogUtil.logError("GatewayRequestThread throw exception", e);
			}
		}
	}
}
