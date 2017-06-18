/**
 * 
 */
package com.hutong.gateway.loadblance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hutong.gateway.GatewayConfig;
import com.hutong.gateway.dispatcher.GatewayRequestMsgQueue;
import com.hutong.gateway.dispatcher.GatewayRequestMsgQueue.GatewayRequestMsgQueueInfo;
import com.hutong.gateway.redis.InnerRedisService;
import com.hutong.socketbase.commonutil.CommonFunc;
import com.hutong.socketbase.load.report.GatewayInfo;
import com.hutong.socketbase.load.report.GatewayLoadInfo;
import com.hutong.socketbase.load.report.SceneBrifeInfo;
import com.hutong.socketbase.load.report.SceneLineInfo;
import com.hutong.socketbase.load.report.SceneLoadInfo;

/**
 * @author DWL
 *
 */
@Service
public class SceneLoadBlance {

	@Autowired
	private InnerRedisService innerRedisService;
	
	@Autowired
	private GatewayConfig gatewayConfig;
	
	@Autowired
	private GatewayRequestMsgQueue gatewayRequestMsgQueue;
	
	private List<SceneBrifeInfo> cachedSceneBrifeInfo = new ArrayList<>();
	
	private Map<Integer, List<SceneLoadInfo>> cachedSceneLoadInfoMap = new ConcurrentHashMap<Integer, List<SceneLoadInfo>>();
	
	//根据一个sceneServerId来判断这个场景的进程是否可用
	public boolean isSceneServerAvailable(String sceneServerId){
		
		List<GatewayRequestMsgQueueInfo> gatewayRequestMsgQueueInfoList = gatewayRequestMsgQueue.getGatewayRequestMsgQueueInfoList(sceneServerId);
		if(gatewayRequestMsgQueueInfoList != null && !gatewayRequestMsgQueueInfoList.isEmpty()){
			return true;
		} else {
			return false;
		}
	}
	
	
	public void reloadSceneLoadBlance(){
		
		cachedSceneBrifeInfo = innerRedisService.hashValues(CommonFunc.getAllSceneBrifeInfo(gatewayConfig.getNamespace()), SceneBrifeInfo.class);
		
		Set<Integer> sceneTypeSet = new HashSet<Integer>();
		
		for(SceneBrifeInfo sceneBrifeInfo : getRawSceneBrifeInfoList()){
			sceneTypeSet.add(sceneBrifeInfo.getSceneType());
		}
		
		for(Integer sceneTypeTmp : sceneTypeSet){
			
			List<SceneLoadInfo> sceneLoadInfoList = innerRedisService.hashValues(CommonFunc.getSceneTypeRedisName(gatewayConfig.getNamespace(), sceneTypeTmp), SceneLoadInfo.class);
			cachedSceneLoadInfoMap.put(sceneTypeTmp, sceneLoadInfoList);
		}
		
	}
	
	//获取所有的SceneBrifeInfo 信息  包括 可用的 和 不可用的，都会获取到
	private List<SceneBrifeInfo> getRawSceneBrifeInfoList(){
		
		return cachedSceneBrifeInfo;
	}
	
	private List<SceneLoadInfo> getRawSceneLoadInfoList(int sceneType){
		
		List<SceneLoadInfo> sceneLoadInfoList = cachedSceneLoadInfoMap.get(sceneType);
		if(sceneLoadInfoList == null){
			return new ArrayList<SceneLoadInfo>();
		} else {
			return sceneLoadInfoList;
		}
	}
	
	
	//获取所有可用的SceneBrifeInfo信息  所谓可用的  就是当前gateway已经成功建立了连接的
	public List<SceneBrifeInfo> getAllSceneBrifeInfoList(){
		
		List<SceneBrifeInfo> sceneBrifeInfoList = new ArrayList<SceneBrifeInfo>();
		
		for(SceneBrifeInfo sceneBrifeInfo : getRawSceneBrifeInfoList()){
			
			if(sceneBrifeInfo.isTimeOut()){
				continue;
			}
			
			if(isSceneServerAvailable(sceneBrifeInfo.getSceneServerId())){
				sceneBrifeInfoList.add(sceneBrifeInfo);
			}
		}
		return sceneBrifeInfoList;
	}
	
	//获取某一种sceneType类型下的所有sceneLoadInfo列表
	public List<SceneLoadInfo> getAllSceneLoadInfoListByType(int sceneType){
		
		List<SceneLoadInfo> sceneLoadInfoList = new ArrayList<SceneLoadInfo>();
		
		for(SceneLoadInfo sceneLoadInfo : getRawSceneLoadInfoList(sceneType)){
			
			if(isSceneServerAvailable(sceneLoadInfo.getSceneServerId())){
				sceneLoadInfoList.add(sceneLoadInfo);
			}
		}
		return sceneLoadInfoList;		
	}
	
	//获取某一种sceneType类型下的所有sceneLineInfo列表
	public List<SceneLineInfo> getAllSceneLineInfoListByType(int sceneType){
		
		List<SceneLineInfo> sceneLineInfoList = new ArrayList<>();
		
		for(SceneLoadInfo sceneLoadInfo : getAllSceneLoadInfoListByType(sceneType)){
			for(SceneLineInfo sceneLineInfo : sceneLoadInfo.getLineMapSceneLineInfo().values()){
				sceneLineInfoList.add(sceneLineInfo);
			}
		}
		
		return sceneLineInfoList;
	}
	
	//获取某一个sceneserverId上的某一条线的总人数
	public int getLinePlayerNum(String sceneServerId, int lineId){
		
		int playerNum = 0;
		
		for(GatewayInfo gatewayInfo : innerRedisService.hashValues(CommonFunc.getAllGatewayInfo(gatewayConfig.getNamespace()), GatewayInfo.class)){
			GatewayLoadInfo gatewayLoadInfo = innerRedisService.hashGet(CommonFunc.getGatewayRedisName(gatewayConfig.getNamespace(), gatewayInfo.getGatewayId()),
					CommonFunc.generateGate2SceneLineId(sceneServerId, lineId), GatewayLoadInfo.class);
			if(gatewayLoadInfo != null){
				playerNum += gatewayLoadInfo.getPlayerNum();
			}
		}
		return playerNum;
	}
	
	//增加某一个线上的人数  不用再写一个dec了  因为num传递负数  就是dec
	public void incLinePlayerNum(String sceneServerId, int lineId, int num){
		GatewayLoadInfo gatewayLoadInfo = innerRedisService.hashGet(CommonFunc.getGatewayRedisName(gatewayConfig.getNamespace(), gatewayConfig.getGatewayId()),
				CommonFunc.generateGate2SceneLineId(sceneServerId, lineId), GatewayLoadInfo.class);
		if(gatewayLoadInfo != null){
			gatewayLoadInfo.setPlayerNum(gatewayLoadInfo.getPlayerNum() + num);
			
			innerRedisService.hashSet(CommonFunc.getGatewayRedisName(gatewayConfig.getNamespace(), gatewayConfig.getGatewayId()),
					CommonFunc.generateGate2SceneLineId(sceneServerId, lineId), gatewayLoadInfo);
		}
	}
}
