/**
 * 
 */
package com.hutong.gateway.gatewayserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.springframework.beans.factory.annotation.Autowired;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.gateway.dispatcher.GateCommonFunction;
import com.hutong.gateway.dispatcher.GatewayDispatcher;
import com.hutong.socketbase.codec.clientmessage.ClientRequestMessageIf;
import com.hutong.socketbase.define.PlayerUniqueKey;
import com.hutong.socketbase.message.InnerOpDefineDefault;
import com.hutong.socketbase.sessionbase.GatewaySession;
import com.hutong.socketbase.socketactiondata.SocketActionData;

/**
 * @author DWL
 *
 */
public class GatewayServerHandlerBase<T extends ClientRequestMessageIf> extends SimpleChannelInboundHandler<T> {

	@Autowired
	private GatewayDispatcher gatewayDispatcher;
	
	@Autowired
	private GatewayServerData<GatewaySession> gatewayServerData;
	
	@Autowired
	private GateCommonFunction commonFunction;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, T clientRequestMessage) throws Exception {
		
		gatewayDispatcher.clientToGateService(clientRequestMessage);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		PlayerUniqueKey playerUniqueKey = gatewayServerData.getPlayerUniqueKeyByChannel(ctx.channel());
		if(playerUniqueKey == null){
			PubQueueLogUtil.logWarn("channelInactive playerUniqueKey is null");
		} else {
			PubQueueLogUtil.logWarn("channelInactive playerUniqueKey is " + playerUniqueKey);
			
			SocketActionData socketActionData = new SocketActionData(playerUniqueKey.getPlayerId(), playerUniqueKey.getServerId(), 
					ctx.channel(), InnerOpDefineDefault.GSPlayerDisconnect, null);
			
			gatewayDispatcher.getGatewayWorkGroup(socketActionData).execute(new ClientChannelInactive(commonFunction, playerUniqueKey));
		}
	}
	
	
	
	public static class ClientChannelInactive extends Thread {

		private GateCommonFunction commonFunction;
		private PlayerUniqueKey playerUniqueKey;
		
		public ClientChannelInactive(GateCommonFunction commonFunction, PlayerUniqueKey playerUniqueKey) {
			this.commonFunction = commonFunction;
			this.playerUniqueKey = playerUniqueKey;
		}
		
		@Override
		public void run() {

			long startTime = System.currentTimeMillis();
			
			commonFunction.freeGatewaySession(playerUniqueKey);
			
			long endTime = System.currentTimeMillis();
			
			if(endTime - startTime > 10){
				PubQueueLogUtil.logError("GatewayWorkThread -> ClientChannelInactive take too long time!!!, costTime is " + (endTime-startTime) 
						+ " ; And PlayerUniqueKey is " + playerUniqueKey.toString());
			}
		}
	}
	
	
	
	
	
	
	
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		PubQueueLogUtil.logError("channel throw exception : " + ctx.channel(), cause);
		cause.printStackTrace();
		ctx.close();
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				PubQueueLogUtil.logError("SceneServerHandler READ IDLE and release client channel : ");
				ctx.close();
			} else if (event.state() == IdleState.WRITER_IDLE) {
				PubQueueLogUtil.logError("SceneServerHandler WRITE IDLE");
			} else if (event.state() == IdleState.ALL_IDLE) {
				PubQueueLogUtil.logError("SceneServerHandler ALL IDLE and release client channel : " + ctx.channel().remoteAddress());
				ctx.close();
			}
		}
	}
}
