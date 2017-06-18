package com.hutong.framework;

/**
 * @author Amyn
 * @description ?
 * 
 */
public class ErrorException extends Throwable {

	private static final long serialVersionUID = 1L;

	/** 异常编号 */
	public int errorCode = -1;

	public ErrorException(int errorCode, Throwable r) {
		super(r);
		this.errorCode = errorCode;
	}

}
