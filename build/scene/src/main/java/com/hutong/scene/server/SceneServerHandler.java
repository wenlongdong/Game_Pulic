package com.hutong.scene.server;

import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.SceneConfig;
import com.hutong.scene.dispatcher.SceneDispatcher;
import com.hutong.scene.dispatcher.SceneResponseMsgQueue;
import com.hutong.scene.dispatcher.SceneResponseMsgQueue.ScenePushMsgQueueInfo;
import com.hutong.socketbase.codec.innermessage.InnerSceneDecodedMessage;
import com.hutong.socketbase.codec.innermessage.InnerSceneToGateMessage;
import com.hutong.socketbase.message.GSMessage.GatewayDisconnect;
import com.hutong.socketbase.message.GSMessage.SGPing;
import com.hutong.socketbase.message.GSMessage.SceneUniqeId;
import com.hutong.socketbase.message.InnerOpDefineDefault;
import com.hutong.socketbase.sessionbase.SceneSession;
import com.hutong.socketbase.socketactiondata.SceneActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Sharable
@Component
public class SceneServerHandler extends SimpleChannelInboundHandler<InnerSceneDecodedMessage> {

	@Autowired
	private SceneConfig sceneConfig;
	
	@Autowired
	private SceneDispatcher sceneDispatcher;

	@Autowired
	private SceneServerData<SceneSession> sceneServerData;
	
	@Autowired
	private SceneResponseMsgQueue sceneResponseMsgQueue;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InnerSceneDecodedMessage innerSceneDecodedMessage) throws Exception {
		Channel channel = ctx.channel();
		sceneDispatcher.service(innerSceneDecodedMessage, channel);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
	@Override
	public synchronized void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		GatewayInfo gatewayInfo = sceneServerData.getGatewayInfoByChannel(ctx.channel());
		if(gatewayInfo != null){
			
			List<ScenePushMsgQueueInfo> scenePushMsgQueueInfoList = sceneResponseMsgQueue.getScenePushMsgQueueInfoList(gatewayInfo.getGatewayId());
			if(scenePushMsgQueueInfoList != null && !scenePushMsgQueueInfoList.isEmpty()){
				
				try {
					sceneResponseMsgQueue.removeSceneQueue(gatewayInfo.getGatewayId(), ctx.channel());
				} catch (Exception e) {
					PubQueueLogUtil.logError("sceneResponseMsgQueue.removeGatewayQueue gatewayId:" + gatewayInfo.getGatewayId(), e);
				}
				
				//再获取一次  看是否为空了
				scenePushMsgQueueInfoList = sceneResponseMsgQueue.getScenePushMsgQueueInfoList(gatewayInfo.getGatewayId());
				//再判断一次，如果这次还不为空  则断掉和这个gatewayId的其他链接
				if(scenePushMsgQueueInfoList != null && !scenePushMsgQueueInfoList.isEmpty()) {
					scenePushMsgQueueInfoList.get(0).closeChannel();
				} else {
					try {
						//这里也统一处理了  走消息机制
						GatewayDisconnect.Builder gatewayDisconnect = GatewayDisconnect.newBuilder();
						gatewayDisconnect.setGatewayId(gatewayInfo.getGatewayId());
						
						InnerSceneDecodedMessage innerSceneDecodedMessage = new InnerSceneDecodedMessage();
						innerSceneDecodedMessage.addInnerGateMessageItem(InnerOpDefineDefault.GSGatewayDisconnect, 0, 0, 0, gatewayDisconnect.build().toByteArray());
						
						sceneDispatcher.service(innerSceneDecodedMessage, ctx.channel());
					} catch (Exception e) {
						PubQueueLogUtil.logError("SceneServerHandler.channelInactive gatewayId:" + gatewayInfo.getGatewayId() + "; removed channel:" + ctx.channel(), e);
					}
				}
			} else {
				//如果能走到这里  一定是添加的时候漏掉了   或者是  多删除了   或者是   外挂？？
				PubQueueLogUtil.logError("SceneServerHandler.channelInactive SceneResponseMsgQueueInfoList is empty!!! gatewayId:" + gatewayInfo.getGatewayId() + ", removedChannel:" + ctx.channel());
			}
		} else {
			PubQueueLogUtil.logError("scene channelInactive and find gatewayInfo is null !!!");
		}
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
				
				SceneUniqeId.Builder sceneUniqeId = SceneUniqeId.newBuilder();
				sceneUniqeId.setSceneType(sceneConfig.getSceneType());
				sceneUniqeId.setSceneId(sceneConfig.getSceneServerId());
				
				SGPing.Builder sgPing = SGPing.newBuilder();
				sgPing.setPingNum(100);
				sgPing.setSceneUniqeId(sceneUniqeId.build());
				
				SceneActionData sceneActionData = new SceneActionData(0, 0, 0, null, InnerOpDefineDefault.SGPingCode, sgPing.build().toByteArray());
				
				InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();
				innerSceneToGateMessage.addSceneActionData(sceneActionData);
				
				ctx.writeAndFlush(innerSceneToGateMessage);
			}
		}
	}
}
