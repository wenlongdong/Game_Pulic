package com.hutong.scene.dispatcher.actions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.protobuf.GeneratedMessage;
import com.hutong.framework.base.dispatch.annocation.Action;
import com.hutong.framework.base.dispatch.annocation.ProtocolPB;
import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.SceneConfig;
import com.hutong.scene.dispatcher.SceneResponseMsgQueue;
import com.hutong.scene.interf.SceneIntercept;
import com.hutong.socketbase.codec.innermessage.InnerSceneToGateMessage;
import com.hutong.socketbase.message.GSMessage.GSPing;
import com.hutong.socketbase.message.GSMessage.GSPong;
import com.hutong.socketbase.message.GSMessage.GSRegist;
import com.hutong.socketbase.message.GSMessage.GatewayDisconnect;
import com.hutong.socketbase.message.GSMessage.SGPlayerDisconnect;
import com.hutong.socketbase.message.GSMessage.SGPong;
import com.hutong.socketbase.message.GSMessage.SGRegist;
import com.hutong.socketbase.message.GSMessage.SceneUniqeId;
import com.hutong.socketbase.message.InnerOpDefineDefault;
import com.hutong.socketbase.socketactiondata.SceneActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Action
public class SceneAction {
	
	@Autowired
	private SceneConfig sceneConfig;
	
	//当游戏方需要拦截一些请求时  需要实现这个抽象类
	@Autowired(required=false)
	@Qualifier("sceneInterceptImpl")
	private SceneIntercept sceneIntercept;
	
	@Autowired
	private SceneResponseMsgQueue sceneResponseMsgQueue;
	
	
	@ProtocolPB(value = InnerOpDefineDefault.GSGatewayDisconnect)
	public void onGatewayDisconnect(SceneActionData data) throws Exception {
		
		GatewayDisconnect gatewayDisconnect = GatewayDisconnect.parseFrom(data.getBytes());
		String gatewayId = gatewayDisconnect.getGatewayId();
		
		try {
			if(sceneIntercept != null){
				sceneIntercept.onGatewayDisconnect(gatewayId);
			}
		} catch (Exception e) {
			PubQueueLogUtil.logError("onGatewayDisconnect sceneIntercept.onGatewayDisconnect " + gatewayId, e);
		}
	}
	
	
	@ProtocolPB(value = InnerOpDefineDefault.GSPlayerDisconnect)
	public void playerDisconnect(SceneActionData data) throws Exception {
		PubQueueLogUtil.logWarn("playerDisconnect");
		final long playerId = data.getPlayerId();
		int serverId = data.getServerId();
		
		if(sceneIntercept != null){
			sceneIntercept.onPlayerDisconnect(data);
		}
		
		SGPlayerDisconnect.Builder sgPlayerDisconnect = SGPlayerDisconnect.newBuilder();
		
		sgPlayerDisconnect.setResult(InnerOpDefineDefault.SGSUCCESS);
		sgPlayerDisconnect.setPlayerId(playerId);
		sgPlayerDisconnect.setServerId(serverId);
		
		SceneUniqeId.Builder sceneUniqeId = SceneUniqeId.newBuilder();
		sceneUniqeId.setSceneType(sceneConfig.getSceneType());
		sceneUniqeId.setSceneId(sceneConfig.getSceneServerId());
		sgPlayerDisconnect.setSceneUniqeId(sceneUniqeId);
		
		SceneActionData retSceneActionData = new SceneActionData(0, playerId, serverId, null, InnerOpDefineDefault.SGPlayerDisconnect, sgPlayerDisconnect.build().toByteArray());
		
		InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();
		innerSceneToGateMessage.addSceneActionData(retSceneActionData);
		
		data.getChannel().writeAndFlush(innerSceneToGateMessage);
	}
	
	@ProtocolPB(value = InnerOpDefineDefault.GSPingCode)
	public void gsPingMsg(SceneActionData data) throws Exception {
		
		GSPing gsPing = GSPing.parseFrom(data.getBytes());
		PubQueueLogUtil.logDebug("GSPing MSG : gateId is " + gsPing.getGatewayId() + " and gateping num is " + gsPing.getPingNum());
		
		SceneUniqeId.Builder sceneUniqeId = SceneUniqeId.newBuilder();
		sceneUniqeId.setSceneType(sceneConfig.getSceneType());
		sceneUniqeId.setSceneId(sceneConfig.getSceneServerId());
		
		SGPong.Builder sgPong = SGPong.newBuilder();
		sgPong.setPongNum(100);
		sgPong.setSceneUniqeId(sceneUniqeId.build());
		
		SceneActionData sceneActionData = new SceneActionData(0, 0, 0, null, InnerOpDefineDefault.SGPongCode, sgPong.build().toByteArray());
		
		InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();
		innerSceneToGateMessage.addSceneActionData(sceneActionData);
		
		data.getChannel().writeAndFlush(innerSceneToGateMessage);
	}

	
	@ProtocolPB(value = InnerOpDefineDefault.GSPongCode)
	public void gsPongMsg(SceneActionData data) throws Exception {
		
		GSPong gsPong = GSPong.parseFrom(data.getBytes());
		PubQueueLogUtil.logDebug("GSPong MSG : gateId is " + gsPong.getGatewayId() + " and gatepong num is " + gsPong.getPongNum());
	}

	
	
	@ProtocolPB(value = InnerOpDefineDefault.GSRegistCode)
	public void gatewayRegist(SceneActionData data) throws Exception {
		
		Channel channel = data.getChannel();
		
		if (ipCheck(channel)) { // ip允许
			
			GSRegist gsRegist = GSRegist.parseFrom(data.getBytes());
			
			String gatewayId = gsRegist.getGatewayId();
			
			sceneResponseMsgQueue.addSceneQueue(gatewayId, channel);
			
			SGRegist.Builder sgRegist = SGRegist.newBuilder();
			
			SceneUniqeId.Builder sceneUniqeId = SceneUniqeId.newBuilder();
			sceneUniqeId.setSceneType(sceneConfig.getSceneType());
			sceneUniqeId.setSceneId(sceneConfig.getSceneServerId());
			
			sgRegist.addSceneUniqeId(sceneUniqeId.build());
			sgRegist.setResult(InnerOpDefineDefault.SGSUCCESS);
			
			SceneActionData sceneActionData = new SceneActionData(0, 0, 0, null, InnerOpDefineDefault.SGRegistCode, sgRegist.build().toByteArray());
			
			InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();
			innerSceneToGateMessage.addSceneActionData(sceneActionData);
			channel.writeAndFlush(innerSceneToGateMessage);
			
			//因为现在都是一定成功的  所以不需要下面的判断了
			/*if(bAddSucc){
				
				sceneResponseMsgQueue.addSceneQueue(gatewayId);
				
				sgRegist.setResult(InnerOpDefineDefault.SGSUCCESS);
				
				SceneActionData sceneActionData = new SceneActionData(0, 0, 0, null, InnerOpDefineDefault.SGRegistCode, sgRegist.build().toByteArray());
				
				InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();
				innerSceneToGateMessage.addSceneActionData(sceneActionData);
				channel.writeAndFlush(innerSceneToGateMessage);
			} else {//没有添加成功，则需要返回错误，并关掉相关channel
				sgRegist.setResult(InnerOpDefineDefault.SGFAILED);
				SceneActionData sceneActionData = new SceneActionData(0, 0, 0, null, InnerOpDefineDefault.SGRegistCode, sgRegist.build().toByteArray());
				InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();
				innerSceneToGateMessage.addSceneActionData(sceneActionData);
				channel.writeAndFlush(innerSceneToGateMessage).addListener(ChannelFutureListener.CLOSE);
			}*/
		} else {
			
			GeneratedMessage pbMessage = SGRegist.newBuilder().setResult(-100).build();
			SceneActionData sceneActionData = new SceneActionData(0, 0, 0, null, InnerOpDefineDefault.SGRegistCode, pbMessage.toByteArray());
			InnerSceneToGateMessage innerSceneToGateMessage = new InnerSceneToGateMessage();
			innerSceneToGateMessage.addSceneActionData(sceneActionData);
			PubQueueLogUtil.logDebug("[SceneHandler] wfcNow channel=<" + channel.toString() + "> message=<" + innerSceneToGateMessage.toString() + "> IP is Invalid!!! FAILED!!!");
			channel.writeAndFlush(innerSceneToGateMessage).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private boolean ipCheck(Channel channel){
		return true;
	}
}
