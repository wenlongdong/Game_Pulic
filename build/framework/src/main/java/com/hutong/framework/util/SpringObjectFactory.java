package com.hutong.framework.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class SpringObjectFactory extends ObjectFactory implements ApplicationContextAware {

	private static final Log LOG = LogFactory.getLog(SpringObjectFactory.class);

	protected ApplicationContext appContext;
	protected AutowireCapableBeanFactory autoWiringFactory;
	protected int autowireStrategy = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
	private boolean alwaysRespectAutowireStrategy = false;

	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
		autoWiringFactory = findAutoWiringBeanFactory(this.appContext);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param autowireStrategy
	 */
	public void setAutowireStrategy(int autowireStrategy) {
		switch (autowireStrategy) {
		case AutowireCapableBeanFactory.AUTOWIRE_BY_NAME:
			LOG.info("Setting autowire strategy to name");
			this.autowireStrategy = autowireStrategy;
			break;
		case AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE:
			LOG.info("Setting autowire strategy to type");
			this.autowireStrategy = autowireStrategy;
			break;
		case AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR:
			LOG.info("Setting autowire strategy to constructor");
			this.autowireStrategy = autowireStrategy;
			break;
		case AutowireCapableBeanFactory.AUTOWIRE_NO:
			LOG.info("Setting autowire strategy to none");
			this.autowireStrategy = autowireStrategy;
			break;
		default:
			throw new IllegalStateException("Invalid autowire type set");
		}
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public int getAutowireStrategy() {
		return autowireStrategy;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param context
	 * @return
	 */
	protected AutowireCapableBeanFactory findAutoWiringBeanFactory(ApplicationContext context) {
		if (context instanceof AutowireCapableBeanFactory) {
			return (AutowireCapableBeanFactory) context;
		} else if (context instanceof ConfigurableApplicationContext) {
			return ((ConfigurableApplicationContext) context).getBeanFactory();
		} else if (context.getParent() != null) {
			return findAutoWiringBeanFactory(context.getParent());
		}
		return null;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public Object buildBean(Class<?> clazz) throws Exception {
		Object o = null;
		try {
			o = appContext.getBean(clazz);
		} catch (NoSuchBeanDefinitionException e) {
			o = _buildBean(clazz);
		}
		return o;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public Object _buildBean(Class<?> clazz) throws Exception {
		Object bean;

		try {
			if (alwaysRespectAutowireStrategy) {
				bean = autoWiringFactory.createBean(clazz, autowireStrategy, false);
				return bean;
			} else {
				bean = autoWiringFactory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
				bean = autoWiringFactory.applyBeanPostProcessorsBeforeInitialization(bean, bean.getClass().getName());
				bean = autoWiringFactory.applyBeanPostProcessorsAfterInitialization(bean, bean.getClass().getName());
				return autoWireBean(bean, autoWiringFactory);
			}
		} catch (UnsatisfiedDependencyException e) {
			if (LOG.isErrorEnabled())
				LOG.error("Error building bean", e);
			return autoWireBean(clazz.newInstance(), autoWiringFactory);
		}
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param bean
	 * @return
	 */
	public Object autoWireBean(Object bean) {
		return autoWireBean(bean, autoWiringFactory);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param bean
	 * @param autoWiringFactory
	 * @return
	 */
	public Object autoWireBean(Object bean, AutowireCapableBeanFactory autoWiringFactory) {
		if (autoWiringFactory != null) {
			autoWiringFactory.autowireBeanProperties(bean, autowireStrategy, false);
		}
		injectApplicationContext(bean);
		return bean;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param bean
	 */
	private void injectApplicationContext(Object bean) {
		if (bean instanceof ApplicationContextAware) {
			((ApplicationContextAware) bean).setApplicationContext(appContext);
		}
	}

}
