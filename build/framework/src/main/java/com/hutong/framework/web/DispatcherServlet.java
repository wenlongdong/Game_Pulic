package com.hutong.framework.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.hutong.framework.util.ObjectFactory;
import com.hutong.framework.util.SpringObjectFactory;
import com.hutong.framework.web.dispatch.WebDispatcher;
import com.hutong.framework.web.dispatch.invocation.URIInvocation;
import com.hutong.framework.web.dispatch.view.View;

/**
 * @author Amyn
 * @description ?
 * 
 */
public abstract class DispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(DispatcherServlet.class);

	/** ? */
	protected WebDispatcher dispatcherBase = new WebDispatcher();

	/** ? */
	protected static ObjectFactory objectFactory = null;

	/** ? */
	protected WebApplicationContext requiredWebApplicationContext;
	
	/** ? */
	private String actionPath;

	@Override
	public void init() throws ServletException {
		System.out.println("init");
		LOG.info("Init DispatcherServlet start ......");

		try {
			requiredWebApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

			actionPath = (String) requiredWebApplicationContext.getBean("actionPath");
			if (StringUtils.isBlank(actionPath)) {
				LOG.error(" can not found actionPath ......");
				System.exit(1);
			}

			dispatcherBase.initHandleAction(actionPath, getFactory());
		} catch (Exception e) {
			LOG.error("Init DispatcherServlet error ......", e);
			System.exit(1);
		}

		LOG.info("Init DispatcherServlet end ......");

		super.init();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String uri = req.getPathInfo();
//		if (RequestMethod.POST.toString().equals(req.getMethod())) {
			if (StringUtils.isBlank(uri)) {
				doInvokeProtocolPB(req, res);
			} else {
				doInvokeProtocolDefault(req, res, uri);
			}
//		} else {
//			LOG.warn(" some request not use POST...... " + uri);
//		}
	}

	protected abstract void doInvokeProtocolPB(HttpServletRequest req, HttpServletResponse res);
	
	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param req
	 * @param res
	 * @param uri
	 */
	protected void doInvokeProtocolDefault(HttpServletRequest req, HttpServletResponse res, String uri) {
		try {
			URIInvocation action = dispatcherBase.getActionInvocation(URIInvocation.class, uri);
			if (null != action) {
				Map<String, String> params = new HashMap<String, String>();
				Enumeration<String> parameterNames = req.getParameterNames();
				while (parameterNames.hasMoreElements()) {
					String nextElement = parameterNames.nextElement();
					String value = req.getParameter(nextElement);
					params.put(nextElement, value);
				}
				Object invoke = null;
				if (MapUtils.isEmpty(params)) {
					invoke = action.invoke();
				} else {
					invoke = action.invoke(params);
				}
				if (null != invoke) {
					View view = action.getView();
					view.render(invoke, req, res);
				} else {

				}
			} else {
				LOG.debug(" no ActionHTTPInvocation found by uri : " + uri);
			}
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}


	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	protected ObjectFactory getFactory() {
		if (null == objectFactory) {
			SpringObjectFactory factory = new SpringObjectFactory();
			ApplicationContext applicationContext = (ApplicationContext) getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
			if (applicationContext == null) {
				LOG.info("ApplicationContext could not be found. Action classes will not be autowired.");
				objectFactory = new ObjectFactory();
			} else {
				factory.setApplicationContext(applicationContext);
				objectFactory = factory;
			}
		}
		return objectFactory;
	}

	@Override
	public void destroy() {
		System.out.println("destroy");
		super.destroy();
	}

}
