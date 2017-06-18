package com.hutong.gateway.sceneclient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.gateway.dispatcher.GatewayDispatcher;
import com.hutong.socketbase.codec.innermessage.InnerGateDecodedMessage;
import com.hutong.socketbase.codec.innermessage.InnerGateToSceneMessage;
import com.hutong.socketbase.message.GSMessage.GSPing;
import com.hutong.socketbase.message.InnerOpDefineDefault;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class SceneClientHandler extends SimpleChannelInboundHandler<InnerGateDecodedMessage> {

	/** ? */
	private String gatewayId;
	
	/** ? */
	private String sceneServerId;
	
	/** ? */
	private int sceneType;
	
	/** ? */
	private GatewayDispatcher gatewayDispatcher;

	public SceneClientHandler(int sceneType, String sceneServerId, String gatewayId, GatewayDispatcher gatewayDispatcher) {
		this.sceneType = sceneType;
		this.sceneServerId = sceneServerId;
		this.gatewayId = gatewayId;
		this.gatewayDispatcher = gatewayDispatcher;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		gatewayDispatcher.sceneClientChannelActive(ctx.channel(), gatewayId);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InnerGateDecodedMessage innerGateDecodedMessage) throws Exception {
		Channel channel = ctx.channel();
		gatewayDispatcher.sceneClientChannelRead(innerGateDecodedMessage, channel);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		gatewayDispatcher.sceneClientChannelInactive(sceneType, sceneServerId, ctx.channel());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				PubQueueLogUtil.logDebug("SceneServerHandler read idle");
			} else if (event.state() == IdleState.WRITER_IDLE) {
				PubQueueLogUtil.logDebug("SceneServerHandler write idle");
			} else if (event.state() == IdleState.ALL_IDLE) {
				
				PubQueueLogUtil.logDebug("<><><>SceneServerHandler all idle and begin send scene ping msg<><><>");
				
				GSPing.Builder gsPing = GSPing.newBuilder();
				gsPing.setPingNum(100);
				gsPing.setGatewayId(gatewayId);

				InnerGateToSceneMessage innerGateToSceneMessage = new InnerGateToSceneMessage();
				innerGateToSceneMessage.addInnerGateMessageItem(InnerOpDefineDefault.GSPingCode, 0, 0, 0, gsPing.build().toByteArray());
				
				ctx.writeAndFlush(innerGateToSceneMessage);
			}
		}
	}
}
