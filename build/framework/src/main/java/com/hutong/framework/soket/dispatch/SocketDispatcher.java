package com.hutong.framework.soket.dispatch;

import java.util.HashMap;
import java.util.Map;

import com.hutong.framework.base.dispatch.BaseDispatcher;
import com.hutong.framework.base.dispatch.invocation.InvocationFactory;
import com.hutong.framework.base.dispatch.invocation.PBInvocation;
import com.hutong.framework.base.dispatch.invocation.PBInvocationFactory;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class SocketDispatcher extends BaseDispatcher {
	
	protected Map<Class<?> ,InvocationFactory<?>> getActionFactories(){
		if(null == actionFactories){
			actionFactories = new HashMap<Class<?> ,InvocationFactory<?>>();
			actionFactories.put(PBInvocation.class, new PBInvocationFactory());
		}
		return actionFactories;
	}
}
