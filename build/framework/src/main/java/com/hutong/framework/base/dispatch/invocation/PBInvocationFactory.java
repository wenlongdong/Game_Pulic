package com.hutong.framework.base.dispatch.invocation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hutong.framework.base.dispatch.annocation.ProtocolPB;
import com.hutong.framework.util.ObjectFactory;

public class PBInvocationFactory implements InvocationFactory<PBInvocation>{
	private static final Log LOG = LogFactory.getLog(PBInvocationFactory.class);
	/** ? */
	protected static Map<Integer, PBInvocation> protocolHandleMap = new HashMap<Integer, PBInvocation>();

	@Override
	public void create(Class<?> clazz, Object obj, Method method, ObjectFactory objectFactory) throws Exception {
		ProtocolPB protocol = method.getAnnotation(ProtocolPB.class);
		if (null != protocol) {
			int code = protocol.value();
			PBInvocation ai = createPBActionInvocation(obj, method);
			if (protocolHandleMap.containsKey(code)) {
				throw new Exception("exists same code " + code + ", handler1:[" + protocolHandleMap.get(code).getActionName() + " " + protocolHandleMap.get(code).getMethodName() + "], handler2:[" + ai.getActionName() + " " + ai.getMethodName() + "]");
			}

			protocolHandleMap.put(code, ai);
			LOG.info("found ProtocolPB handler [code:" + code + ", handler:" + clazz.getName() + " " + method.getName() + "]");
		}
	}

	private static PBInvocation createPBActionInvocation(Object obj, Method method) {
		PBInvocation action = new PBInvocation(obj,method);
		action.init();
		return action;
	}

	public PBInvocation get(Serializable code) {
		return protocolHandleMap.get(code);
	}

}
