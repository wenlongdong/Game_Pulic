package com.hutong.framework.web.dispatch;

import java.util.HashMap;
import java.util.Map;

import com.hutong.framework.base.dispatch.BaseDispatcher;
import com.hutong.framework.base.dispatch.invocation.InvocationFactory;
import com.hutong.framework.base.dispatch.invocation.PBInvocation;
import com.hutong.framework.base.dispatch.invocation.PBInvocationFactory;
import com.hutong.framework.web.dispatch.invocation.URIInvocation;
import com.hutong.framework.web.dispatch.invocation.URIInvocationFactory;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class WebDispatcher extends BaseDispatcher {
	
	protected Map<Class<?> ,InvocationFactory<?>> getActionFactories(){
		Map<Class<?> ,InvocationFactory<?>> maps = new HashMap<Class<?> ,InvocationFactory<?>>();
		maps.put(PBInvocation.class, new PBInvocationFactory());
		maps.put(URIInvocation.class, new URIInvocationFactory());
		return maps;
	}
}
