package com.hutong.framework.web.dispatch.invocation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hutong.framework.base.dispatch.invocation.InvocationFactory;
import com.hutong.framework.util.ObjectFactory;
import com.hutong.framework.web.dispatch.annocation.ProtocolURI;
import com.hutong.framework.web.dispatch.view.View;
import com.hutong.framework.web.dispatch.view.ViewManager;

public class URIInvocationFactory implements InvocationFactory<URIInvocation>{
	private static final Log LOG = LogFactory.getLog(URIInvocationFactory.class);
	/** ? */
	protected static Map<String, URIInvocation> protocolHandleMap = new HashMap<String, URIInvocation>();
	
	/** ? */
	private static ViewManager viewManager = new ViewManager();

	public void create(Class<?> clazz, Object obj, Method method, ObjectFactory objectFactory) throws Exception {
		ProtocolURI protocol = method.getAnnotation(ProtocolURI.class);
		if (null != protocol) {
			String uri = protocol.value();
			Class<? extends View> viewClazz = protocol.view();
			View view = viewManager.getView(viewClazz);
			if (view == null) {
				view = (View) objectFactory.buildBean(viewClazz);
				viewManager.addView(viewClazz, view);
			}

			URIInvocation ai = createURIActionInvocation(obj, method, view);
			if (protocolHandleMap.containsKey(uri)) {
				throw new Exception("exists same uri " + uri + ", handler1:[" + protocolHandleMap.get(uri).getActionName() + " " + protocolHandleMap.get(uri).getMethodName() + "], handler2:[" + ai.getActionName() + " " + ai.getMethodName() + "]");
			}

			protocolHandleMap.put(uri, ai);
			LOG.info("found ProtocolDefault handler [uri:" + uri + ", handler:" + clazz.getName() + " " + method.getName() + "]");
		}
		
	}
	
	private static URIInvocation createURIActionInvocation(Object obj, Method method, View view) {
		URIInvocation action = new URIInvocation(obj,method);
		action.setView(view);
		action.init();
		return action;
	}

	public URIInvocation get(Serializable uri) {
		return protocolHandleMap.get(uri);
	}
}
