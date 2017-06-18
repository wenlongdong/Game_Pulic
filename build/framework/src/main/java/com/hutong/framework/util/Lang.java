package com.hutong.framework.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.NoSuchMechanismException;

import org.apache.commons.lang.StringUtils;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class Lang {

	/** ? */
	private static Map<Class<?>, Object> DEFAULT_VALUE_MAP = new HashMap<Class<?>, Object>();

	/** ? */
	private static Map<Class<?>, Class<?>> DEFAULT_WRAPPERCLASS_MAP = new HashMap<Class<?>, Class<?>>();

	/** ? */
	private static Map<Class<?>, Lang> cacheMap = new HashMap<Class<?>, Lang>();

	static {
		DEFAULT_VALUE_MAP.put(boolean.class, false);
		DEFAULT_VALUE_MAP.put(byte.class, 0);
		DEFAULT_VALUE_MAP.put(char.class, 0);
		DEFAULT_VALUE_MAP.put(short.class, 0);
		DEFAULT_VALUE_MAP.put(int.class, 0);
		DEFAULT_VALUE_MAP.put(long.class, 0);
		DEFAULT_VALUE_MAP.put(float.class, 0);
		DEFAULT_VALUE_MAP.put(double.class, 0);

		DEFAULT_WRAPPERCLASS_MAP.put(boolean.class, Boolean.class);
		DEFAULT_WRAPPERCLASS_MAP.put(int.class, Integer.class);
		DEFAULT_WRAPPERCLASS_MAP.put(byte.class, Byte.class);
		DEFAULT_WRAPPERCLASS_MAP.put(char.class, Character.class);
		DEFAULT_WRAPPERCLASS_MAP.put(short.class, Short.class);
		DEFAULT_WRAPPERCLASS_MAP.put(int.class, Integer.class);
		DEFAULT_WRAPPERCLASS_MAP.put(long.class, Long.class);
		DEFAULT_WRAPPERCLASS_MAP.put(float.class, Float.class);
		DEFAULT_WRAPPERCLASS_MAP.put(double.class, Double.class);
	}

	/** ? */
	private Class<?> clazz;

	private Lang(Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @return
	 */
	public static Lang getInstance(Class<?> clazz) {
		Lang lang = cacheMap.get(clazz);
		if (null == lang) {
			synchronized (cacheMap) {
				lang = cacheMap.get(clazz);
				if (null == lang) {
					lang = new Lang(clazz);
					cacheMap.put(clazz, lang);
				}
			}
		}
		return lang;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param src
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T castTo(Object src, Class<T> type) {
		if (null == src) {
			return (T) Lang.getInstance(type).getDefaultValue();
		}
		return castTo(src, src.getClass(), type);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param src
	 * @param fromType
	 * @param toType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, F> T castTo(Object src, Class<F> fromType, Class<T> toType) {
		if (fromType.getName().equals(toType.getName()))
			return (T) src;
		if (toType.isAssignableFrom(fromType))
			return (T) src;
		if (fromType == String.class) {
			return String2Object((String) src, toType);
		}

		if (fromType.isArray() && !toType.isArray()) {
			return castTo(Array.get(src, 0), toType);
		}

		if (fromType.isArray() && toType.isArray()) {
			int len = Array.getLength(src);
			Object result = Array.newInstance(toType.getComponentType(), len);
			for (int i = 0; i < len; i++) {
				Array.set(result, i, castTo(Array.get(src, i), toType.getComponentType()));
			}
			return (T) result;

		}

		return (T) Lang.getInstance(toType).getDefaultValue();
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param str
	 * @param toType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T String2Object(String str, Class<T> toType) {
		try {
			if (Lang.getInstance(toType).isBoolean()) {
				return (T) Boolean.valueOf(str);
			} else if (Lang.getInstance(toType).isByte()) {
				return (T) Byte.valueOf(str);
			} else if (Lang.getInstance(toType).isChar()) {
				return (T) Character.valueOf(str.charAt(0));
			} else if (Lang.getInstance(toType).isInt()) {
				return (T) Integer.valueOf(str);
			} else if (Lang.getInstance(toType).isFloat()) {
				return (T) Float.valueOf(str);
			} else if (Lang.getInstance(toType).isDouble()) {
				return (T) Double.valueOf(str);
			} else if (Lang.getInstance(toType).isLong()) {
				return (T) Long.valueOf(str);
			} else if (Lang.getInstance(toType).isShort()) {
				return (T) Short.valueOf(str);
			} else if (Lang.getInstance(toType).isString()) {
				return (T) str;
			} else if (Lang.getInstance(toType).isStringLike()) {
				return (T) str;
			} else {
				Constructor<T> constructor = (Constructor<T>) Lang.getInstance(toType).getWrapper().getConstructor(String.class);
				if (null != constructor) {
					return constructor.newInstance(str);
				}
			}
		} catch (Throwable t) {
		}
		return (T) Lang.getInstance(toType).getDefaultValue();
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @param anClass
	 * @return
	 */
	public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> anClass) {
		Class<?> cc = clazz;
		T annotation = null;
		while (null != cc && cc != Object.class) {
			annotation = cc.getAnnotation(anClass);
			if (null != annotation) {
				return annotation;
			}
			cc = cc.getSuperclass();
		}
		return null;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public Object getDefaultValue() {
		if (clazz.isPrimitive()) {
			return DEFAULT_VALUE_MAP.get(clazz);
		}

		return null;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public Class<?> getWrapper() {
		if (clazz.isPrimitive())
			return getWrapperClass();
		return clazz;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public Class<?> getWrapperClass() {
		Class<?> wrapperClazz = DEFAULT_WRAPPERCLASS_MAP.get(clazz);

		if (null == wrapperClazz)
			throw new AssertionError(clazz.getName() + " is not primitive type");
		return wrapperClazz;
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @return
	 */
	public boolean is(Class<?> clazz) {
		return is(this.clazz, clazz);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isBoolean() {
		return is(clazz, boolean.class) || is(clazz, Boolean.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isByte() {
		return is(clazz, byte.class) || is(clazz, Byte.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isChar() {
		return is(clazz, char.class) || is(clazz, Character.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isShort() {
		return is(clazz, short.class) || is(clazz, Short.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isInt() {
		return is(clazz, int.class) || is(clazz, Integer.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isLong() {
		return is(clazz, long.class) || is(clazz, Long.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isFloat() {
		return is(clazz, float.class) || is(clazz, Float.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isDouble() {
		return is(clazz, double.class) || is(clazz, Double.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isString() {
		return is(clazz, String.class);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @return
	 */
	public boolean isStringLike() {
		return CharSequence.class.isAssignableFrom(clazz);
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 */
	public static Method getGetter(Class<?> clazz, Field field) throws NoSuchMethodException {
		try {
			try {
				String fieldName = StringUtils.capitalize(field.getName());
				if (is(field.getType(), boolean.class) || is(field.getType(), Boolean.class)) {
					return clazz.getMethod("is" + fieldName);
				} else {
					return clazz.getMethod("get" + fieldName);
				}
			} catch (NoSuchMethodException e) {
				return clazz.getMethod(field.getName());
			}
		} catch (Exception e) {
			throw new NoSuchMechanismException();
		}
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param clazz
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 */
	public static Method getSetter(Class<?> clazz, Field field) throws NoSuchMethodException {
		try {
			try {
				return clazz.getMethod("set" + StringUtils.capitalize(field.getName()), field.getType());
			} catch (Exception e) {
				try {
					if (field.getName().startsWith("is") && (is(field.getType(), boolean.class) || is(field.getType(), Boolean.class))) {
						return clazz.getMethod("set" + field.getName().substring(2), field.getType());
					}
				} catch (Exception e1) {
				}
				return clazz.getMethod(field.getName(), field.getType());
			}
		} catch (RuntimeException e) {
			throw new NoSuchMechanismException();
		}
	}

	/**
	 * @author Amyn
	 * @description ?
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	private static boolean is(Class<?> source, Class<?> target) {
		return source == target;
	}

}
