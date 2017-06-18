package com.hutong.socketbase.conf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class Config {

	/** ? */
	private String log4jConf;
	/** ? */
	private List<String> springConfList;

	public Config(String log4jConfig, List<String> springConfList) {
		this.log4jConf = log4jConfig;
		this.springConfList = springConfList;
	}

	public Config(String log4jConfig, String... springConfArray) {
		List<String> springConfList = new ArrayList<String>();
		for (String springConf : springConfArray) {
			springConfList.add(springConf);
		}
		this.log4jConf = log4jConfig;
		this.springConfList = springConfList;
	}

	/**
	 * @return the log4jConf
	 */
	public String getLog4jConf() {
		return log4jConf;
	}

	/**
	 * @param log4jConf the log4jConf to set
	 */
	public void setLog4jConf(String log4jConf) {
		this.log4jConf = log4jConf;
	}

	/**
	 * @return the springConfList
	 */
	public List<String> getSpringConfList() {
		return springConfList;
	}

	/**
	 * @param springConfList the springConfList to set
	 */
	public void setSpringConfList(List<String> springConfList) {
		this.springConfList = springConfList;
	}
}
