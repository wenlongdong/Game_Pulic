package com.hutong.framework.web.dispatch.view;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class ViewManager {

	/** ? */
	private View defaultView;

	/** ? */
	private Map<Class<? extends View>, View> viewMap = new HashMap<Class<? extends View>, View>();

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param viewClazz
	 * @return
	 */
	public View getView(Class<? extends View> viewClazz) {
		View view = viewMap.get(viewClazz);
		return null == view ? defaultView : view;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param viewClazz
	 * @param view
	 */
	public void addView(Class<? extends View> viewClazz, View view) {
		viewMap.put(viewClazz, view);

	}

}
