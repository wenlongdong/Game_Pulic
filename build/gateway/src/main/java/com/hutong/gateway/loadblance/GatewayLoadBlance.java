package com.hutong.gateway.loadblance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hutong.gateway.GatewayConfig;
import com.hutong.gateway.redis.InnerRedisService;
import com.hutong.socketbase.commonutil.CommonFunc;
import com.hutong.socketbase.load.report.GatewayInfo;

@Service
public class GatewayLoadBlance {

	@Autowired
	private InnerRedisService innerRedisService;
	
	@Autowired
	private GatewayConfig gatewayConfig;
	
	public List<GatewayInfo> getAllGatewayInfoList(){
		
		List<GatewayInfo> gateWayInfoList = new ArrayList<GatewayInfo>();
		
		for(GatewayInfo gatewayInfo : innerRedisService.hashValues(CommonFunc.getAllGatewayInfo(gatewayConfig.getNamespace()), GatewayInfo.class)){
			if(!gatewayInfo.isTimeOut()){
				gateWayInfoList.add(gatewayInfo);
			}
		}
		
		return gateWayInfoList;
	}
}
