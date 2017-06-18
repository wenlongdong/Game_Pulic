package com.hutong.framework.util;

import com.alibaba.fastjson.JSON;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class UtilJson {

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param o
	 * @return
	 */
	public static String O2S(Object o) {
		return JSON.toJSONString(o);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param s
	 * @param clazz
	 * @return
	 */
	public static <T> T S2O(String s, Class<T> clazz) {
		return JSON.parseObject(s, clazz);
	}
	
}
