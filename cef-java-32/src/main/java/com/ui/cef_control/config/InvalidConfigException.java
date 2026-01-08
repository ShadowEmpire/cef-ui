package com.anca.appl.fw.gui.cef_control.config;

public class InvalidConfigException extends RuntimeException {

	public InvalidConfigException(String message) {
		super(message);
	}

	public InvalidConfigException(String message, Throwable cause) {
		super(message, cause);
	}
}
