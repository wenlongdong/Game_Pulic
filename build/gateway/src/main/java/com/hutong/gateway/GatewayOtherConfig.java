package com.hutong.gateway;

import org.springframework.stereotype.Component;

@Component
public class GatewayOtherConfig {

	//如果找不到特定的配置  就会使用默认的配置
	private String gatewayId = "";
	
	////////////////////////////////////////////////////gateway作为server的配置信息//////////////////////////
	// gateway作为server的io线程数
	private int gateway_server_io_thread_num = 4;
	// 连接数
	private int gateway_server_backlog = 100;
	// 接收消息最大值  K为单位  10K
	private int gateway_server_rcvbuf = 10;
	// 发送消息最大值  K为单位  100K
	private int gateway_server_sndbuf = 100;
	// 心跳包读超时，单位秒
	private int gateway_server_reader_idle_time = 300;
	// 心跳包写超时，单位秒
	private int gateway_server_writer_idle_time = 300;
	// 心跳包读写超时，单位秒
	private int gateway_server_all_idle_time = 300;
	//低水位线  32K
	private int gateway_server_writeBufferLowWaterMark = 32;
	//高水位线 64K
	private int gateway_server_writeBufferHighWaterMark = 64;
	////////////////////////////////////////////////////gateway作为server的配置信息//////////////////////////



	////////////////////////////////////////////////////gateway作为sceneclient的配置////////////////////
	//gateway作为sceneClient的配置
	//接收消息最大值   K为单位  10M
	private int gateway_sceneClient_rcvbuf = 10000;
	// 发送消息最大值   K为单位  10M
	private int gateway_sceneClient_sndbuf = 10000;
	// 超时时间  S 为单位
	private int gateway_sceneClient_timeOut = 30;
	// gateway 和 每个scene之间的连接数
	private int gateway_sceneClient_channelNum = 4;
	//低水位线  K为单位   30M
	private int gateway_sceneClient_writeBufferLowWaterMark = 30720;
	//高水位线  K为单位   60M
	private int gateway_sceneClient_writeBufferHighWaterMark = 61440;
	////////////////////////////////////////////////////gateway作为sceneclient的配置////////////////////
	
	
	public void init(GatewayOtherConfig gatewayOtherConfig) throws Exception {
		
		this.gatewayId = gatewayOtherConfig.getGatewayId();
		
		////////////////////////////////////////////////////gateway作为server的配置信息//////////////////////////
		// gateway作为server的io线程数
		this.gateway_server_io_thread_num = gatewayOtherConfig.getGateway_server_io_thread_num();
		// 连接数
		this.gateway_server_backlog = gatewayOtherConfig.getGateway_server_backlog();
		// 接收消息最大值
		this.gateway_server_rcvbuf = gatewayOtherConfig.getGateway_server_rcvbuf();
		// 发送消息最大值
		this.gateway_server_sndbuf = gatewayOtherConfig.getGateway_server_sndbuf();
		// 心跳包读超时，单位秒
		this.gateway_server_reader_idle_time = gatewayOtherConfig.getGateway_server_reader_idle_time();
		// 心跳包写超时，单位秒
		this.gateway_server_writer_idle_time = gatewayOtherConfig.getGateway_server_writer_idle_time();
		// 心跳包读写超时，单位秒
		this.gateway_server_all_idle_time = gatewayOtherConfig.getGateway_server_all_idle_time();
		//低水位线  32K
		this.gateway_server_writeBufferLowWaterMark = gatewayOtherConfig.getGateway_server_writeBufferLowWaterMark();
		//高水位线 64K
		this.gateway_server_writeBufferHighWaterMark = gatewayOtherConfig.getGateway_server_writeBufferHighWaterMark();
		////////////////////////////////////////////////////gateway作为server的配置信息//////////////////////////


		////////////////////////////////////////////////////gateway作为sceneclient的配置////////////////////
		//gateway作为sceneClient的配置
		//接收消息最大值
		this.gateway_sceneClient_rcvbuf = gatewayOtherConfig.getGateway_sceneClient_rcvbuf();
		// 发送消息最大值
		this.gateway_sceneClient_sndbuf = gatewayOtherConfig.getGateway_sceneClient_sndbuf();
		// 超时时间
		this.gateway_sceneClient_timeOut = gatewayOtherConfig.getGateway_sceneClient_timeOut();
		// gateway 和 每个scene之间的连接数
		this.gateway_sceneClient_channelNum = gatewayOtherConfig.getGateway_sceneClient_channelNum();
		//低水位线  30M
		this.gateway_sceneClient_writeBufferLowWaterMark = gatewayOtherConfig.getGateway_sceneClient_writeBufferLowWaterMark();
		//高水位线 60M
		this.gateway_sceneClient_writeBufferHighWaterMark = gatewayOtherConfig.getGateway_sceneClient_writeBufferHighWaterMark();
	}

	
	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("gatewayId:" + gatewayId + ";  ");
		
		sb.append(" gatewayServerConfig[ gateway_server_io_thread_num : " + gateway_server_io_thread_num + " ;");
		sb.append(" gateway_server_backlog : " + gateway_server_backlog + " ;");
		sb.append(" gateway_server_rcvbuf : " + gateway_server_rcvbuf + " ;");
		sb.append(" gateway_server_sndbuf : " + gateway_server_sndbuf + " ;");
		sb.append(" gateway_server_reader_idle_time : " + gateway_server_reader_idle_time + " ;");
		sb.append(" gateway_server_writer_idle_time : " + gateway_server_writer_idle_time + " ;");
		sb.append(" gateway_server_all_idle_time : " + gateway_server_all_idle_time + " ;");
		sb.append(" gateway_server_writeBufferLowWaterMark : " + gateway_server_writeBufferLowWaterMark + " ;");
		sb.append(" gateway_server_writeBufferHighWaterMark : " + gateway_server_writeBufferHighWaterMark + " ];  ");
		
		
		sb.append(" gatewaySceneClientConfig[ gateway_sceneClient_rcvbuf : " + gateway_sceneClient_rcvbuf + " ;");
		sb.append(" gateway_sceneClient_sndbuf : " + gateway_sceneClient_sndbuf + " ;");
		sb.append(" gateway_sceneClient_timeOut : " + gateway_sceneClient_timeOut + " ;");
		sb.append(" gateway_sceneClient_channelNum : " + gateway_sceneClient_channelNum + " ;");
		sb.append(" gateway_sceneClient_writeBufferLowWaterMark : " + gateway_sceneClient_writeBufferLowWaterMark + " ;");
		sb.append(" gateway_sceneClient_writeBufferHighWaterMark : " + gateway_sceneClient_writeBufferHighWaterMark + " ]");
		
		
		return sb.toString();
	}

	/**
	 * @return the gatewayId
	 */
	public String getGatewayId() {
		return gatewayId;
	}


	/**
	 * @param gatewayId the gatewayId to set
	 */
	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}


	/**
	 * @return the gateway_server_io_thread_num
	 */
	public int getGateway_server_io_thread_num() {
		return gateway_server_io_thread_num;
	}


	/**
	 * @param gateway_server_io_thread_num the gateway_server_io_thread_num to set
	 */
	public void setGateway_server_io_thread_num(int gateway_server_io_thread_num) {
		this.gateway_server_io_thread_num = gateway_server_io_thread_num;
	}


	/**
	 * @return the gateway_server_backlog
	 */
	public int getGateway_server_backlog() {
		return gateway_server_backlog;
	}


	/**
	 * @param gateway_server_backlog the gateway_server_backlog to set
	 */
	public void setGateway_server_backlog(int gateway_server_backlog) {
		this.gateway_server_backlog = gateway_server_backlog;
	}


	/**
	 * @return the gateway_server_rcvbuf
	 */
	public int getGateway_server_rcvbuf() {
		return gateway_server_rcvbuf;
	}


	/**
	 * @param gateway_server_rcvbuf the gateway_server_rcvbuf to set
	 */
	public void setGateway_server_rcvbuf(int gateway_server_rcvbuf) {
		this.gateway_server_rcvbuf = gateway_server_rcvbuf;
	}


	/**
	 * @return the gateway_server_sndbuf
	 */
	public int getGateway_server_sndbuf() {
		return gateway_server_sndbuf;
	}


	/**
	 * @param gateway_server_sndbuf the gateway_server_sndbuf to set
	 */
	public void setGateway_server_sndbuf(int gateway_server_sndbuf) {
		this.gateway_server_sndbuf = gateway_server_sndbuf;
	}


	/**
	 * @return the gateway_server_reader_idle_time
	 */
	public int getGateway_server_reader_idle_time() {
		return gateway_server_reader_idle_time;
	}


	/**
	 * @param gateway_server_reader_idle_time the gateway_server_reader_idle_time to set
	 */
	public void setGateway_server_reader_idle_time(
			int gateway_server_reader_idle_time) {
		this.gateway_server_reader_idle_time = gateway_server_reader_idle_time;
	}


	/**
	 * @return the gateway_server_writer_idle_time
	 */
	public int getGateway_server_writer_idle_time() {
		return gateway_server_writer_idle_time;
	}


	/**
	 * @param gateway_server_writer_idle_time the gateway_server_writer_idle_time to set
	 */
	public void setGateway_server_writer_idle_time(
			int gateway_server_writer_idle_time) {
		this.gateway_server_writer_idle_time = gateway_server_writer_idle_time;
	}


	/**
	 * @return the gateway_server_all_idle_time
	 */
	public int getGateway_server_all_idle_time() {
		return gateway_server_all_idle_time;
	}


	/**
	 * @param gateway_server_all_idle_time the gateway_server_all_idle_time to set
	 */
	public void setGateway_server_all_idle_time(int gateway_server_all_idle_time) {
		this.gateway_server_all_idle_time = gateway_server_all_idle_time;
	}


	/**
	 * @return the gateway_server_writeBufferLowWaterMark
	 */
	public int getGateway_server_writeBufferLowWaterMark() {
		return gateway_server_writeBufferLowWaterMark;
	}


	/**
	 * @param gateway_server_writeBufferLowWaterMark the gateway_server_writeBufferLowWaterMark to set
	 */
	public void setGateway_server_writeBufferLowWaterMark(
			int gateway_server_writeBufferLowWaterMark) {
		this.gateway_server_writeBufferLowWaterMark = gateway_server_writeBufferLowWaterMark;
	}


	/**
	 * @return the gateway_server_writeBufferHighWaterMark
	 */
	public int getGateway_server_writeBufferHighWaterMark() {
		return gateway_server_writeBufferHighWaterMark;
	}


	/**
	 * @param gateway_server_writeBufferHighWaterMark the gateway_server_writeBufferHighWaterMark to set
	 */
	public void setGateway_server_writeBufferHighWaterMark(
			int gateway_server_writeBufferHighWaterMark) {
		this.gateway_server_writeBufferHighWaterMark = gateway_server_writeBufferHighWaterMark;
	}


	/**
	 * @return the gateway_sceneClient_rcvbuf
	 */
	public int getGateway_sceneClient_rcvbuf() {
		return gateway_sceneClient_rcvbuf;
	}


	/**
	 * @param gateway_sceneClient_rcvbuf the gateway_sceneClient_rcvbuf to set
	 */
	public void setGateway_sceneClient_rcvbuf(int gateway_sceneClient_rcvbuf) {
		this.gateway_sceneClient_rcvbuf = gateway_sceneClient_rcvbuf;
	}


	/**
	 * @return the gateway_sceneClient_sndbuf
	 */
	public int getGateway_sceneClient_sndbuf() {
		return gateway_sceneClient_sndbuf;
	}


	/**
	 * @param gateway_sceneClient_sndbuf the gateway_sceneClient_sndbuf to set
	 */
	public void setGateway_sceneClient_sndbuf(int gateway_sceneClient_sndbuf) {
		this.gateway_sceneClient_sndbuf = gateway_sceneClient_sndbuf;
	}


	/**
	 * @return the gateway_sceneClient_timeOut
	 */
	public int getGateway_sceneClient_timeOut() {
		return gateway_sceneClient_timeOut;
	}


	/**
	 * @param gateway_sceneClient_timeOut the gateway_sceneClient_timeOut to set
	 */
	public void setGateway_sceneClient_timeOut(int gateway_sceneClient_timeOut) {
		this.gateway_sceneClient_timeOut = gateway_sceneClient_timeOut;
	}


	/**
	 * @return the gateway_sceneClient_channelNum
	 */
	public int getGateway_sceneClient_channelNum() {
		return gateway_sceneClient_channelNum;
	}


	/**
	 * @param gateway_sceneClient_channelNum the gateway_sceneClient_channelNum to set
	 */
	public void setGateway_sceneClient_channelNum(int gateway_sceneClient_channelNum) {
		this.gateway_sceneClient_channelNum = gateway_sceneClient_channelNum;
	}


	/**
	 * @return the gateway_sceneClient_writeBufferLowWaterMark
	 */
	public int getGateway_sceneClient_writeBufferLowWaterMark() {
		return gateway_sceneClient_writeBufferLowWaterMark;
	}


	/**
	 * @param gateway_sceneClient_writeBufferLowWaterMark the gateway_sceneClient_writeBufferLowWaterMark to set
	 */
	public void setGateway_sceneClient_writeBufferLowWaterMark(
			int gateway_sceneClient_writeBufferLowWaterMark) {
		this.gateway_sceneClient_writeBufferLowWaterMark = gateway_sceneClient_writeBufferLowWaterMark;
	}


	/**
	 * @return the gateway_sceneClient_writeBufferHighWaterMark
	 */
	public int getGateway_sceneClient_writeBufferHighWaterMark() {
		return gateway_sceneClient_writeBufferHighWaterMark;
	}


	/**
	 * @param gateway_sceneClient_writeBufferHighWaterMark the gateway_sceneClient_writeBufferHighWaterMark to set
	 */
	public void setGateway_sceneClient_writeBufferHighWaterMark(
			int gateway_sceneClient_writeBufferHighWaterMark) {
		this.gateway_sceneClient_writeBufferHighWaterMark = gateway_sceneClient_writeBufferHighWaterMark;
	}
}
