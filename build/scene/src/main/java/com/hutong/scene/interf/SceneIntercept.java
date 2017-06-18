package com.hutong.scene.interf;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.hutong.framework.base.dispatch.invocation.PBInvocation;
import com.hutong.socketbase.codec.innermessage.InnerGateMessageItem;
import com.hutong.socketbase.socketactiondata.SceneActionData;

public abstract class SceneIntercept {

	//获取游戏方提供的线程池
	public abstract ThreadPoolExecutor getThreadPoolExecutor(SceneActionData sceneActionData);
	
	//开始执行前的事件
	public abstract Object beforeSceneExecute(SceneActionData sceneActionData, PBInvocation protocol);
	
	//执行后的事件
	public abstract void afterSceneExecute(SceneActionData responseMessage, Object attachment);
	public abstract void afterSceneExecute(SceneActionData[] responseMessages, Object attachment);

	//异常捕获函数
	public abstract Object catchExecuteException(Exception e, SceneActionData sceneActionData);
	
	//scene主动发送的数据的日志
	public abstract void sceneResponseData(SceneActionData sceneActionData);
	
	//玩家掉线时接收到消息通知
	public abstract void onPlayerDisconnect(SceneActionData sceneActionData);
	
	//gateway掉线了  一般是gateway重启了
	public abstract void onGatewayDisconnect(String gatewayId);
	
	//scene 到  gateway 的 isWritable 为  false
	public abstract void sceneToGatewayWritableFalse(String gatewayId, List<SceneActionData> sceneActionDataList);
	
	public abstract void doBuzInSceneIO(InnerGateMessageItem innerGateMessageItem);
	
}
