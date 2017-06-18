package com.hutong.gateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hutong.framework.util.PubQueueLogUtil;
import com.hutong.framework.util.UtilJson;
import com.hutong.gateway.dispatcher.GatewayDispatcher;
import com.hutong.gateway.gatewayserver.GatewayServerData;
import com.hutong.gateway.loadblance.SceneLoadBlance;
import com.hutong.gateway.redis.InnerRedisService;
import com.hutong.gateway.sceneclient.SceneClientService;
import com.hutong.socketbase.commonutil.CommonFunc;
import com.hutong.socketbase.define.SceneGateDefine;
import com.hutong.socketbase.load.report.GatewayInfo;
import com.hutong.socketbase.load.report.GatewayLoadInfo;
import com.hutong.socketbase.load.report.SceneBrifeInfo;
import com.hutong.socketbase.sessionbase.GatewaySession;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Component
public class GatewayReportTimeTask extends Thread {

	@Autowired
	private GatewayConfig gatewayConfig;
	
	@Autowired
	private InnerRedisService redisService;
	
	@Autowired
	private SceneClientService sceneClientService;
	
	@Autowired
	private GatewayServerData<GatewaySession> gatewayServerData;
	
	@Autowired
	private GatewayDispatcher dispatcher;
	
	@Autowired
	public GatewayOtherConfig gatewayOtherConfig;
	
	@Autowired
	public SceneLoadBlance sceneLoadBlance;
	
	@Override
	public void run() {
		
		PubQueueLogUtil.logWarn("GatewayReportTimeTask start <<<<<<<<<<<<<<<<<<");
		
		try {
			sceneClientService.startSceneClientService();
		} catch (Exception e) {
			PubQueueLogUtil.logError(" report this scene status error ", e);
		}
		
		try {
			refreshGatewayLoadInfo();
		} catch (Exception e) {
			PubQueueLogUtil.logError(" refreshGatewayLoadInfo throw exception!!! ", e);
		}
		
		
		try {
			
			GatewayInfo gatewayInfo = new GatewayInfo();

			gatewayInfo.setGatewayId(gatewayConfig.getGatewayId());
			gatewayInfo.setIp(gatewayConfig.getIp());
			gatewayInfo.setPort(gatewayConfig.getPort());
			gatewayInfo.setRefreshTime(System.currentTimeMillis());
			gatewayInfo.setIoThreadsNum(gatewayOtherConfig.getGateway_server_io_thread_num());
			gatewayInfo.setChannelsNum(gatewayServerData.getChannelNums());
			
			redisService.hashSet(CommonFunc.getAllGatewayInfo(gatewayConfig.getNamespace()), gatewayConfig.getGatewayId(), gatewayInfo);
			redisService.expire(CommonFunc.getAllGatewayInfo(gatewayConfig.getNamespace()), SceneGateDefine.INNER_ALL_GATES_INFOS_EXPIRE_TIME, TimeUnit.SECONDS);
		} catch (Exception e) {
			PubQueueLogUtil.logError("上报gateway数据失败", e);
		}
		
		
		try {
			sceneLoadBlance.reloadSceneLoadBlance();
		} catch (Exception e) {
			PubQueueLogUtil.logError("reloadSceneLoadBlance throws Exception", e);
		}
		
		PubQueueLogUtil.logWarn("GatewayReportTimeTask end >>>>>>>>>>>>>>>>>>>>>>>");
	}
	
	
	private void refreshGatewayLoadInfo(){
		
		try {
			//先删除那些超时的loadinfo
			List<GatewayLoadInfo> gatewayLoadInfoList = redisService.hashValues(CommonFunc.getGatewayRedisName(gatewayConfig.getNamespace(), gatewayConfig.getGatewayId()), GatewayLoadInfo.class);
			for(GatewayLoadInfo gatewayLoadInfo : gatewayLoadInfoList){
				if(gatewayLoadInfo.isTimeOut()){
					redisService.hashDel(CommonFunc.getGatewayRedisName(gatewayConfig.getNamespace(), gatewayConfig.getGatewayId()), gatewayLoadInfo.getSceneServerIdWithLineId());
				}
			}
			
			//用所有scene的当前线初始化结构
			Map<String, GatewayLoadInfo> sceneLinePlayerNumMap = new HashMap<String, GatewayLoadInfo>();
			List<SceneBrifeInfo> sceneBrifeInfoList = redisService.hashValues(CommonFunc.getAllSceneBrifeInfo(gatewayConfig.getNamespace()), SceneBrifeInfo.class);
			for(SceneBrifeInfo sceneBrifeInfo : sceneBrifeInfoList){
				for(String lineId : sceneBrifeInfo.getSceneLineIdList()){
					String sceneServerIdWithLineId = CommonFunc.generateGate2SceneLineId(sceneBrifeInfo.getSceneServerId(), Integer.parseInt(lineId));
					GatewayLoadInfo gatewayLoadInfo = new GatewayLoadInfo(sceneServerIdWithLineId);
					sceneLinePlayerNumMap.put(sceneServerIdWithLineId, gatewayLoadInfo);
				}
			}
			
			//初始化当前不在任何scene的结构
			String notInAnySceneId = CommonFunc.generateGate2SceneLineId("NOT_IN_ANY_SCENE", 0);
			sceneLinePlayerNumMap.put(notInAnySceneId, new GatewayLoadInfo(notInAnySceneId));
			
			//填充结构
			List<GatewaySession> gatewaySessionList = gatewayServerData.getAllGatewaySessions();
			for(GatewaySession gatewaySession : gatewaySessionList){
				
				String sceneServerId = gatewaySession.getSceneServerId();
				int lineId = gatewaySession.getLineId();
				
				String sceneServerIdWithLineId = "";
				if(sceneServerId.isEmpty()){
					sceneServerIdWithLineId = notInAnySceneId;
				} else {
					sceneServerIdWithLineId = CommonFunc.generateGate2SceneLineId(sceneServerId, lineId);
				}
				
				GatewayLoadInfo gatewayLoadInfo = sceneLinePlayerNumMap.get(sceneServerIdWithLineId);
				if(gatewayLoadInfo == null){
					gatewayLoadInfo = new GatewayLoadInfo("<<<WARNING>>>" + sceneServerIdWithLineId);
					sceneLinePlayerNumMap.put("<<<WARNING>>>" + sceneServerIdWithLineId, gatewayLoadInfo);
				}
				
				gatewayLoadInfo.setPlayerNum(gatewayLoadInfo.getPlayerNum() + 1);
			}
			
			//使用管道一次性更新到redis里面
			Map<String, String> redisMultiMap = new HashMap<String, String>();
			for(String sceneServerIdWithLineId : sceneLinePlayerNumMap.keySet()){
				
				GatewayLoadInfo gatewayLoadInfo = sceneLinePlayerNumMap.get(sceneServerIdWithLineId);
				String gatewayLoadInfoStr = UtilJson.O2S(gatewayLoadInfo);
				
				redisMultiMap.put(sceneServerIdWithLineId, gatewayLoadInfoStr);
			}
			
			redisService.hashSetAll(CommonFunc.getGatewayRedisName(gatewayConfig.getNamespace(), gatewayConfig.getGatewayId()), redisMultiMap);
			redisService.expire(CommonFunc.getGatewayRedisName(gatewayConfig.getNamespace(), gatewayConfig.getGatewayId()), SceneGateDefine.INNER_ALL_GATES_INFOS_EXPIRE_TIME, TimeUnit.SECONDS);
		} catch (Exception e) {
			PubQueueLogUtil.logError("refreshGatewayLoadInfo failed", e);
		}
	}
}
