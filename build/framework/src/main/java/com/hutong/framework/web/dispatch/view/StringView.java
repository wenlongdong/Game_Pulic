package com.hutong.framework.web.dispatch.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hutong.framework.HuTong;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class StringView implements View {

	@Override
	public void render(Object o, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding(HuTong.UTF8);
		response.setContentType(HuTong.TEXT_PLAIN);
		String o2s = o != null ? o.toString() : "null";
		response.getOutputStream().write(o2s.getBytes());
	}

}
