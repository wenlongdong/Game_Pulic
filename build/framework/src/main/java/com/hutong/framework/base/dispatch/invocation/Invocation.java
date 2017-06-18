package com.hutong.framework.base.dispatch.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Amyn
 * @description ?
 * 
 */
public abstract class Invocation {

	/** ? */
	private Object target;

	/** ? */
	private Method method;	
		
	public Invocation(Object target, Method method) {
		this.target = target;
		this.method = method;
	}

	public void init() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object invoke(Object... args) throws Exception {
		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			throw t instanceof Exception ? (Exception) t : e;
		}
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public String getActionName() {
		return target.getClass().getSimpleName();
	}

	public String getMethodName() {
		return method.getName();
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object obj) {
		this.target = obj;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

}
