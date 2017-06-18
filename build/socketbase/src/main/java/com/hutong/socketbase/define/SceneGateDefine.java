/**
 * 
 */
package com.hutong.socketbase.define;

/**
 * @author DWL
 *	
 * scene 和 gate 公共的定义key放在这里
 *
 */
public class SceneGateDefine {
	
	//内部读写心跳时间
	public static final int INNER_READER_IDLE_TIME_SECONDS = 0;
	public static final int INNER_WRITER_IDLE_TIME_SECONDS = 0;
	public static final int INNER_ALL_IDLE_TIME_SECONDS = 30;
	
	// 秒 为单位  主要用于gate选择合适的scene 和 线
	public static final int INNER_SCENE_EXPIRE_TIME = 30;//scene信息的超时时间为30s  即在redis中保存的时间
	
	// 内部所有scene信息的redis集合  主要用于gateway和scene之间的连接
	public static final String INNER_ALL_SCENES_BRIFE_INFOS = "ALL_SCENES_BRIFE_INFOS";//所有scene 简要信息的集合
	public static final int INNER_ALL_SCENE_BRIFE_INFO_EXPIRE_TIME = 30;//scene简要信息的超时时间
	public static final int INNER_ALL_SCENES_BRIFE_INFOS_TIME_OUT = 30;//超时时间 S为单位
	
	// 内部所有gate信息的redis集合  主要用于短连接选择合适的gate
	public static final String INNER_ALL_GATES_INFOS = "ALL_GATES_BRIFE_INFOS";//所有gate 信息的集合
	public static final int INNER_ALL_GATES_INFOS_EXPIRE_TIME = 30;//超时时间 S为单位
	public static final int INNER_ALL_GATES_INFOS_TIME_OUT = 30;//超时时间 S为单位
	public static final int INNER_ALL_GATES_LOADINFO_TIME_OUT = 30;//超时时间 S为单位

}
