package com.hutong.scene.interf;

import com.hutong.scene.dispatcher.SceneHttpDispatcher;
import com.hutong.socketbase.codec.httpmessage.HttpRequestMessage;

public interface ISceneHttpIntercept {
	//开始执行前的事件
	public SceneHttpDispatcher.SceneHttpParam beforeSceneExecute(HttpRequestMessage request) throws Throwable;
	
	//执行后的事件
	public void afterSceneExecute(HttpRequestMessage request, int result) throws Throwable;

	//异常捕获函数
	public void catchExecuteException(HttpRequestMessage request);
}
