package com.ui.cef_control.ipc;

public final class MessageTypes {

	public static final String HELLO = "HELLO";

	private MessageTypes() {
		// Utility class - prevent instantiation
	}
    
    public static boolean isValid(String messageType) {
        if (messageType == null) {
            return false;
        }
        return HELLO.equals(messageType);
    }
}