package com.hutong.socketbase.codec.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hutong.socketbase.codec.innermessage.InnerGateDecodedMessage;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class InnerGateDecoder extends ByteToMessageDecoder {

	private final Log log = LogFactory.getLog(InnerGateDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		try {
			if(in.isReadable(4)){
				
				InnerGateDecodedMessage innerGateDecodedMessage = new InnerGateDecodedMessage();
				int size = in.readInt();
				for(int index=0; index<size; ++index){
					int code = in.readInt();
					long pid = in.readLong();
					int sid = in.readInt();
					
					int len = in.readInt();
					byte[] bytes = new byte[0];
					if (len > 0 && in.isReadable(len)) {
						bytes = new byte[len];
						in.readBytes(bytes, 0, len);
					}
					innerGateDecodedMessage.addFromSceneMessageItem(code, pid, sid, bytes);
				}
				out.add(innerGateDecodedMessage);
			} else {
				try {
					InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
					log.error("can not decode !  readableBytes length: " + in.readableBytes() + " InnerGateDecoder ip: " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
				} catch (Exception e) {
					log.error("InnerGateDecoder throw exception!!!", e);
				}
			}
		} catch (Exception e) {
			try {
				InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
				log.error("InnerGateDecoder throw exception!!!  readableBytes length: " + in.readableBytes() + " InnerGateDecoder ip: " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
			} catch (Exception e2) {
				log.error("InnerGateDecoder throw exception!!!", e2);
			}
		}
	}
}
