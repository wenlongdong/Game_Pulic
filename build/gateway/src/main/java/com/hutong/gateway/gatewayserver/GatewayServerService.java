package com.hutong.gateway.gatewayserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hutong.gateway.GatewayConfig;
import com.hutong.gateway.GatewayOtherConfig;
import com.hutong.gateway.interf.GatewayInterface;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Service
public class GatewayServerService {
	
	/** ? */
	@Autowired
	public GatewayOtherConfig gatewayOtherConfig;
	
	@Autowired
	@Qualifier(value="gatewayInterfaceImpl")
	public GatewayInterface gatewayInterface;

	@Autowired
	private GatewayConfig gatewayConfig;
	
	
	private NioEventLoopGroup acceptorGroup = null;
	private NioEventLoopGroup ioGroup = null;
	
	public ChannelFuture startService() throws Exception {
		
		acceptorGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("Gateway-Group-Acceptor"));
		ioGroup = new NioEventLoopGroup(gatewayOtherConfig.getGateway_server_io_thread_num(), new DefaultThreadFactory("Gateway-Group-IO"));
		
		System.out.println("--------------------------------------");
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(acceptorGroup, ioGroup)
			.channel(NioServerSocketChannel.class)
			
			.option(ChannelOption.SO_BACKLOG, gatewayOtherConfig.getGateway_server_backlog())
			.option(ChannelOption.SO_RCVBUF, gatewayOtherConfig.getGateway_server_rcvbuf() * 1024)
			.option(ChannelOption.SO_REUSEADDR, true)
			.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			
			.childOption(ChannelOption.SO_RCVBUF, gatewayOtherConfig.getGateway_server_rcvbuf() * 1024)
			.childOption(ChannelOption.SO_SNDBUF, gatewayOtherConfig.getGateway_server_sndbuf() * 1024)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			
			.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, gatewayOtherConfig.getGateway_server_writeBufferHighWaterMark() * 1024)
			.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, gatewayOtherConfig.getGateway_server_writeBufferLowWaterMark() * 1024)
			
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					
					ChannelPipeline pipeline = sc.pipeline();
					
					IdleStateHandler idleStateHandler = new IdleStateHandler(gatewayOtherConfig.getGateway_server_reader_idle_time(), 
							gatewayOtherConfig.getGateway_server_writer_idle_time(), gatewayOtherConfig.getGateway_server_all_idle_time());
					
					LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4, true);
					LengthFieldPrepender lengthFieldPrepender = new LengthFieldPrepender(4);
					
					pipeline.addLast(IdleStateHandler.class.getName(), idleStateHandler);
					pipeline.addLast(LengthFieldBasedFrameDecoder.class.getName(), lengthFieldBasedFrameDecoder);
					pipeline.addLast(LengthFieldPrepender.class.getName(), lengthFieldPrepender);
					
					for(MessageToByteEncoder messageToByteEncoder : gatewayInterface.getGateMsgToByteEncoderList()){
						
						pipeline.addLast(messageToByteEncoder.getClass().getName(), messageToByteEncoder);
					}
					for(ByteToMessageDecoder byteToMessageDecoder : gatewayInterface.getGateByteToMsgDecoderList()){
						
						pipeline.addLast(byteToMessageDecoder.getClass().getName(), byteToMessageDecoder);
					}
					
					for(ChannelInboundHandler channelInboundHandler : gatewayInterface.getGateChannelInboundHandlerList()){
						
						pipeline.addLast(channelInboundHandler.getClass().getName(), channelInboundHandler);
					}
				}
			});
		
		ChannelFuture gateWayServerFuture = serverBootstrap.bind(gatewayConfig.getPort()).sync();
		
		return gateWayServerFuture;
	}

	public void stopService(){
		if(null != acceptorGroup){
			acceptorGroup.shutdownGracefully();
			acceptorGroup = null;
		}
		
		if(null != ioGroup){
			ioGroup.shutdownGracefully();
			ioGroup = null;
		}
	}
}
