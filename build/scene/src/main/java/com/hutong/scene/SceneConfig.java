package com.hutong.scene;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SceneConfig {

	// scene唯一标识
	private String sceneServerId = "";
	
	// scene 的名字空间
	private String namespace = "";
	
	// 100: 代表主城    
	private int sceneType = -1;
	
	// 该场景可允许连接的服务器id 
	private List<String> serverIdList = new ArrayList<String>();
	
	// 这个scene支持的线
	private List<String> sceneLineIdList = new ArrayList<String>();
	
	private String ip = "";
	
	private int port = -1;
	
	// scene中工作线程池
	private int sceneWorkGroupNum = -1;
	
	// http使用的端口
	private int httpPort = -1;
	
	public void init(SceneConfig sceneConfig) throws Exception {
		
		if(sceneConfig.getSceneServerId() == null || sceneConfig.getSceneServerId().isEmpty() 
				|| sceneConfig.getNamespace() == null || sceneConfig.getNamespace().isEmpty() 
				|| sceneConfig.getSceneType() == -1 
				|| sceneConfig.getServerIdList() == null || sceneConfig.getServerIdList().isEmpty()
				|| sceneConfig.getSceneLineIdList() == null || sceneConfig.getSceneLineIdList().isEmpty()
				|| sceneConfig.getIp() == null || sceneConfig.getIp().isEmpty()
				|| sceneConfig.getPort() == -1 
				|| sceneConfig.getSceneWorkGroupNum() == -1
				|| sceneConfig.getHttpPort() == -1) {
			throw new Exception("SceneConfig wrong! SceneConfig:" + sceneConfig.toString());
		}
		
		this.sceneServerId = sceneConfig.getSceneServerId();
		this.namespace = sceneConfig.getNamespace();
		this.sceneType = sceneConfig.getSceneType();
		this.serverIdList = sceneConfig.getServerIdList();
		this.sceneLineIdList = sceneConfig.getSceneLineIdList();
		this.ip = sceneConfig.getIp();
		this.port = sceneConfig.getPort();
		this.sceneWorkGroupNum = sceneConfig.getSceneWorkGroupNum();
		this.httpPort = sceneConfig.getHttpPort();
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("sceneServerId:" + sceneServerId + "; namespace:" + namespace);
		sb.append("; sceneType:" + sceneType + "; serverIdAry:" + serverIdList + "; sceneLineIdAry:" + sceneLineIdList);
		sb.append("; ip:" + ip + "; port:" + port + "; sceneWorkGroupNum:" + sceneWorkGroupNum);
		sb.append("; httpPort:" + httpPort);
		
		return sb.toString();
	}

	/**
	 * @return the sceneServerId
	 */
	public String getSceneServerId() {
		return sceneServerId;
	}

	/**
	 * @param sceneServerId the sceneServerId to set
	 */
	public void setSceneServerId(String sceneServerId) {
		this.sceneServerId = sceneServerId;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the sceneType
	 */
	public int getSceneType() {
		return sceneType;
	}

	/**
	 * @param sceneType the sceneType to set
	 */
	public void setSceneType(int sceneType) {
		this.sceneType = sceneType;
	}

	/**
	 * @return the serverIdList
	 */
	public List<String> getServerIdList() {
		return serverIdList;
	}

	/**
	 * @param serverIdList the serverIdList to set
	 */
	public void setServerIdList(List<String> serverIdList) {
		this.serverIdList = serverIdList;
	}

	/**
	 * @return the sceneLineIdList
	 */
	public List<String> getSceneLineIdList() {
		return sceneLineIdList;
	}

	/**
	 * @param sceneLineIdList the sceneLineIdList to set
	 */
	public void setSceneLineIdList(List<String> sceneLineIdList) {
		this.sceneLineIdList = sceneLineIdList;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the sceneWorkGroupNum
	 */
	public int getSceneWorkGroupNum() {
		return sceneWorkGroupNum;
	}

	/**
	 * @param sceneWorkGroupNum the sceneWorkGroupNum to set
	 */
	public void setSceneWorkGroupNum(int sceneWorkGroupNum) {
		this.sceneWorkGroupNum = sceneWorkGroupNum;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}
}
