package com.hutong.framework.base.dispatch.invocation;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.hutong.framework.util.ObjectFactory;

public interface InvocationFactory<T extends Invocation> {

	void create(Class<?> clazz, Object obj, Method method,
			ObjectFactory objectFactory) throws Exception;

	T get(Serializable code);

}
