package com.hutong.socketbase.codec.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hutong.socketbase.codec.innermessage.InnerSceneDecodedMessage;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class InnerSceneDecoder extends ByteToMessageDecoder {

	private final Log log = LogFactory.getLog(InnerSceneDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			if(in.isReadable(4)){
				InnerSceneDecodedMessage innerSceneDecodedMessage = new InnerSceneDecodedMessage();
				int size = in.readInt();
				for(int index=0; index<size; ++index){
					int code = in.readInt();//4
					long pid = in.readLong();//8
					int sid = in.readInt();//4
					int lineId = in.readInt();//4
					int len = in.readInt();//4
					byte[] bytes = new byte[0];
					if (len > 0 && in.isReadable(len)) {
						bytes = new byte[len];
						in.readBytes(bytes, 0, len);
					}
					innerSceneDecodedMessage.addInnerGateMessageItem(code, pid, sid, lineId, bytes);
				}
				out.add(innerSceneDecodedMessage);
			} else {
				try {
					InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
					log.error("can not decode !  readableBytes length: " + in.readableBytes() + " InnerSceneDecoder ip: " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
				} catch (Exception e) {
					log.error("InnerSceneDecoder throw exception!!!", e);
				}
			}
		} catch (Exception e) {
			try {
				InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
				log.error("InnerSceneDecoder throw exception!!!  readableBytes length: " + in.readableBytes() + " InnerSceneDecoder ip: " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
			} catch (Exception e2) {
				log.error("InnerSceneDecoder throw exception!!!", e2);
			}
		}
	}
}
