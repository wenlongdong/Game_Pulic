package com.hutong.framework.web.dispatch.annocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hutong.framework.web.dispatch.view.JsonView;
import com.hutong.framework.web.dispatch.view.View;

/**
 * @author Amyn
 * @description ?
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ProtocolURI {

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public String value();
	
	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public Class<? extends View> view() default JsonView.class;
}
