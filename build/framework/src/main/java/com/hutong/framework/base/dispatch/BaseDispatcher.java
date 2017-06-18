package com.hutong.framework.base.dispatch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hutong.framework.base.dispatch.annocation.Action;
import com.hutong.framework.base.dispatch.invocation.Invocation;
import com.hutong.framework.base.dispatch.invocation.InvocationFactory;
import com.hutong.framework.util.Lang;
import com.hutong.framework.util.ObjectFactory;
import com.hutong.framework.util.Scans;

/**
 * @author Amyn
 * @description ?
 * 
 */
public abstract class BaseDispatcher {

	private static final Log LOG = LogFactory.getLog(BaseDispatcher.class);

	/** ? */
	protected ObjectFactory objectFactory = null;

	protected Map<Class<?> ,InvocationFactory<?>> actionFactories = null;
	
	/**
	 * 
	 * @return
	 */
	protected abstract Map<Class<?> ,InvocationFactory<?>> getActionFactories();

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param path
	 * @param objectFactory
	 * @throws Exception
	 */
	public void initHandleAction(String path, ObjectFactory objectFactory) throws Exception {
		
		this.actionFactories = this.getActionFactories();
		this.objectFactory = objectFactory;
		Set<Class<?>> set = Scans.getClasses(path);
		for (Class<?> clazz : set) {
			if (!Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
				Action action = Lang.getAnnotation(clazz, Action.class);
				if (null != action) {
					Object obj = createObject(clazz);
					Method[] methods = clazz.getDeclaredMethods();
					for (Method method : methods) {
						if (!isStaticMethod(method)) {
							this.initActionInvocation(clazz, obj, method);
						}
					}
				}
			}
		}
		
		LOG.info("Action init end");
	}

	private void initActionInvocation(Class<?> clazz, Object obj, Method method) throws Exception {
		for (InvocationFactory<?> factory : actionFactories.values()) {
			factory.create(clazz, obj, method, this.objectFactory);
		}
	}
	@SuppressWarnings("unchecked")
	public <T extends Invocation> T getActionInvocation(Class<T> clazz,Serializable code) {
		return (T) this.actionFactories.get(clazz).get(code);
	}
	

	protected Object createObject(Class<?> clazz) throws Exception {
		return objectFactory.buildBean(clazz);
	}
	
	private static boolean isStaticMethod(Method method) {
		return Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers());
	}

}
