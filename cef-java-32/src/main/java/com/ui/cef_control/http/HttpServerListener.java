package com.ui.cef_control.http;

/**
 * Listener for HTTP server lifecycle events.
 *
 * Phase-6 Section 1: Notifies CEF (via IPC) before and after server restart.
 * Java owns the lifecycle; listeners are passive observers.
 *
 * Implementation note: Listeners must NOT throw exceptions.
 * Listeners must NOT mutate server state.
 */
public interface HttpServerListener {

	/**
	 * Called immediately before the server restarts.
	 * Use this to notify CEF that the UI will be temporarily unavailable.
	 *
	 * Phase-6 concern: Functional notification only.
	 * Phase-7 concern: Deferred (auth/security around this notification).
	 */
	void onBeforeRestart();

	/**
	 * Called immediately after the server restarts successfully.
	 * Use this to notify CEF of the new server address and to resume operations.
	 *
	 * @param newAddress The new address (host:port) where the server is accessible
	 *                   Example: "127.0.0.1:54321"
	 *
	 * Phase-6 concern: Functional notification with new address.
	 * Phase-7 concern: Deferred (secure re-sync, re-auth).
	 */
	void onAfterRestart(String newAddress);

	/**
	 * Called when the server fails to start or restart.
	 * Useful for diagnostics; actual recovery is handled by supervisor.
	 *
	 * @param error The exception that caused the failure
	 */
	void onStartFailure(Throwable error);
}

