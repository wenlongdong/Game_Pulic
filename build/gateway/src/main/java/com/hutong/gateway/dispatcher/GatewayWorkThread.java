package com.hutong.gateway.dispatcher;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hutong.framework.base.dispatch.invocation.PBInvocation;
import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.gateway.gatewayserver.GatewayServerData;
import com.hutong.gateway.interf.GatewayIntercept;
import com.hutong.gateway.interf.GatewayInterface;
import com.hutong.socketbase.codec.clientmessage.ClientRequestMessageIf;
import com.hutong.socketbase.codec.clientmessage.ClientResponseMessageIf;
import com.hutong.socketbase.sessionbase.GatewaySession;
import com.hutong.socketbase.socketactiondata.SocketActionData;

/*
 * 
 * 这个类里面有一些重复代码  原因是  如果找到了handler，则直接调用commonFunction.write2Scene就行，不用再开一个线程来执行
 * 而这里的函数大多都判断了null == protocol 的情况  这种情况只是为了再次验证一下  防止连锁错误
 * 
 */
@Service
public class GatewayWorkThread<T extends ClientRequestMessageIf> {
	
	@Autowired
	@Qualifier(value="gatewayInterfaceImpl")
	private GatewayInterface gatewayInterface;
	
	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("gatewayInterceptImpl")
	private GatewayIntercept<T> gatewayIntercept;
	
	@Autowired
	private GatewayServerData<GatewaySession> gatewayServerData;
	
	@Autowired
	private GatewayDispatcher gatewayDispatcher;
	
	@Autowired
	private GateCommonFunction commonFunction;
	
	
	private void clientToGateFunc(T clientRequestMessage){
		
		SocketActionData socketActionData = new SocketActionData(clientRequestMessage.getPlayerId(), clientRequestMessage.getServerId(), 
				clientRequestMessage.getClientChannel(), clientRequestMessage.getCode(), clientRequestMessage.getBytes());
		
		PBInvocation protocol = gatewayDispatcher.getActionInvocation(PBInvocation.class, socketActionData.getCode());
		
		Object attachMent = null;
		Object resultObj = null;
		if(null == protocol){//如果找不到响应需要处理的注解，则直接查找玩家当前对应的scene,将消息发送给响应的scene
			try {
				if(gatewayIntercept != null){
					attachMent = gatewayIntercept.beforeClientToGate(clientRequestMessage, protocol);
				}
				commonFunction.write2Scene(socketActionData);
			} catch (Exception e) {
				if(gatewayIntercept != null){
					resultObj = gatewayIntercept.catchClientToGateException(e, clientRequestMessage);
				} else {
					PubQueueLogUtil.logError("clientToGateService throw a big exception!!!", e);
				}
			} finally {
				if(gatewayIntercept != null){
					gatewayIntercept.afterClientToGate(resultObj, attachMent);
				}
			}
		} else {//如果找到了相应的handler，则单独开启一个线程来处理逻辑
			try {
				if(gatewayIntercept != null){
					attachMent = gatewayIntercept.beforeClientToGate(clientRequestMessage, protocol);
				}
				protocol.invoke(socketActionData);
			} catch (Exception e) {
				if(gatewayIntercept != null){
					resultObj = gatewayIntercept.catchClientToGateException(e, clientRequestMessage);
				} else {
					PubQueueLogUtil.logError("clientToGateService throw a big exception!!!", e);
				}
			} finally {
				if(gatewayIntercept != null){
					gatewayIntercept.afterClientToGate(resultObj, attachMent);
				}
			}
		}
	}
	
	public static class ClientToGateWorkThread<T extends ClientRequestMessageIf> extends Thread {

		private GatewayWorkThread<T> gatewayWorkThread;
		private T clientRequestMessage;
		private long startTime = System.currentTimeMillis();
		
		public ClientToGateWorkThread(GatewayWorkThread<T> gatewayWorkThread, T clientRequestMessage) {
			this.gatewayWorkThread = gatewayWorkThread;
			this.clientRequestMessage = clientRequestMessage;
		}
		
		@Override
		public void run() {
			
			gatewayWorkThread.clientToGateFunc(clientRequestMessage);

			long endTime = System.currentTimeMillis();
			
			if(endTime - startTime > 10){
				SocketActionData socketActionData = new SocketActionData(clientRequestMessage.getPlayerId(), clientRequestMessage.getServerId(), 
						clientRequestMessage.getClientChannel(), clientRequestMessage.getCode(), clientRequestMessage.getBytes());
				PubQueueLogUtil.logError("GatewayWorkThread -> ClientToGateWork take too long time!!!, costTime is " + (endTime-startTime) 
						+ " ; And SocketActionData is " + socketActionData.toString());
			}
		}
	}

	
	
	
	private void gateToSceneWorkFunc(SocketActionData socketActionData){
		try {
			PBInvocation protocol = gatewayDispatcher.getActionInvocation(PBInvocation.class, socketActionData.getCode());
			if(null == protocol){//这里按说protocol不可能为null  加这个只是为了再次验证一下，防止连锁错误
				commonFunction.write2Scene(socketActionData);
			} else {
				protocol.invoke(socketActionData);
			}
		} catch (Exception e) {
			if(gatewayIntercept != null){
				gatewayIntercept.catchGateToSceneException(e, socketActionData);
			} else {
				PubQueueLogUtil.logError("GateToSceneWorkThread throw a big exception!!!", e);
			}
		}
	}
	
	public static class GateToSceneWorkThread<T extends ClientRequestMessageIf> extends Thread {

		private GatewayWorkThread<T> gatewayWorkThread;
		private SocketActionData socketActionData;
		private long startTime = System.currentTimeMillis();
		
		public GateToSceneWorkThread(GatewayWorkThread<T> gatewayWorkThread, SocketActionData socketActionData) {
			this.gatewayWorkThread = gatewayWorkThread;
			this.socketActionData = socketActionData;
		}
		
		@Override
		public void run() {
			
			gatewayWorkThread.gateToSceneWorkFunc(socketActionData);
			
			long endTime = System.currentTimeMillis();
			
			if(endTime - startTime > 10){
				PubQueueLogUtil.logError("GatewayWorkThread -> GateToSceneWork take too long time!!!, costTime is " + (endTime-startTime) 
						+ " ; And SocketActionData is " + socketActionData.toString());
			}
		}
	}
	
	
	
	private void sceneToGateWorkFunc(SocketActionData socketActionData){
		try {
			PBInvocation protocol = gatewayDispatcher.getActionInvocation(PBInvocation.class, socketActionData.getCode());
			if(null == protocol){//如果找不到响应需要处理的注解，则直接查找响应的客户端channel，返回消息给客户端
				Channel channel = gatewayServerData.getClientChannelBy(socketActionData.getServerId(), socketActionData.getPlayerId());
				if(channel != null){
					ClientResponseMessageIf clientResponseMessageIf = gatewayInterface.genResponseMsgWith(socketActionData.getPlayerId(),
							socketActionData.getServerId(), socketActionData.getCode(), socketActionData.getBytes());
					if(channel.isWritable()){
						channel.writeAndFlush(clientResponseMessageIf);
					} else {
						if(gatewayIntercept != null){
							gatewayIntercept.gatewayToClientWritableFalse(channel, socketActionData);
						} else {
							InetSocketAddress socketAddress = (InetSocketAddress)channel.remoteAddress();
							PubQueueLogUtil.logError("isWritable is false, gateway->client playerId:" + socketActionData.getPlayerId() + ", serverId:" + socketActionData.getServerId()
									+ ", opCode:" + socketActionData.getCode() + ", ip: " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
						}
					}
				} else {
					PubQueueLogUtil.logWarn("sceneToGateService could not process msg from scene!! playerId is " + socketActionData.getPlayerId() +
							" serverId is " + socketActionData.getServerId() + " and msg code is : " + socketActionData.getCode());
				}
			} else {
				protocol.invoke(socketActionData);
			}
		} catch (Exception e) {
			if(gatewayIntercept != null){
				gatewayIntercept.catchSceneToGateException(e, socketActionData);
			} else {
				PubQueueLogUtil.logError("SceneToGateWorkThread throw a big exception!!!", e);
			}
		}
	}
	
	public static class SceneToGateWorkThread<T extends ClientRequestMessageIf> extends Thread {

		private GatewayWorkThread<T> gatewayWorkThread;
		private SocketActionData socketActionData;
		long startTime = System.currentTimeMillis();
		
		public SceneToGateWorkThread(GatewayWorkThread<T> gatewayWorkThread, SocketActionData socketActionData) {
			this.gatewayWorkThread = gatewayWorkThread;
			this.socketActionData = socketActionData;
		}
		
		@Override
		public void run() {
			
			gatewayWorkThread.sceneToGateWorkFunc(socketActionData);
			
			long endTime = System.currentTimeMillis();
			
			if(endTime - startTime > 10){
				PubQueueLogUtil.logError("GatewayWorkThread -> SceneToGateWork take too long time!!!, costTime is " + (endTime-startTime) 
						+ " ; And SocketActionData is " + socketActionData.toString());
			}
		}
	}
}
