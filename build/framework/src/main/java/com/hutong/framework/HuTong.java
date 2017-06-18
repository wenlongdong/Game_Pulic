package com.hutong.framework;

import java.nio.charset.Charset;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class HuTong {

	/** 编码格式 */
	public static final String UTF8 = "UTF-8";
	
	/** 编码格式 */
	public static final Charset CHARSET = Charset.forName(UTF8);
	
	/** 超时时间 */
	public static final int TIME_OUT = 1000 * 3;
	public static final String COOKIE = "Cookie";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String APPLICATION_JSON = "application/json";

	/** CPU核数 */
	public static final int PROCCESSOR_NUM = Runtime.getRuntime().availableProcessors();
	
	/* 短连接的session定义 长连接也会用 */
	public static final String REDIS_CLIENT_SESSION_KEY = "client_session";
	
	public String PROJECT_NAME = "";
	
}
