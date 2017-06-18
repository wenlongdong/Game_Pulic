/**
 * 
 */
package com.hutong.gateway.interf;

import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.hutong.framework.base.dispatch.invocation.PBInvocation;
import com.hutong.socketbase.codec.clientmessage.ClientRequestMessageIf;
import com.hutong.socketbase.codec.innermessage.InnerGateMessageItem;
import com.hutong.socketbase.socketactiondata.SocketActionData;

/**
 * @author DWL
 *
 */
public abstract class GatewayIntercept <T extends ClientRequestMessageIf> {
	
	//获取playerId
	public abstract long queryPlayerId(int opCode, byte[] bytes) throws Exception;
	
	//获取游戏方提供的线程池
	public abstract ThreadPoolExecutor getThreadPoolExecutor(SocketActionData socketActionData);
	
	//玩家断开连接的时候触发
	public abstract void onPlayerDisconnect(int serverId, long playerId) throws Exception;
	
	//scene掉线了  一般是scene重启了
	public abstract void onSceneDisconnect(int sceneType, String sceneServerId, Channel sceneChannel);
	
	//客户端请求发来之前 的拦截逻辑
	public abstract Object beforeClientToGate(T clientRequestMessageIf, PBInvocation protocol) throws Exception;

	public abstract void afterClientToGate(Object responseMessage, Object attachment);

	//client->gateway时发出的异常  也就是客户端请求的异常
	public abstract Object catchClientToGateException(Exception e, T clientRequestMessageIf);
	
	//gateway->scene时发出的异常  也就是转发客户端的消息到scene时抛出异常
	public abstract void catchGateToSceneException(Exception e, SocketActionData socketActionData);
	
	//scene->gateway时发出的异常  也就是scene返回消息给gateway处理时抛出的异常
	public abstract void catchSceneToGateException(Exception e, SocketActionData socketActionData);
	
	//客户端玩家的writable is false
	public abstract void gatewayToClientWritableFalse(Channel channel, SocketActionData socketActionData);
	
	//gateway 到  scene 的 isWritable 为  false
	public abstract void gatewayToSceneWritableFalse(String sceneServerId, List<InnerGateMessageItem> innerGateMessageItemList);

	public abstract void doBuzInCGIO(T crm);
}
