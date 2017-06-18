package com.hutong.scene;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.scene.redis.InnerRedisService;
import com.hutong.scene.server.SceneServerData;
import com.hutong.socketbase.commonutil.CommonFunc;
import com.hutong.socketbase.define.SceneGateDefine;
import com.hutong.socketbase.load.report.SceneBrifeInfo;
import com.hutong.socketbase.load.report.SceneLineInfo;
import com.hutong.socketbase.load.report.SceneLoadInfo;
import com.hutong.socketbase.sessionbase.SceneSession;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Component
public class SceneReportTimeTask extends TimerTask {

	@Autowired
	private SceneConfig sceneConfig;
	
	@Autowired
	private SceneServerData<SceneSession> sceneServerData;
	
	@Autowired
	private SceneOtherConfig sceneOtherConfig;
	
	@Autowired
	private InnerRedisService redisService;
	
	public void updateSceneRedisInfo(){
		
		PubQueueLogUtil.logDebug("SceneReportTimeTask start <<<<<<<<<<<<<<<<<<");
		
		try {
			sceneServerData.correctLinePlayerNum();
		} catch (Exception e) {
			PubQueueLogUtil.logError("sceneServerData correctLinePlayerNum throw exception!!!", e);
		}
		
		try {
			SceneLoadInfo sceneLoadInfo = new SceneLoadInfo();

			sceneLoadInfo.setSceneType(sceneConfig.getSceneType());
			sceneLoadInfo.setSceneServerId(sceneConfig.getSceneServerId());
			sceneLoadInfo.setIp(sceneConfig.getIp());
			sceneLoadInfo.setPort(sceneConfig.getPort());
			sceneLoadInfo.setIoThreadsNum(sceneOtherConfig.getScene_io_group_num());
			sceneLoadInfo.setWorkThreadsNum(sceneConfig.getSceneWorkGroupNum());
			sceneLoadInfo.setRefreshTime(System.currentTimeMillis());
			sceneLoadInfo.setTotalPlayerNum(sceneServerData.getTotalPlayerNum());
			
			for(String lineId : sceneConfig.getSceneLineIdList()){
				
				SceneLineInfo sceneLineInfo = new SceneLineInfo();
				
				sceneLineInfo.setSceneServerId(sceneConfig.getSceneServerId());
				sceneLineInfo.setSceneLineId(Integer.parseInt(lineId));
				sceneLineInfo.setPlayersNum(sceneServerData.getLinePlayerNum(Integer.parseInt(lineId)));
				
				sceneLoadInfo.getLineMapSceneLineInfo().put(sceneLineInfo.getSceneLineId(), sceneLineInfo);
			}
			
			redisService.hashSet(CommonFunc.getSceneTypeRedisName(sceneConfig.getNamespace(), sceneConfig.getSceneType()), sceneConfig.getSceneServerId(), sceneLoadInfo);
			redisService.expire(CommonFunc.getSceneTypeRedisName(sceneConfig.getNamespace(), sceneConfig.getSceneType()), SceneGateDefine.INNER_SCENE_EXPIRE_TIME, TimeUnit.SECONDS);
			
			//scene信息的总览
			SceneBrifeInfo sceneBrifeInfo = new SceneBrifeInfo();
			sceneBrifeInfo.setSceneType(sceneConfig.getSceneType());
			sceneBrifeInfo.setSceneServerId(sceneConfig.getSceneServerId());
			sceneBrifeInfo.setIp(sceneConfig.getIp());
			sceneBrifeInfo.setPort(sceneConfig.getPort());
			sceneBrifeInfo.setSceneLineIdList(sceneConfig.getSceneLineIdList());
			sceneBrifeInfo.setPlayersNum(sceneServerData.getTotalPlayerNum());
			sceneBrifeInfo.setRefreshTime(System.currentTimeMillis());
			
			redisService.hashSet(CommonFunc.getAllSceneBrifeInfo(sceneConfig.getNamespace()), sceneBrifeInfo.getSceneServerId(), sceneBrifeInfo);
			redisService.expire(CommonFunc.getAllSceneBrifeInfo(sceneConfig.getNamespace()), SceneGateDefine.INNER_ALL_SCENE_BRIFE_INFO_EXPIRE_TIME, TimeUnit.SECONDS);
			
		} catch (Exception e) {
			PubQueueLogUtil.logError(" report this scene status error ", e);
		}
		
		PubQueueLogUtil.logDebug("SceneReportTimeTask end >>>>>>>>>>>>>>>>>>>>>>>");
	}
	
	@Override
	public void run() {
		
		updateSceneRedisInfo();
	}
}
