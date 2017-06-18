package com.hutong.scene.dispatcher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hutong.framework.base.dispatch.invocation.InvocationFactory;
import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.framework.util.SpringObjectFactory;
import com.hutong.framework.web.dispatch.WebDispatcher;
import com.hutong.framework.web.dispatch.invocation.URIInvocation;
import com.hutong.framework.web.dispatch.invocation.URIInvocationFactory;
import com.hutong.scene.SceneOtherConfig;
import com.hutong.scene.interf.ISceneHttpIntercept;
import com.hutong.socketbase.codec.httpmessage.HttpRequestMessage;
import com.hutong.socketbase.codec.httpmessage.HttpResponseMessage;

/**
 * @author Jay
 * @description ?
 * 
 */
@Service
public class SceneHttpDispatcher extends WebDispatcher {
	
	@Autowired
	private SpringObjectFactory springObjectFactory;
	
	@Autowired
	@Qualifier(value="webActionPath")
	private String webActionPath;
	
	@Autowired
	private SceneOtherConfig sceneOtherConfig;
	
	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("sceneHttpIntercept")
	private ISceneHttpIntercept sceneHttpIntercept;
	
	//netty工作线程池，用来处理玩家的实际业务需求
	public ExecutorService workGroup;
	
	public void init () throws Exception {
		if(sceneOtherConfig.getScene_http_use() != 1){
			return;
		}
		this.initHandleAction(webActionPath, springObjectFactory);
		workGroup = Executors.newFixedThreadPool(sceneOtherConfig.getScene_http_work_group_num(), new DefaultThreadFactory("Scene-http-WorkGroup"));
	}
	
	public void service(HttpRequestMessage request, Channel channel) throws Exception {
		try {
			WorkThread workThread = new WorkThread(request, channel);		
			this.workGroup.execute(workThread);
		} catch (Exception e) {
			PubQueueLogUtil.logError("SceneDispatcher service throw a big exception!!!", e);
		}
	}
	
	private void processor(HttpRequestMessage request, Channel channel) {
		try{
			SceneHttpParam param = sceneHttpIntercept.beforeSceneExecute(request);
			String actionStr = param.getAction();
			Map<String, String> params = param.getParams();
			int result = 0;
			
			URIInvocation action = this.getActionInvocation(URIInvocation.class, actionStr);
			HttpResponseMessage response;
			if (null != action) {
				Object invoke = null;
				if (MapUtils.isEmpty(params)) {
					invoke = action.invoke();
				} else {
					invoke = action.invoke(params);
				}
				response = new HttpResponseMessage();
				response.setContent(invoke.toString());
				channel.writeAndFlush(response);
				result = 1;
			} else {
				PubQueueLogUtil.logError(" no ActionHTTPInvocation found by uri : " + actionStr);
				response = new HttpResponseMessage();
				response.setContent("no action");
				result = 2;
			}
			channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			sceneHttpIntercept.afterSceneExecute(request, result);
		}catch(Throwable e){
			sceneHttpIntercept.catchExecuteException(request);
			PubQueueLogUtil.logError("处理http请求异常", e);
			HttpResponseMessage response = new HttpResponseMessage();
			response.setContent("exception!!!");
			channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private class WorkThread extends Thread {

		private final Log LOG = LogFactory.getLog(WorkThread.class);
		private HttpRequestMessage request;
		private Channel channel;
		
		public WorkThread(HttpRequestMessage request, Channel channel) {
			this.request = request;
			this.channel = channel;
		}
		
		@Override
		public void run() {
			try {
				processor(request, channel);
			} catch (Throwable e) {
				LOG.error("", e);
			}
		}
	}
	
	/**
	 * 重写方法，这个分发器只会检测web相关action
	 */
	protected Map<Class<?> ,InvocationFactory<?>> getActionFactories(){
		Map<Class<?> ,InvocationFactory<?>> maps = new HashMap<Class<?> ,InvocationFactory<?>>();
		maps.put(URIInvocation.class, new URIInvocationFactory());
		return maps;
	}
	
	public SceneHttpParam newSceneHttpParam(String action, Map<String, String> params){
		return new SceneHttpParam(action, params);
	}
	
	public static class SceneHttpParam{
		
		private String action;
		private Map<String, String> params;
		
		public SceneHttpParam(String action, Map<String, String> params) {
			this.action = action;
			this.params = params;
		}
		
		public String getAction() {
			return action;
		}
		
		public void setAction(String action) {
			this.action = action;
		}
		
		public Map<String, String> getParams() {
			return params;
		}
		
		public void setParams(Map<String, String> params) {
			this.params = params;
		}
	}
}
