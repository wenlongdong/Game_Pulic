package com.hutong.framework.base.dispatch.annocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ProtocolPB {
	
	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public int value();
	
}
