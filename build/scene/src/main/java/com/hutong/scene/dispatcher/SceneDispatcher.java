package com.hutong.scene.dispatcher;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hutong.framework.base.dispatch.invocation.PBInvocation;
import com.hutong.framework.soket.dispatch.SocketDispatcher;
import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.framework.util.SpringObjectFactory;
import com.hutong.scene.SceneConfig;
import com.hutong.scene.interf.SceneIntercept;
import com.hutong.scene.server.SceneServerData;
import com.hutong.socketbase.codec.innermessage.InnerGateMessageItem;
import com.hutong.socketbase.codec.innermessage.InnerSceneDecodedMessage;
import com.hutong.socketbase.sessionbase.SceneSession;
import com.hutong.socketbase.socketactiondata.SceneActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Service
public class SceneDispatcher extends SocketDispatcher {
	
	@Autowired
	private SpringObjectFactory springObjectFactory;
	
	@Autowired
	@Qualifier(value="innerActionPath")
	private String innerActionPath; //scene内部自己需要处理的一些action

	@Autowired
	@Qualifier(value="actionPath")
	private String actionPath;//外部框架使用的actionpath
	
	@Autowired
	private SceneConfig sceneConfig;
	
	@Autowired
	private SceneServerData<SceneSession> sceneServerData;
	
	@Autowired
	private SceneCommonFunction sceneCommonFunction;

	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("sceneInterceptImpl")
	private SceneIntercept sceneIntercept;
	
	//netty工作线程池，用来处理玩家的实际业务需求
	private ThreadPoolExecutor[] sceneWorkGroupAry;
	
	private ThreadPoolExecutor sceneDefaultWorkGroup;
	
	public void init() throws Exception {
		this.initHandleAction(innerActionPath, springObjectFactory);
		this.initHandleAction(actionPath, springObjectFactory);
		
		sceneWorkGroupAry = new ThreadPoolExecutor[sceneConfig.getSceneWorkGroupNum()];
		for(int num=0; num<sceneWorkGroupAry.length; ++num){
			sceneWorkGroupAry[num] = (ThreadPoolExecutor)Executors.newFixedThreadPool(1, new DefaultThreadFactory("Scene-WorkGroup-" + num));
		}
		
		sceneDefaultWorkGroup = (ThreadPoolExecutor)Executors.newFixedThreadPool(8, new DefaultThreadFactory("Scene-WorkGroup-Default"));
	}
	
	public ThreadPoolExecutor getSceneWorkGroup(long playerId){
		
		if(playerId <= 0){
			return sceneDefaultWorkGroup;
		} else {
			int index = (int)(playerId % sceneWorkGroupAry.length);
			return sceneWorkGroupAry[index]; 
		}
	}
	
	public void service(InnerSceneDecodedMessage innerSceneDecodedMessage, Channel channel) throws Exception {
		
		for(InnerGateMessageItem innerGateMessageItem : innerSceneDecodedMessage.getInnerGateMessageItemList()){
			try {
				int lineId = innerGateMessageItem.getLineId();
				long playerId = innerGateMessageItem.getPlayerId();
				int serverId = innerGateMessageItem.getServerId();
				SceneActionData sceneActionData = new SceneActionData(lineId, playerId, serverId, channel, innerGateMessageItem.getCode(),
						innerGateMessageItem.getBytes());
				
				WorkThread workThread = new WorkThread(this, sceneActionData);		

				ThreadPoolExecutor workThreadExecutor = null;
				if(sceneIntercept != null){
					workThreadExecutor = sceneIntercept.getThreadPoolExecutor(sceneActionData);
				}
				
				if(workThreadExecutor == null){//在scene默认的线程池里面执行
					workThreadExecutor = getSceneWorkGroup(playerId);
				} 
					
				if(workThreadExecutor.getQueue().size() > 1000){
					PubQueueLogUtil.logError("SceneDispatcher : Scene current queue size is :" + workThreadExecutor.getQueue().size());
				}
				workThreadExecutor.execute(workThread);
				
			} catch (Exception e) {
				PubQueueLogUtil.logError("SceneDispatcher service throw a big exception!!!", e);
			}
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////
	private void service(SceneActionData sceneActionData) throws Exception {
		
		PBInvocation protocol = this.getActionInvocation(PBInvocation.class, sceneActionData.getCode());
		
		Object attachMent = null;
		
		if(sceneIntercept != null && sceneActionData.getCode() >= 10000){
			attachMent = sceneIntercept.beforeSceneExecute(sceneActionData, protocol);
		}
		
		Object resultObj = null;
		try {
			if (null != protocol) {
				resultObj = protocol.invoke(sceneActionData);
			} else {
				PubQueueLogUtil.logWarn(" not found invocation by code = " + sceneActionData.getCode() + " [" + sceneActionData.toString() + "]");
			}
		} catch (Exception e) {
			if(sceneIntercept != null && sceneActionData.getCode() >= 10000){
				resultObj = sceneIntercept.catchExecuteException(e, sceneActionData);
			} else {
				PubQueueLogUtil.logError("scene dispatch service throw exception!!", e);
			}
		} finally {
				
			//因为一般的scene action不会通过返回值来反馈消息，所以这里其实不必要存在responseMessage
			//这里responseMessage存在的主要原因是  在抛出异常的情况下，resultObj会是customException,打印出异常信息
			if(resultObj instanceof SceneActionData){
				SceneActionData responseMessage = (SceneActionData)resultObj;
				sceneCommonFunction.write2Gate(responseMessage);
				if(sceneIntercept != null && sceneActionData.getCode() >= 10000){
					sceneIntercept.afterSceneExecute(responseMessage, attachMent);
				}
			} else if (resultObj instanceof SceneActionData[]){
				SceneActionData[] responseMessages = (SceneActionData[])resultObj;
				for (SceneActionData responseMessage : responseMessages) {
					sceneCommonFunction.write2Gate(responseMessage);
				}
				if(sceneIntercept != null && sceneActionData.getCode() >= 10000){
					sceneIntercept.afterSceneExecute(responseMessages, attachMent);
				}
			} else {
				if(sceneIntercept != null && sceneActionData.getCode() >= 10000){
					sceneIntercept.afterSceneExecute((SceneActionData)null, attachMent);
				}
			}
		}
	}

	
	private static class WorkThread extends Thread {

		private SceneDispatcher dispatcher;
		private SceneActionData sceneActionData;
		
		public WorkThread(SceneDispatcher dispatcher, SceneActionData sceneActionData) {
			this.dispatcher = dispatcher;
			this.sceneActionData = sceneActionData;
		}
		
		@Override
		public void run() {
			try {
				dispatcher.service(sceneActionData);
			} catch (Throwable e) {
				PubQueueLogUtil.logError("SceneDispatcher WorkThread", e);
			}
		}
	}
}
