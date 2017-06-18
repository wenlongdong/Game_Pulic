package com.hutong.framework.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.hutong.framework.HuTong;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class UtilHttp {

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param url
	 * @param paramMap
	 * @param timeout
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String httpPost(String url, Map<String, String> paramMap, int timeout) throws HttpException, IOException {

		List<NameValuePair> valueList = new ArrayList<NameValuePair>();
		Iterator<String> it = paramMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = paramMap.get(key);
			NameValuePair nv = new NameValuePair(key, value);
			valueList.add(nv);
		}

		NameValuePair[] valueArray = valueList.toArray(new NameValuePair[0]);

		String response = null;
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		PostMethod method = new PostMethod(url);

		method.getParams().setContentCharset(HuTong.UTF8);
		if (timeout > 0) {
			client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
		}
		method.setRequestBody(valueArray);

		int status = client.executeMethod(method);
		if (status != HttpStatus.SC_OK) {
			return response;
		}

		response = method.getResponseBodyAsString();

		return response;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param url
	 * @param paramMap
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String httpPost(String url, Map<String, String> paramMap) throws HttpException, IOException {
		return httpPost(url, paramMap, HuTong.TIME_OUT);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param url
	 * @param paramMap
	 * @param cookie
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String httpPost(String url, Map<String, String> paramMap, String cookie) throws HttpException, IOException {

		List<NameValuePair> valueList = new ArrayList<NameValuePair>();
		Iterator<String> it = paramMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = paramMap.get(key);
			NameValuePair nv = new NameValuePair(key, value);
			valueList.add(nv);
		}

		NameValuePair[] valueArray = valueList.toArray(new NameValuePair[0]);

		String response = null;
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

		PostMethod method = new PostMethod(url);

		method.setRequestHeader(HuTong.COOKIE, cookie);

		method.getParams().setContentCharset(HuTong.UTF8);
		method.setRequestBody(valueArray);

		int status = client.executeMethod(method);
		if (status != HttpStatus.SC_OK) {
			return response;
		}

		response = method.getResponseBodyAsString();

		return response;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param host
	 * @param port
	 * @param url
	 * @param data
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String httpPostData(String host, int port, String url, String data) throws HttpException, IOException {

		String response = null;
		HttpClient client = new HttpClient();
		HostConfiguration hostconf = new HostConfiguration();
		hostconf.setHost(host, port);
		client.setHostConfiguration(hostconf);
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		PostMethod method = new PostMethod(url);

		RequestEntity stringEntity = new StringRequestEntity(data, HuTong.TEXT_PLAIN, HuTong.UTF8);
		method.setRequestEntity(stringEntity);

		int status = client.executeMethod(method);
		if (status != HttpStatus.SC_OK) {
			return response;
		}

		response = method.getResponseBodyAsString();

		return response;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param url
	 * @param data
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String httpPost(String url, String data) throws HttpException, IOException {

		String response = null;
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		PostMethod method = new PostMethod(url);

		RequestEntity stringEntity = new StringRequestEntity(data, HuTong.APPLICATION_JSON, HuTong.UTF8);
		method.setRequestEntity(stringEntity);

		int status = client.executeMethod(method);
		if (status != HttpStatus.SC_OK) {
			return response;
		}

		response = method.getResponseBodyAsString();

		return response;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String httpGet(String url) throws HttpException, IOException {

		String response = null;
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		GetMethod method = new GetMethod(url);

		int status = client.executeMethod(method);
		if (status != HttpStatus.SC_OK) {
			return response;
		}

		response = method.getResponseBodyAsString();

		return response;
	}

}
