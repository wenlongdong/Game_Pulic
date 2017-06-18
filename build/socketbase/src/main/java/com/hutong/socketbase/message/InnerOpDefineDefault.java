package com.hutong.socketbase.message;


/**
 * @author Amyn
 * @description ?
 * 
 */
//框架内消息的编号 从  1-9999
public class InnerOpDefineDefault {

	public static final int DEFAULE_BEGIN = 0;
	
	public static final int SGFAILED = 99;
	public static final int SGSUCCESS = 100;

	public static final int GSRegistCode = 101;
	public static final int SGRegistCode = 102;
	
	//gate向scene发送的心跳消息
	public static final int GSPingCode = 103;
	public static final int SGPongCode = 104;
	
	//scene向gate发送的心跳消息
	public static final int SGPingCode = 105;
	public static final int GSPongCode = 106;
	
	public static final int GSPlayerDisconnect = 1001;
	public static final int SGPlayerDisconnect = 1002;
	
	//scene方主动断开gateway玩家的连接
	public static final int SGDisconnectPlayer = 1003;
	
	//gateway掉线，大多数是因为gateway重启了
	public static final int GSGatewayDisconnect = 1004;
	
	//scene到其他scene的消息
	public static final int SGSceneToSceneMsg = 1104;
	
	public static final int DEFAULE_END = 10000;//10000以后就是游戏逻辑需要使用的了
}
