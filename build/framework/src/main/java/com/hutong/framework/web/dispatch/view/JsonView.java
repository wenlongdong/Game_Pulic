package com.hutong.framework.web.dispatch.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hutong.framework.HuTong;
import com.hutong.framework.util.UtilJson;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class JsonView implements View {

	@Override
	public void render(Object o, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding(HuTong.UTF8);
		response.setContentType(HuTong.APPLICATION_JSON);
		String o2s = UtilJson.O2S(o);
		response.getOutputStream().write(o2s.getBytes());
	}

}
