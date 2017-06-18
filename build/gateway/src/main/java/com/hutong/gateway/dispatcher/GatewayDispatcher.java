package com.hutong.gateway.dispatcher;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.hutong.framework.base.dispatch.invocation.PBInvocation;
import com.hutong.framework.soket.dispatch.SocketDispatcher;
import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.framework.util.SpringObjectFactory;
import com.hutong.gateway.GatewayConfig;
import com.hutong.gateway.dispatcher.GatewayRequestMsgQueue.GatewayRequestMsgQueueInfo;
import com.hutong.gateway.dispatcher.GatewayWorkThread.ClientToGateWorkThread;
import com.hutong.gateway.dispatcher.GatewayWorkThread.GateToSceneWorkThread;
import com.hutong.gateway.dispatcher.GatewayWorkThread.SceneToGateWorkThread;
import com.hutong.gateway.gatewayserver.GatewayServerData;
import com.hutong.gateway.interf.GatewayIntercept;
import com.hutong.gateway.interf.GatewayInterface;
import com.hutong.socketbase.codec.clientmessage.ClientRequestMessageIf;
import com.hutong.socketbase.codec.innermessage.InnerGateDecodedMessage;
import com.hutong.socketbase.codec.innermessage.InnerGateDecodedMessage.FromSceneMessageItem;
import com.hutong.socketbase.define.PlayerUniqueKey;
import com.hutong.socketbase.message.GSMessage.GSRegist;
import com.hutong.socketbase.message.InnerOpDefineDefault;
import com.hutong.socketbase.sessionbase.GatewaySession;
import com.hutong.socketbase.socketactiondata.SocketActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Component
public class GatewayDispatcher extends SocketDispatcher{

	@Autowired
	private GatewayServerData<GatewaySession> gatewayServerData;
	
	@Autowired
	@Qualifier(value="innerActionPath")
	private String innerActionPath; //gateway内部自己需要处理的一些action

	@Autowired
	@Qualifier(value="actionPath")
	private String actionPath;//外部框架使用的actionpath
	
	@Autowired
	private SpringObjectFactory springObjectFactory;

	@Autowired
	@Qualifier(value="gatewayInterfaceImpl")
	private GatewayInterface gatewayInterface;
	
	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("gatewayInterceptImpl")
	private GatewayIntercept<ClientRequestMessageIf> gatewayIntercept;
	
	@Autowired
	private GateCommonFunction commonFunction;
	
	@Autowired
	private GatewayWorkThread<ClientRequestMessageIf> gatewayWorkThread;
	
	@Autowired
	private GatewayConfig gatewayConfig;
	
	@Autowired
	private GatewayRequestMsgQueue gatewayRequestMsgQueue;
	
	//netty工作线程池，用来处理玩家的实际业务需求
	public ThreadPoolExecutor[] gatewayWorkGroupAry;
	
	public ThreadPoolExecutor gatewayDefaultWorkGroup;
	
	public void init() throws Exception {
		this.initHandleAction(innerActionPath, springObjectFactory);
		this.initHandleAction(actionPath, springObjectFactory);
		
		gatewayWorkGroupAry = new ThreadPoolExecutor[gatewayConfig.getWorkGroupThreadNum()];
		for(int num=0; num<gatewayWorkGroupAry.length; ++num){
			gatewayWorkGroupAry[num] = (ThreadPoolExecutor)Executors.newFixedThreadPool(1, new DefaultThreadFactory("Gateway-WorkGroup-" + num));
		}
	
		gatewayDefaultWorkGroup = (ThreadPoolExecutor)Executors.newFixedThreadPool(8, new DefaultThreadFactory("Gateway-WorkGroup-Default"));
	}

	public ThreadPoolExecutor getGatewayWorkGroup(long playerId){
		
		if(playerId <= 0){
			return gatewayDefaultWorkGroup;
		} else {
			int index = (int)(playerId % gatewayWorkGroupAry.length);
			return gatewayWorkGroupAry[index]; 
		}
	}
	
	public ThreadPoolExecutor getGatewayWorkGroup(SocketActionData socketActionData){
		
		ThreadPoolExecutor workThreadExecutor = null;
		if(gatewayIntercept != null){
			workThreadExecutor = gatewayIntercept.getThreadPoolExecutor(socketActionData);
		}
		
		if(workThreadExecutor == null){//在gateway默认的线程池里面执行
			workThreadExecutor = getGatewayWorkGroup(socketActionData.getPlayerId());
		}
		
		return workThreadExecutor;
	}
	
	public <T extends ClientRequestMessageIf> void clientToGateService(T clientRequestMessage) throws Exception {
		
		//从GatewaySession获取playerId和 serverId 而不是客户端发过来
		PlayerUniqueKey playerUniqueKey = gatewayServerData.getPlayerUniqueKeyByChannel(clientRequestMessage.getClientChannel());
		GatewaySession gatewaySession = gatewayServerData.getGatewaySessionBy(playerUniqueKey);
		if(null != gatewaySession){
			clientRequestMessage.setPlayerId(gatewaySession.getPlayerId());
			clientRequestMessage.setServerId(gatewaySession.getServerId());
		}
		
		/////////////////////
		long playerId = clientRequestMessage.getPlayerId();
		if(playerId == 0 && gatewayIntercept != null){
			playerId = gatewayIntercept.queryPlayerId(clientRequestMessage.getCode(), clientRequestMessage.getBytes());
		}
		
		gatewayIntercept.doBuzInCGIO(clientRequestMessage);
		
		SocketActionData socketActionData = new SocketActionData(playerId, clientRequestMessage.getServerId(), 
				clientRequestMessage.getClientChannel(), clientRequestMessage.getCode(), clientRequestMessage.getBytes());
		ThreadPoolExecutor workThreadExecutor = getGatewayWorkGroup(socketActionData);
		/////////////////////
		
		if(workThreadExecutor.getQueue().size() > 1000){
			PubQueueLogUtil.logError("GatewayDispatcher : client->gateway current queue size is :" + workThreadExecutor.getQueue().size());
		}

		ClientToGateWorkThread<ClientRequestMessageIf> clientToGateWorkThread = new ClientToGateWorkThread<ClientRequestMessageIf>(gatewayWorkThread, clientRequestMessage);		
		workThreadExecutor.execute(clientToGateWorkThread);
	}
	
	private <T extends ClientRequestMessageIf> void gateToSceneService(SocketActionData socketActionData) throws Exception {
		
		PBInvocation protocol = this.getActionInvocation(PBInvocation.class, socketActionData.getCode());
		
		if(null == protocol){//如果找不到响应需要处理的注解，则直接查找玩家当前对应的scene,将消息发送给响应的scene
			try {
				commonFunction.write2Scene(socketActionData);
			} catch (Exception e) {
				if(gatewayIntercept != null){
					gatewayIntercept.catchGateToSceneException(e, socketActionData);
				} else {
					PubQueueLogUtil.logError("gateToSceneService throw a big exception!!!", e);
				}
			}
		} else {
			ThreadPoolExecutor workThreadExecutor = getGatewayWorkGroup(socketActionData);
			if(workThreadExecutor.getQueue().size() > 1000){
				PubQueueLogUtil.logWarn("GatewayDispatcher : gateway->scene current queue size is :" + workThreadExecutor.getQueue().size());
			}
			GateToSceneWorkThread<ClientRequestMessageIf> gateToSceneWorkThread = new GateToSceneWorkThread<ClientRequestMessageIf>(gatewayWorkThread, socketActionData);		
			workThreadExecutor.execute(gateToSceneWorkThread);
		}
	}
	
	private <T extends ClientRequestMessageIf> void sceneToGateService(SocketActionData socketActionData) throws Exception {

		ThreadPoolExecutor workThreadExecutor = getGatewayWorkGroup(socketActionData);
		
		if(workThreadExecutor.getQueue().size() > 1000){
			PubQueueLogUtil.logWarn("GatewayDispatcher : scene->gateway current queue size is :" + workThreadExecutor.getQueue().size());
		}
		
		SceneToGateWorkThread<ClientRequestMessageIf> sceneToGateWorkThread = new SceneToGateWorkThread<ClientRequestMessageIf>(gatewayWorkThread, socketActionData);		
		workThreadExecutor.execute(sceneToGateWorkThread);
	}
////////////////////////////////////////////////////
	
	public void sceneClientChannelActive(Channel channel, String gatewayId) throws Exception {
		
		byte[] bytes = GSRegist.newBuilder().setGatewayId(gatewayId).build().toByteArray();
		SocketActionData socketActionData = new SocketActionData(0, 0, channel, InnerOpDefineDefault.GSRegistCode, bytes);
		this.gateToSceneService(socketActionData);
	}


	public void sceneClientChannelRead(InnerGateDecodedMessage innerGateDecodedMessage, Channel channel) throws Exception {
		for(FromSceneMessageItem fromSceneMessageItem : innerGateDecodedMessage.getFromSceneMessageList()){
			long playerId = fromSceneMessageItem.getPlayerId();
			int serverId = fromSceneMessageItem.getServerId();
			SocketActionData socketActionData = new SocketActionData(playerId, serverId, channel, fromSceneMessageItem.getCode(),
					fromSceneMessageItem.getBytes());
			this.sceneToGateService(socketActionData);
		}
	}


	public synchronized void sceneClientChannelInactive(int sceneType, String sceneServerId, Channel removedChannel) {
		
		List<GatewayRequestMsgQueueInfo> gatewayRequestMsgQueueInfoList = gatewayRequestMsgQueue.getGatewayRequestMsgQueueInfoList(sceneServerId);
		if(gatewayRequestMsgQueueInfoList != null && !gatewayRequestMsgQueueInfoList.isEmpty()){
			try {
				gatewayRequestMsgQueue.removeGatewayQueue(sceneServerId, removedChannel);
			} catch (Exception e) {
				PubQueueLogUtil.logError("gatewayRequestMsgQueue.removeSceneQueue sceneServerId:" + sceneServerId, e);
			}
			
			//再获取一次  看是否为空了
			gatewayRequestMsgQueueInfoList = gatewayRequestMsgQueue.getGatewayRequestMsgQueueInfoList(sceneServerId);
			//再判断一次，如果这次还不为空  则断掉和这个scene的其他链接
			if(gatewayRequestMsgQueueInfoList != null && !gatewayRequestMsgQueueInfoList.isEmpty()){
				gatewayRequestMsgQueueInfoList.get(0).closeChannel();
			} else {
				try {
					if(gatewayIntercept != null){
						//scene掉线了  一般是scene重启了  那再这个线程中执行  问题不大  不用新开线程了
						gatewayIntercept.onSceneDisconnect(sceneType, sceneServerId, removedChannel);
					}
				} catch (Exception e) {
					PubQueueLogUtil.logError("gatewayIntercept.onSceneDisconnect sceneType:" + sceneType + "; sceneServerId:" + sceneServerId, e);
				}
			}
			
		} else {
			//如果能走到这里  一定是添加的时候漏掉了   或者是  多删除了
			PubQueueLogUtil.logError("gatewayRequestMsgQueue.removeSceneQueue gatewayRequestMsgQueueInfoList is empty!!! sceneServerId:" + sceneServerId + ", removedChannel:" + removedChannel);
		}
	}
}
