package com.hutong.scene.interf;

public class SceneHttpInterceptExeption extends Throwable {
	
	private static final long serialVersionUID = 4199170453545994840L;

	public SceneHttpInterceptExeption(String exception) {
		super(exception);
	}
	
	public SceneHttpInterceptExeption(String exception, Throwable e) {
		super(exception, e);
	}
}