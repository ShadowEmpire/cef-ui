package com.ui.cef_control.ipc;

/**
 * Listener for WebSocket connection lifecycle events.
 *
 * Implemented by clients that need to be notified of connection
 * state changes and errors.
 */
public interface ConnectionListener {

	/**
	 * Called when the WebSocket connection is successfully established.
	 */
	void onConnected();

	/**
	 * Called when the WebSocket connection is closed.
	 */
	void onDisconnected();

	/**
	 * Called when an error occurs during connection or communication.
	 *
	 * @param error the error that occurred
	 */
	void onError(Throwable error);
}