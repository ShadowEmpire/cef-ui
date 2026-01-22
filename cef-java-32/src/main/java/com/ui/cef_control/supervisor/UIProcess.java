package com.ui.cef_control.supervisor;

public interface UIProcess {

	/**
	 * Start the UI process.
	 * Throws exception on failure.
	 */
	void start();

	/**
	 * Stop the UI process.
	 * Must be idempotent.
	 */
	void stop();

	/**
	 * sendCommand to the UI process.
	 * Must be idempotent.
	 */
	void sendCommand(String command);
}
