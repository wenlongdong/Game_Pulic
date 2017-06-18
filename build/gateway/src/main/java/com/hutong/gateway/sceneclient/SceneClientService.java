package com.hutong.gateway.sceneclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.gateway.GatewayConfig;
import com.hutong.gateway.GatewayOtherConfig;
import com.hutong.gateway.dispatcher.GatewayDispatcher;
import com.hutong.gateway.loadblance.SceneLoadBlance;
import com.hutong.gateway.redis.InnerRedisService;
import com.hutong.socketbase.codec.codec.InnerGateDecoder;
import com.hutong.socketbase.codec.codec.InnerGateEncoder;
import com.hutong.socketbase.commonutil.CommonFunc;
import com.hutong.socketbase.define.SceneGateDefine;
import com.hutong.socketbase.load.report.SceneBrifeInfo;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Component
public class SceneClientService {

	@Autowired
	private GatewayOtherConfig gatewayOtherConfig;

	/** ? */
	@Autowired
	private GatewayDispatcher gatewayDispatcher;

	/** ? */
	@Autowired
	private InnerRedisService redisService;
	
	@Autowired
	private GatewayConfig gatewayConfig;
	
	@Autowired
	private SceneLoadBlance sceneLoadBlance;
	
	//设置一个最大数量的线程池，用来和scene建立连接，目前是20，也就是说，在每个scene一个连接的情况下，最大允许同时存在20个scene
	//目前改为不限制数量
	private ExecutorService sceneClientPool = Executors.newCachedThreadPool(new DefaultThreadFactory("Scene-Client-GroupPool"));

	//scene的gateway客户端(sceneServerId -> ChannelFurture)  一个作用是防止重复连接，另一个是用于关闭时的资源回收
	private final Map<String, Set<ChannelFuture>> sceneServerIdMapChannelFurture = new ConcurrentHashMap<String, Set<ChannelFuture>>();
	
	
	public synchronized void addSceneServerIdChannelFuture(String sceneServerId, ChannelFuture channelFuture){
		
		Set<ChannelFuture> channelFutureSet = sceneServerIdMapChannelFurture.get(sceneServerId);
		if(channelFutureSet == null){
			channelFutureSet = new HashSet<ChannelFuture>();
			sceneServerIdMapChannelFurture.put(sceneServerId, channelFutureSet);
		}
		
		if(channelFuture != null){
			channelFutureSet.add(channelFuture);
		}
	}
	
	public Set<ChannelFuture> getChannelFutureSetBySceneServerId(String sceneServerId){
		if(sceneServerIdMapChannelFurture.containsKey(sceneServerId)){
			return sceneServerIdMapChannelFurture.get(sceneServerId);
		} else {
			return new HashSet<ChannelFuture>();
		}
	}
	
	public synchronized void removeSceneServerIdChannelFuture(String sceneServerId){
		sceneServerIdMapChannelFurture.remove(sceneServerId);
	}
	
	
	public void startSceneClientService() {
		
		List<SceneBrifeInfo> sceneBrifeInfoList = redisService.hashValues(CommonFunc.getAllSceneBrifeInfo(gatewayConfig.getNamespace()), SceneBrifeInfo.class);
		for (SceneBrifeInfo sceneBrifeInfo : sceneBrifeInfoList) {
			if (!sceneBrifeInfo.isTimeOut()//scene没有超时 
					&& !sceneServerIdMapChannelFurture.containsKey(sceneBrifeInfo.getSceneServerId())//这个scene当前不在连接中的状态 
					&& !sceneLoadBlance.isSceneServerAvailable(sceneBrifeInfo.getSceneServerId())) {//这个scene没有连接上
				
				PubQueueLogUtil.logDebug("add sceneBrifeInfo is >>>>> " + sceneBrifeInfo.toString());
				startSceneClient(sceneBrifeInfo);
			} else if(sceneBrifeInfo.isTimeOut()){
				
				PubQueueLogUtil.logDebug("sceneBrifeInfo is timeout >>>>> " + sceneBrifeInfo.toString());
			} else if(sceneServerIdMapChannelFurture.containsKey(sceneBrifeInfo.getSceneServerId())){
				
				PubQueueLogUtil.logDebug("sceneBrifeInfo is connecting >>>>> " + sceneBrifeInfo.toString());
				if(sceneLoadBlance.isSceneServerAvailable(sceneBrifeInfo.getSceneServerId())){
					
					PubQueueLogUtil.logDebug("Also sceneBrifeInfo has connectted >>>>> " + sceneBrifeInfo.toString());
				}
			} else if(sceneLoadBlance.isSceneServerAvailable(sceneBrifeInfo.getSceneServerId())){
				
				PubQueueLogUtil.logDebug("sceneBrifeInfo has connectted >>>>> " + sceneBrifeInfo.toString());
			}
		}
	}

	/**
	 * @author Amyn
	 * @description ？
	 * 
	 * @param sceneLoadInfo
	 */
	public void startSceneClient(final SceneBrifeInfo sceneBrifeInfo) {
		
		final GatewayOtherConfig gatewayOtherConfig = this.gatewayOtherConfig;
		
		//此处先将这个scene的信息填充进去，等到收到具体消息的时候，成功后再将null值替换掉  防止gate这边无限重连；不成功的话会关闭此client，关闭的时候也会释放
		addSceneServerIdChannelFuture(sceneBrifeInfo.getSceneServerId(), null);
		
		sceneClientPool.execute(new Runnable() {
				@Override
				public void run() {
					
					NioEventLoopGroup nioEventLoopGroup = null;
					
					try {
						
						nioEventLoopGroup = new NioEventLoopGroup(gatewayOtherConfig.getGateway_sceneClient_channelNum(), new DefaultThreadFactory("Scene-Client-Bootstrap-" + sceneBrifeInfo.getSceneServerId()));
						
						Bootstrap bootstrap = new Bootstrap();
						bootstrap.group(nioEventLoopGroup)
								.channel(NioSocketChannel.class)
								
								.option(ChannelOption.SO_RCVBUF, gatewayOtherConfig.getGateway_sceneClient_rcvbuf() * 1024)
								.option(ChannelOption.SO_SNDBUF, gatewayOtherConfig.getGateway_sceneClient_sndbuf() * 1024)
								.option(ChannelOption.SO_KEEPALIVE, true)
								.option(ChannelOption.TCP_NODELAY, true)
								.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, gatewayOtherConfig.getGateway_sceneClient_timeOut() * 1000)
								.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
								
								.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, gatewayOtherConfig.getGateway_sceneClient_writeBufferHighWaterMark() * 1024)
								.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, gatewayOtherConfig.getGateway_sceneClient_writeBufferLowWaterMark() * 1024)
								
								.handler(new ChannelInitializer<SocketChannel>() {
	
									@Override
									protected void initChannel(SocketChannel sc) throws Exception {
										
										LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4, true);
										LengthFieldPrepender lengthFieldPrepender = new LengthFieldPrepender(4);
										
										IdleStateHandler idleStateHandler = new IdleStateHandler(SceneGateDefine.INNER_READER_IDLE_TIME_SECONDS, 
												SceneGateDefine.INNER_WRITER_IDLE_TIME_SECONDS, SceneGateDefine.INNER_ALL_IDLE_TIME_SECONDS);
										
										SceneClientHandler sceneClientHandler = new SceneClientHandler(sceneBrifeInfo.getSceneType(), sceneBrifeInfo.getSceneServerId(), gatewayConfig.getGatewayId(), gatewayDispatcher);
										
										ChannelPipeline pipeline = sc.pipeline();
										pipeline.addLast(IdleStateHandler.class.getName(), idleStateHandler);
										pipeline.addLast(LengthFieldPrepender.class.getName(), lengthFieldPrepender);
										pipeline.addLast(LengthFieldBasedFrameDecoder.class.getName(), lengthFieldBasedFrameDecoder);
										pipeline.addLast(InnerGateEncoder.class.getName(), new InnerGateEncoder());
										pipeline.addLast(InnerGateDecoder.class.getName(), new InnerGateDecoder());
										pipeline.addLast(SceneClientHandler.class.getName(), sceneClientHandler);
									}
								});
						
						//启动多个channel建立和scene之间的联系
						for(int num=1; num <= gatewayOtherConfig.getGateway_sceneClient_channelNum(); ++num){
							ChannelFuture channelFuture = null;
							channelFuture = bootstrap.connect(sceneBrifeInfo.getIp(), sceneBrifeInfo.getPort()).sync();
							addSceneServerIdChannelFuture(sceneBrifeInfo.getSceneServerId(), channelFuture);
						}
						
						//同步关闭所有的channel
						for(ChannelFuture channelFuture : getChannelFutureSetBySceneServerId(sceneBrifeInfo.getSceneServerId())){
							channelFuture.channel().closeFuture().sync();
						}
					} catch (Exception e){
						
						PubQueueLogUtil.logError("gateway scene Client connect sceneType " + sceneBrifeInfo.getSceneType() + " SceneServerId " + sceneBrifeInfo.getSceneServerId() + " throws an exception!! ", e);
					} finally {
						
						for(ChannelFuture channelFuture : getChannelFutureSetBySceneServerId(sceneBrifeInfo.getSceneServerId())){
							
							PubQueueLogUtil.logWarn("In startSceneClient finally channelFuture is " + channelFuture + " SceneServerId " + sceneBrifeInfo.getSceneServerId());
							if(channelFuture != null){
								PubQueueLogUtil.logWarn("In startSceneClient close channelFuture finally channelFuture is " + channelFuture + " SceneServerId " + sceneBrifeInfo.getSceneServerId());
								channelFuture.channel().close();
							}
						}
						
						nioEventLoopGroup.shutdownGracefully();
						
						//这个不用管是否连接上  没连接上就抛异常，则channelfuture为null，直接删除；连接上了，channelfuture已经close了，删除也没错
						removeSceneServerIdChannelFuture(sceneBrifeInfo.getSceneServerId());
//						if(null != channelFuture){//说明连接上了 才可能有gsregist消息，填充这个集合
//							//此处将scene的channel释放掉，使timer的驱动可以再次重连
//							sceneClientData.remove(sceneBrifeInfo.getSceneType(), sceneBrifeInfo.getSceneServerId(), channelFuture.channel());
//						}
						PubQueueLogUtil.logWarn("gateway scene Client disconnect with SceneType " + sceneBrifeInfo.getSceneType() + " SceneServerId " 
								+ sceneBrifeInfo.getSceneServerId());
					}
				}
			}
		);
	}

	public void stopSceneClient() throws InterruptedException{
		
		for(Set<ChannelFuture> channelFutureSet : sceneServerIdMapChannelFurture.values()){
			for(ChannelFuture channelFuture : channelFutureSet){
				if(null != channelFuture){
					channelFuture.channel().close();
				}
			}
		}
		
//		sceneClientPool.shutdownGracefully();
		sceneClientPool.shutdown();
	}
}
