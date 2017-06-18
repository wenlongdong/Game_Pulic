package com.hutong.gateway;

import io.netty.channel.ChannelFuture;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import com.hutong.gateway.dispatcher.GatewayDispatcher;
import com.hutong.gateway.gatewayserver.GatewayServerService;
import com.hutong.gateway.redis.InnerRedisService;
import com.hutong.gateway.sceneclient.SceneClientService;
import com.hutong.socketbase.conf.Config;


/**
 * @author Amyn
 * @description ?
 * 
 */
public class GatewayBoot {
	
	private static Log log = LogFactory.getLog(GatewayBoot.class);

	private static ClassPathXmlApplicationContext context = null;
	
	public static ClassPathXmlApplicationContext getApplicationContext(){
		return context;
	}
	
	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static ClassPathXmlApplicationContext initSpring(Config conf) throws Exception {

		//配置log4j日志文件
		PropertyConfigurator.configureAndWatch(conf.getLog4jConf(), 1);

		//初始化spring
		List<String> springConfList = conf.getSpringConfList();
		springConfList.add("classpath:conf/gateway.base.xml");
		
		context = new ClassPathXmlApplicationContext(springConfList.toArray(new String[springConfList.size()]));
		
		return context;
	}
	
	
	public static void initGateway(RedisTemplate<Object, Object> redisTemplate, GatewayConfig gatewayConfig, 
			GatewayOtherConfig gatewayOtherConfig) throws Exception {
		//初始化公共库内部使用的redis
		context.getBean(InnerRedisService.class).initInnerRedisGateway(redisTemplate);

		context.getBean(GatewayConfig.class).init(gatewayConfig);
		
		context.getBean(GatewayOtherConfig.class).init(gatewayOtherConfig);
		
		//初始化注解
		context.getBean(GatewayDispatcher.class).init();
	}
	
	public static void startService() throws Exception {
		
		SceneClientService sceneClientService = context.getBean(SceneClientService.class);
		GatewayServerService gatewayServerService = context.getBean(GatewayServerService.class);

		ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		try {
			
			//开始启动服务,先启动gateway->scene的连接，等连接成功了之后，才能启动gateway的服务器
//			sceneClientService.startSceneClientService(); // GatewayReportTimeTask中有startSceneClientService
			
			//启动注册redis信息的timer
			GatewayReportTimeTask bean = context.getBean(GatewayReportTimeTask.class);
//			timer.scheduleAtFixedRate(bean, 0, 3 * 1000); //scheduleAtFixedRate 会导致scene server关闭再重启的那段时间里 积累大量
			exec.scheduleWithFixedDelay(bean, 0, 2, TimeUnit.SECONDS);
			
			//启动gatewayServer这个阶段以后，客户端会有接入
			ChannelFuture gateWayServerFuture = gatewayServerService.startService();
			
			gateWayServerFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			log.error("GatewayBoot failed !!!!!!!", e);
		} finally {
			
			/////////////下面是关闭回收资源逻辑//////////////////////////		
			//关闭timer
			exec.shutdown();
			
			//先停止gateway对客户端的服务
			gatewayServerService.stopService();
			
			//再停止gate对scene的客户端请求
			sceneClientService.stopSceneClient();
			
			//关闭spring
			context.close();
		}
		
		log.info("gateway closed !!!!!!!");
	}
}
