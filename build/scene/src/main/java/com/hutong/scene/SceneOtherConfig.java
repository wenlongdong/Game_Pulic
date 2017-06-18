package com.hutong.scene;

import org.springframework.stereotype.Component;

@Component
public class SceneOtherConfig {

	// scene唯一标识
	private String sceneServerId = "";
	
	//////////////////////////////////////////////////scene的配置信息////////////////////////////////////////////////
	// TCP配置
	// 接收消息最大值  K 为单位  10M
	private int scene_rcvbuf = 10000; 
	// 发送消息最大值  K 为单位  10M
	private int scene_sndbuf = 10000;
	// acceptor线程池大小
	private int scene_acceptor_group_num = 1;
	// io 线程池数
	private int scene_io_group_num = 2;
	//低水位线  K 为单位  30M 
	private int scene_writeBufferLowWaterMark = 30720;
	//高水位线  K 为单位   60M
	private int scene_writeBufferHighWaterMark = 61440;
	//////////////////////////////////////////////////scene的配置信息////////////////////////////////////////////////

	//////////////////////////////////////////////////scene http的配置信息////////////////////////////////////////////////
	// 是否启用http服务
	private int scene_http_use = 1;
	// io线程池数
	private int scene_http_io_group_num = 1;
	// scene的工作线程池数量
	private int scene_http_work_group_num = 2;
	// scene http访问的白名单
	private String scene_http_ipFilter = "192_168_1_153,192_168_1_167";
	//////////////////////////////////////////////////scene http的配置信息////////////////////////////////////////////////
	
	public void init(SceneOtherConfig sceneOtherConfig) throws Exception {
		
		this.sceneServerId = sceneOtherConfig.getSceneServerId();
		
		//////////////////////////////////////////////////scene的配置信息////////////////////////////////////////////////
		// TCP配置
		// 接收消息最大值
		this.scene_rcvbuf = sceneOtherConfig.getScene_rcvbuf();
		// 发送消息最大值
		this.scene_sndbuf = sceneOtherConfig.getScene_sndbuf();
		// acceptor线程池大小
		this.scene_acceptor_group_num = sceneOtherConfig.getScene_acceptor_group_num();
		// io线程池数
		this.scene_io_group_num = sceneOtherConfig.getScene_io_group_num();
		//低水位线  30M
		this.scene_writeBufferLowWaterMark = sceneOtherConfig.getScene_writeBufferLowWaterMark();
		//高水位线 60M
		this.scene_writeBufferHighWaterMark = sceneOtherConfig.getScene_writeBufferHighWaterMark();
		//////////////////////////////////////////////////scene的配置信息////////////////////////////////////////////////

		//////////////////////////////////////////////////scene http的配置信息////////////////////////////////////////////////
		// 是否启用http服务
		this.scene_http_use = sceneOtherConfig.getScene_http_use();
		// io线程池数
		this.scene_http_io_group_num = sceneOtherConfig.getScene_http_io_group_num();
		// scene的工作线程池数量
		this.scene_http_work_group_num = sceneOtherConfig.getScene_http_work_group_num();
		// scene http访问的白名单
		this.scene_http_ipFilter = sceneOtherConfig.getScene_http_ipFilter();
	}

	
	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("sceneServerId:" + sceneServerId + ";  ");
		
		sb.append(" Scene TCP Server Config[ scene_rcvbuf : " + scene_rcvbuf + " ;");
		sb.append(" scene_sndbuf : " + scene_sndbuf + " ;");
		sb.append(" scene_acceptor_group_num : " + scene_acceptor_group_num + " ;");
		sb.append(" scene_io_group_num : " + scene_io_group_num + " ;");
		sb.append(" scene_writeBufferLowWaterMark : " + scene_writeBufferLowWaterMark + " ;");
		sb.append(" scene_writeBufferHighWaterMark : " + scene_writeBufferHighWaterMark + " ];");
		
		
		sb.append(" Scene HTTP Server Config[ scene_http_use : " + scene_http_use + " ;");
		sb.append(" scene_http_io_group_num : " + scene_http_io_group_num + " ;");
		sb.append(" scene_http_work_group_num : " + scene_http_work_group_num + " ;");
		sb.append(" scene_http_ipFilter : " + scene_http_ipFilter + " ];");
		
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
	 * @return the scene_rcvbuf
	 */
	public int getScene_rcvbuf() {
		return scene_rcvbuf;
	}

	/**
	 * @param scene_rcvbuf the scene_rcvbuf to set
	 */
	public void setScene_rcvbuf(int scene_rcvbuf) {
		this.scene_rcvbuf = scene_rcvbuf;
	}

	/**
	 * @return the scene_sndbuf
	 */
	public int getScene_sndbuf() {
		return scene_sndbuf;
	}

	/**
	 * @param scene_sndbuf the scene_sndbuf to set
	 */
	public void setScene_sndbuf(int scene_sndbuf) {
		this.scene_sndbuf = scene_sndbuf;
	}

	/**
	 * @return the scene_acceptor_group_num
	 */
	public int getScene_acceptor_group_num() {
		return scene_acceptor_group_num;
	}

	/**
	 * @param scene_acceptor_group_num the scene_acceptor_group_num to set
	 */
	public void setScene_acceptor_group_num(int scene_acceptor_group_num) {
		this.scene_acceptor_group_num = scene_acceptor_group_num;
	}

	/**
	 * @return the scene_io_group_num
	 */
	public int getScene_io_group_num() {
		return scene_io_group_num;
	}

	/**
	 * @param scene_io_group_num the scene_io_group_num to set
	 */
	public void setScene_io_group_num(int scene_io_group_num) {
		this.scene_io_group_num = scene_io_group_num;
	}

	/**
	 * @return the scene_writeBufferLowWaterMark
	 */
	public int getScene_writeBufferLowWaterMark() {
		return scene_writeBufferLowWaterMark;
	}

	/**
	 * @param scene_writeBufferLowWaterMark the scene_writeBufferLowWaterMark to set
	 */
	public void setScene_writeBufferLowWaterMark(int scene_writeBufferLowWaterMark) {
		this.scene_writeBufferLowWaterMark = scene_writeBufferLowWaterMark;
	}

	/**
	 * @return the scene_writeBufferHighWaterMark
	 */
	public int getScene_writeBufferHighWaterMark() {
		return scene_writeBufferHighWaterMark;
	}

	/**
	 * @param scene_writeBufferHighWaterMark the scene_writeBufferHighWaterMark to set
	 */
	public void setScene_writeBufferHighWaterMark(int scene_writeBufferHighWaterMark) {
		this.scene_writeBufferHighWaterMark = scene_writeBufferHighWaterMark;
	}

	/**
	 * @return the scene_http_use
	 */
	public int getScene_http_use() {
		return scene_http_use;
	}

	/**
	 * @param scene_http_use the scene_http_use to set
	 */
	public void setScene_http_use(int scene_http_use) {
		this.scene_http_use = scene_http_use;
	}

	/**
	 * @return the scene_http_io_group_num
	 */
	public int getScene_http_io_group_num() {
		return scene_http_io_group_num;
	}

	/**
	 * @param scene_http_io_group_num the scene_http_io_group_num to set
	 */
	public void setScene_http_io_group_num(int scene_http_io_group_num) {
		this.scene_http_io_group_num = scene_http_io_group_num;
	}

	/**
	 * @return the scene_http_work_group_num
	 */
	public int getScene_http_work_group_num() {
		return scene_http_work_group_num;
	}

	/**
	 * @param scene_http_work_group_num the scene_http_work_group_num to set
	 */
	public void setScene_http_work_group_num(int scene_http_work_group_num) {
		this.scene_http_work_group_num = scene_http_work_group_num;
	}

	/**
	 * @return the scene_http_ipFilter
	 */
	public String getScene_http_ipFilter() {
		return scene_http_ipFilter;
	}

	/**
	 * @param scene_http_ipFilter the scene_http_ipFilter to set
	 */
	public void setScene_http_ipFilter(String scene_http_ipFilter) {
		this.scene_http_ipFilter = scene_http_ipFilter;
	}
}
