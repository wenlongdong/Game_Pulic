package com.hutong.scene.server.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hutong.scene.dispatcher.SceneHttpDispatcher;
import com.hutong.socketbase.codec.httpmessage.HttpRequestMessage;

/**
 * @author Jay
 * @description ?
 * 
 */
@Sharable
@Component
public class SceneHttpServerHandler extends SimpleChannelInboundHandler<HttpRequestMessage> {

	@Autowired
	private SceneHttpDispatcher sceneHttpDispatcher;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpRequestMessage innerGateToSceneMessage) throws Exception {
		Channel channel = ctx.channel();
		sceneHttpDispatcher.service(innerGateToSceneMessage, channel);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
