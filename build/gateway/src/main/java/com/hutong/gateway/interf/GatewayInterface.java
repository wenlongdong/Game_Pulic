/**
 * 
 */
package com.hutong.gateway.interf;

import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

import com.hutong.socketbase.codec.clientmessage.ClientResponseMessageIf;

/**
 * @author DWL
 *
 */
public interface GatewayInterface {

	//获取gate的编解码handler
	public <T> List<MessageToByteEncoder<T>> getGateMsgToByteEncoderList();
	public List<ByteToMessageDecoder> getGateByteToMsgDecoderList();
	
	//gate提供的handler
	public List<ChannelInboundHandler> getGateChannelInboundHandlerList();
	
	//从actionData转换成客户端的返回信息
	public ClientResponseMessageIf genResponseMsgWith(long playerId, int serverId, int code, byte[] bytes);
}
