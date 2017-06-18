package com.hutong.framework.web.dispatch.invocation;

import java.lang.reflect.Method;

import com.hutong.framework.base.dispatch.invocation.Invocation;
import com.hutong.framework.web.dispatch.view.View;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class URIInvocation extends Invocation {

	/** ? */
	private View view;
	
	public URIInvocation(Object target, Method method) {
		super(target, method);
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}
}
