package com.hutong.framework.web.dispatch.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Amyn
 * @description ?
 * 
 */
public interface View {
	
	void render(Object o, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
}
