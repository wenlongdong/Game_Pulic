package com.hutong.scene;

import java.util.List;
import java.util.Timer;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.dispatcher.SceneDispatcher;
import com.hutong.scene.dispatcher.SceneHttpDispatcher;
import com.hutong.scene.redis.InnerRedisService;
import com.hutong.scene.server.SceneServerService;
import com.hutong.socketbase.conf.Config;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Service
public class SceneBoot {
	
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
	 * @throws BeansException
	 */
	public static ClassPathXmlApplicationContext initSpring(Config config) throws Exception {

		//配置log4j日志文件
		PropertyConfigurator.configure(config.getLog4jConf());
		
		//初始化spring
		List<String> springConfList = config.getSpringConfList();
		springConfList.add("classpath:conf/scene.base.xml");
		
		context = new ClassPathXmlApplicationContext(springConfList.toArray(new String[springConfList.size()]));
		
		return context;
	}
	
	public static void initScene(RedisTemplate<Object, Object> redisTemplate, SceneConfig sceneConfig, 
			SceneOtherConfig sceneOtherConfig) throws Exception {
		//初始化公共库内部使用的redis
		context.getBean(InnerRedisService.class).initInnerRedisScene(redisTemplate);

		context.getBean(SceneConfig.class).init(sceneConfig);
		
		context.getBean(SceneOtherConfig.class).init(sceneOtherConfig);
		
		//初始化注解
		context.getBean(SceneDispatcher.class).init();
		//初始化scene http 分发器
		context.getBean(SceneHttpDispatcher.class).init();
	}
	
	public static void startService() throws Exception {
		
		Timer timer = new Timer();

		try {
			
			//启动注册redis信息的timer
			SceneReportTimeTask bean = context.getBean(SceneReportTimeTask.class);
			timer.scheduleAtFixedRate(bean, 0, 1000);
			
			//开始启动服务,此时有可能有连接接入			
			context.getBean(SceneServerService.class).startService();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			PubQueueLogUtil.logError("SceneBoot failed !!!!!!!!!!!!", e);
		} finally {
			
			//关闭timer
			timer.cancel();
			
			//关闭spring
			context.close();
		}
		
		PubQueueLogUtil.logInfo("Scene closed !!!!!!!!!!!");
	}
}
