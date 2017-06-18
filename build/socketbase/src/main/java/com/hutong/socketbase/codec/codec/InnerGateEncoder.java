package com.hutong.socketbase.codec.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hutong.socketbase.codec.innermessage.InnerGateMessageItem;
import com.hutong.socketbase.codec.innermessage.InnerGateToSceneMessage;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class InnerGateEncoder extends MessageToByteEncoder<InnerGateToSceneMessage> {
	
	private final Log log = LogFactory.getLog(InnerGateEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, InnerGateToSceneMessage msg, ByteBuf out) throws Exception {

		try {
			if(msg.getInnerGateMessageItemList().isEmpty()){
				log.error("InnerGateEncoder is empty");
				return;
			}
			
			out.writeInt(msg.getInnerGateMessageItemList().size());
			for(InnerGateMessageItem innerGateMessageItem : msg.getInnerGateMessageItemList()){
				
				out.writeInt(innerGateMessageItem.getCode());
				out.writeLong(innerGateMessageItem.getPlayerId());
				out.writeInt(innerGateMessageItem.getServerId());
				out.writeInt(innerGateMessageItem.getLineId());
				byte[] bytes = innerGateMessageItem.getBytes();
				out.writeInt(null == bytes ? 0 : bytes.length);
				if (null != bytes && bytes.length != 0) {
					out.writeBytes(bytes);
				}
			}
		} catch (Exception e) {
			log.error("InnerGateEncoder throw exception", e);
		}
	}
}
	