package com.hutong.scene.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.SceneConfig;
import com.hutong.scene.SceneOtherConfig;
import com.hutong.socketbase.codec.codec.CommonHttpCodec;
import com.hutong.socketbase.codec.codec.IpAcceptFilter;

@Service
public class SceneHttpServerService {

	@Autowired
	private SceneOtherConfig sceneOtherConfig;
	
	@Autowired
	private SceneHttpServerHandler sceneHttpServerHandler;
	
	@Autowired
	private SceneConfig sceneConfig;
	
	NioEventLoopGroup acceptorGroup;
	NioEventLoopGroup ioGroup;
    private Channel channel;
	
	public void startService() throws InterruptedException {
		
		PubQueueLogUtil.logInfo("SceneHttpServerService startService start");
		
		if(sceneOtherConfig.getScene_http_use() != 1){
			PubQueueLogUtil.logInfo("SceneHttpServerService startService start");
			return;
		}
		
		acceptorGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("Scene-http-Group-Acceptor"));
		ioGroup = new NioEventLoopGroup(sceneOtherConfig.getScene_http_io_group_num(), new DefaultThreadFactory("Scene-http-Group-IO"));

		String ipFilterStr = sceneOtherConfig.getScene_http_ipFilter();
		if(ipFilterStr == null || ipFilterStr.length() <= 0){
			PubQueueLogUtil.logError(">>>>>>>>>>>>>http 服务启动失败，原因白名单过滤器未配置-" + ipFilterStr);
			return;
		}
		String[] ipFilterArr = ipFilterStr.split(",");
		IpFilterRule[] rules = new IpFilterRule[ipFilterArr.length];
		for(int i = 0; i < ipFilterArr.length; i++){
			String ip = ipFilterArr[i];
			rules[i] = new IpSubnetFilterRule(ip, 32, IpFilterRuleType.ACCEPT);
		}
		final IpAcceptFilter ipAcceptFilter = new IpAcceptFilter(rules);
		
		try{
			
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(acceptorGroup, ioGroup)
							.channel(NioServerSocketChannel.class)
							
							.option(ChannelOption.SO_RCVBUF, sceneOtherConfig.getScene_rcvbuf() * 1024)
							.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
							.option(ChannelOption.SO_REUSEADDR, true)
							
							.childOption(ChannelOption.SO_RCVBUF, sceneOtherConfig.getScene_rcvbuf() * 1024)
							.childOption(ChannelOption.SO_SNDBUF, sceneOtherConfig.getScene_sndbuf() * 1024)
							.childOption(ChannelOption.TCP_NODELAY, true)
							.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
							
							.handler(new LoggingHandler(LogLevel.INFO))
							.childHandler(new ChannelInitializer<SocketChannel>() {
								@Override
								protected void initChannel(SocketChannel sc) throws Exception {
									ChannelPipeline pipeline = sc.pipeline();
									//白名单
									pipeline.addLast(IpAcceptFilter.class.getSimpleName(), ipAcceptFilter);
									//访问超时关闭
									pipeline.addLast(ReadTimeoutHandler.class.getSimpleName(), new ReadTimeoutHandler(10));
									pipeline.addLast(WriteTimeoutHandler.class.getSimpleName(), new WriteTimeoutHandler(10));
									//访问最大内容缓冲
									pipeline.addLast(HttpObjectAggregator.class.getSimpleName(), new HttpObjectAggregator(655350));
									//基础http协议解析
									pipeline.addLast(HttpResponseEncoder.class.getSimpleName(), new HttpResponseEncoder());
									pipeline.addLast(HttpRequestDecoder.class.getSimpleName(), new HttpRequestDecoder());
									//自定义http协议解析，主要为了接收post请求
									pipeline.addLast(CommonHttpCodec.class.getSimpleName(), new CommonHttpCodec());
									//业务逻辑
									pipeline.addLast(SceneHttpServerHandler.class.getName(), sceneHttpServerHandler);
								}
							});
			
			ChannelFuture sync = serverBootstrap.bind(sceneConfig.getHttpPort()).sync();
			channel = sync.channel();
			sync.channel().closeFuture().sync();
		} catch(InterruptedException e) {
			
			e.printStackTrace();
			
		} finally {
			
			acceptorGroup.shutdownGracefully().sync();
			ioGroup.shutdownGracefully().sync();
		}
	}
	
	public void stopService() {
        if (channel != null) {
                channel.close();
        }
        if (acceptorGroup != null) {
        	acceptorGroup.shutdownGracefully();
        }
        if (ioGroup != null) {
        	ioGroup.shutdownGracefully();
        }
	}
}
