package com.hutong.framework.util;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class ObjectFactory {

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public Object buildBean(Class<?> clazz) throws Exception {
		Object o = null;
		o = clazz.newInstance();
		return o;
	}

}
