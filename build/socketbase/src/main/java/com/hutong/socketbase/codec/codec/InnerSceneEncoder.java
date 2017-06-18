package com.hutong.socketbase.codec.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hutong.socketbase.codec.innermessage.InnerSceneToGateMessage;
import com.hutong.socketbase.socketactiondata.SceneActionData;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class InnerSceneEncoder extends MessageToByteEncoder<InnerSceneToGateMessage> {
	private final Log log = LogFactory.getLog(InnerSceneEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, InnerSceneToGateMessage msg, ByteBuf out) throws Exception {
		
		try {
			if(msg.getSceneActionDataList().isEmpty()){
				log.error("InnerSceneToGateMessage is empty");
				return;
			}
			
			out.writeInt(msg.getSceneActionDataList().size());
			
			for(SceneActionData sceneActionData : msg.getSceneActionDataList()){
				out.writeInt(sceneActionData.getCode());
				out.writeLong(sceneActionData.getPlayerId());
				out.writeInt(sceneActionData.getServerId());
				
				byte[] bytes = sceneActionData.getBytes();
				out.writeInt(null == bytes ? 0 : bytes.length);
				if (null != bytes && bytes.length != 0) {
					out.writeBytes(bytes);
				}
			}
		} catch (Exception e) {
			log.error("InnerSceneEncoder throw exception", e);
		}
	}
}
	