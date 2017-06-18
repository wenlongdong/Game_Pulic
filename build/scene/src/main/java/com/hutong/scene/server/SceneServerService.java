package com.hutong.scene.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.channel.Channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.SceneConfig;
import com.hutong.scene.SceneOtherConfig;
import com.hutong.scene.server.http.SceneHttpServerService;
import com.hutong.socketbase.codec.codec.InnerSceneDecoder;
import com.hutong.socketbase.codec.codec.InnerSceneEncoder;
import com.hutong.socketbase.define.SceneGateDefine;

@Service
public class SceneServerService {
	
	@Autowired
	private SceneOtherConfig sceneOtherConfig;
	
	@Autowired
	private SceneServerHandler sceneServerHandler;
	
	@Autowired
	private SceneHttpServerService sceneHttpServerService;

	@Autowired
	private SceneConfig sceneConfig;
	

    private EventLoopGroup acceptorGroup;
    private EventLoopGroup ioGroup;
    private Channel channel;
	
	public void startService() throws Exception {

		acceptorGroup = new NioEventLoopGroup(sceneOtherConfig.getScene_acceptor_group_num(), new DefaultThreadFactory("Scene-Group-Acceptor"));
		ioGroup = new NioEventLoopGroup(sceneOtherConfig.getScene_io_group_num(), new DefaultThreadFactory("Scene-Group-IO"));

		try{
			PubQueueLogUtil.logInfo("startService start");
			
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(acceptorGroup, ioGroup)
							.channel(NioServerSocketChannel.class)
							
							.option(ChannelOption.SO_RCVBUF, sceneOtherConfig.getScene_rcvbuf() * 1024)
							.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
							.option(ChannelOption.SO_REUSEADDR, true)
							
							.childOption(ChannelOption.SO_RCVBUF, sceneOtherConfig.getScene_rcvbuf() * 1024)
							.childOption(ChannelOption.SO_SNDBUF, sceneOtherConfig.getScene_sndbuf() * 1024)
							.childOption(ChannelOption.SO_KEEPALIVE, true)
							.childOption(ChannelOption.TCP_NODELAY, true)
							.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
							
							.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, sceneOtherConfig.getScene_writeBufferHighWaterMark() * 1024)
							.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, sceneOtherConfig.getScene_writeBufferLowWaterMark() * 1024)
							
							
							.handler(new LoggingHandler(LogLevel.INFO))
							.childHandler(new ChannelInitializer<SocketChannel>() {
								@Override
								protected void initChannel(SocketChannel sc) throws Exception {
									
									ChannelPipeline pipeline = sc.pipeline();
									
									LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4, true);
									LengthFieldPrepender lengthFieldPrepender = new LengthFieldPrepender(4);
									
									IdleStateHandler idleStateHandler = new IdleStateHandler(SceneGateDefine.INNER_READER_IDLE_TIME_SECONDS, 
											SceneGateDefine.INNER_WRITER_IDLE_TIME_SECONDS, SceneGateDefine.INNER_ALL_IDLE_TIME_SECONDS);
									
									pipeline.addLast(IdleStateHandler.class.getName(), idleStateHandler);
									pipeline.addLast(LengthFieldBasedFrameDecoder.class.getName(), lengthFieldBasedFrameDecoder);
									pipeline.addLast(LengthFieldPrepender.class.getName(), lengthFieldPrepender);
									pipeline.addLast(InnerSceneEncoder.class.getName(), new InnerSceneEncoder());
									pipeline.addLast(InnerSceneDecoder.class.getName(), new InnerSceneDecoder());
									pipeline.addLast(SceneServerHandler.class.getName(), sceneServerHandler);
								}
							});
			
//			ChannelFuture sync = serverBootstrap.bind(sceneConfig.getPort()).sync();
			ChannelFuture sync = this.doBind(serverBootstrap, sceneConfig.getPort());
			channel = sync.channel();
			
			PubQueueLogUtil.logInfo("sceneHttpServerService http start");
			//在这里启动http服务
			sceneHttpServerService.startService();
			PubQueueLogUtil.logInfo("sceneHttpServerService http finish");
			
			sync.channel().closeFuture().sync();
			
			PubQueueLogUtil.logInfo("startService end");
			
		} catch(InterruptedException e) {
			
			PubQueueLogUtil.logError("SceneServerService:startService", e);
		} finally {
			
			PubQueueLogUtil.logWarn("SceneServerService:shutdown service");
			acceptorGroup.shutdownGracefully().sync();
			ioGroup.shutdownGracefully().sync();
		}
	}
	
	/**
	 * 绑定端口方法，重试24次，每次十秒，即4分钟
	 * @param serverBootstrap
	 * @param port
	 * @return
	 * @throws Exception
	 */
	private ChannelFuture doBind(ServerBootstrap serverBootstrap, int port) throws Exception{
		Exception exception = null;
		for(int i=0; i < 24; i++){
			try{
				this.testPort(port);
				ChannelFuture sync = serverBootstrap.bind(sceneConfig.getPort()).sync();
				return sync;
			}catch (Exception e){
				exception = e;
				if(e instanceof BindException){
					PubQueueLogUtil.logError(port + "端口绑定失败，十秒后重试！", e);
					this.testPort(port);
					Thread.sleep(10 * 1000);
				}
			}
		}
		throw exception;
	}
	
	/**
	 * 测试端口被占用情况，并打印日志
	 * @param port
	 */
	private void testPort(int port){
		try{
			String[] cmds = {"/bin/sh","-c","lsof -i :" + port};
	        Process pro = Runtime.getRuntime().exec(cmds);
	        pro.waitFor();
	        InputStream in = pro.getInputStream();
	        BufferedReader read = new BufferedReader(new InputStreamReader(in));
	        String line = null;
	        while((line = read.readLine())!=null){
				PubQueueLogUtil.logError(port + "端口检测结果：" + line);
	        }
		}catch (Exception e){
			PubQueueLogUtil.logError(port + "检测端口状态异常！", e);
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
		sceneHttpServerService.stopService();
	}
}
