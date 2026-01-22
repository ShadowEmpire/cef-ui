package com.ui.cef_control.http;

import com.ui.cef_control.supervisor.RetryPolicy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Supervisor for the VuePress HTTP server lifecycle.
 *
 * Phase-6 Section 1: Manages server restarts and crash recovery.
 *
 * Responsibilities:
 * - Start the HTTP server and handle startup failures
 * - Restart the server on crashes (e.g., port conflicts, OOM)
 * - Notify listeners (via HttpServerListener) before and after restarts
 * - Use exponential backoff for restart attempts (Phase-6 functional, not Phase-7 hardening)
 * - Maintain deterministic lifecycle: never mutate server from outside
 *
 * Lifecycle:
 * 1. Create supervisor with server and retry policy
 * 2. Add listeners to be notified of restart events
 * 3. Call start() -> server starts, listeners notified on success/failure
 * 4. Server runs independently; supervisor is passive observer
 * 5. If server crashes, supervisor can orchestrate restart (future integration)
 * 6. Call stop() -> graceful shutdown
 *
 * Design constraints:
 * - Java owns the lifecycle
 * - Server is a black box; supervisor only calls start/stop/getActualAddress
 * - No security/auth logic (Phase-7)
 * - No metrics/logging beyond basic diagnostics (Phase-7)
 * - Simple exponential backoff only (Phase-7: advanced retry strategies)
 *
 * Integration with ProcessController:
 * - Phase-7: HttpServerSupervisor will implement LifecycleListener
 *   to be notified of CEF process events and coordinate restarts
 * - Phase-6: Standalone, purely functional
 */
public class HttpServerSupervisor {

	private final VuePressHttpServer server;
	private final RetryPolicy retryPolicy;
	private final List<HttpServerListener> listeners;
	private boolean serverRunning;

	/**
	 * Creates supervisor for HTTP server.
	 *
	 * @param server The HTTP server to supervise
	 * @param retryPolicy Policy for restart retries (exponential backoff, max attempts)
	 */
	public HttpServerSupervisor(VuePressHttpServer server, RetryPolicy retryPolicy) {
		if (server == null) {
			throw new IllegalArgumentException("server cannot be null");
		}
		if (retryPolicy == null) {
			throw new IllegalArgumentException("retryPolicy cannot be null");
		}

		this.server = server;
		this.retryPolicy = retryPolicy;
		this.listeners = new ArrayList<>();
		this.serverRunning = false;
	}

	/**
	 * Starts the HTTP server with automatic retry on failure.
	 *
	 * Attempts to start the server; if it fails, retries according to the policy.
	 * Notifies listeners of success or final failure via HttpServerListener callbacks.
	 *
	 * Phase-6: Simple exponential backoff and notify-on-restart.
	 * Phase-7: Deferred (metrics, advanced recovery, external service registration).
	 */
	public void start() {
		int attempt = 1;

		while (true) {
			try {
				// Notify listeners that restart is about to happen
				notifyBeforeRestart();

				// Perform actual start
				server.start();

				// Mark as running
				this.serverRunning = true;

				// Get new address and notify listeners
				String actualAddress = server.getActualAddress();
				notifyAfterRestart(actualAddress);

				return; // Success

			} catch (IOException e) {
				// Notify listeners of failure
				notifyStartFailure(e);

				// Check retry policy
				if (!retryPolicy.shouldRetry(attempt, e)) {
					// Retries exhausted
					throw new RuntimeException(
							"Failed to start HTTP server after " + attempt + " attempts",
							e
					);
				}

				// Wait before retry (backoff is handled by RetryPolicy)
				long backoffMs = retryPolicy.getBackoffMs(attempt);
				try {
					Thread.sleep(backoffMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Interrupted while waiting for server restart", ie);
				}

				attempt++;
			}
		}
	}

	/**
	 * Stops the HTTP server gracefully.
	 *
	 * @throws IllegalStateException if server is not running
	 */
	public void stop() {
		if (!serverRunning) {
			throw new IllegalStateException("Server is not running");
		}

		server.stop();
		this.serverRunning = false;
	}

	/**
	 * Returns true if the server is currently running.
	 *
	 * @return Server running state
	 */
	public boolean isServerRunning() {
		return serverRunning;
	}

	/**
	 * Gets the current server address (host:port).
	 *
	 * @return Address string or null if not running
	 */
	public String getServerAddress() {
		return server.getActualAddress();
	}

	/**
	 * Gets the current server port.
	 *
	 * @return Port number or -1 if not running
	 */
	public int getServerPort() {
		return server.getActualPort();
	}

	/**
	 * Adds a listener to receive restart notifications.
	 *
	 * @param listener The listener
	 */
	public void addListener(HttpServerListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
		// Also add to underlying server so it gets notified directly
		server.addListener(listener);
	}

	/**
	 * Removes a listener.
	 *
	 * @param listener The listener
	 */
	public void removeListener(HttpServerListener listener) {
		listeners.remove(listener);
		server.removeListener(listener);
	}

	// Internal notification methods

	private void notifyBeforeRestart() {
		List<HttpServerListener> snapshot = new ArrayList<>(listeners);
		for (HttpServerListener listener : snapshot) {
			try {
				listener.onBeforeRestart();
			} catch (Exception e) {
				System.err.println("HttpServerListener.onBeforeRestart() threw exception: " + e);
				e.printStackTrace();
			}
		}
	}

	private void notifyAfterRestart(String newAddress) {
		List<HttpServerListener> snapshot = new ArrayList<>(listeners);
		for (HttpServerListener listener : snapshot) {
			try {
				listener.onAfterRestart(newAddress);
			} catch (Exception e) {
				System.err.println("HttpServerListener.onAfterRestart() threw exception: " + e);
				e.printStackTrace();
			}
		}
	}

	private void notifyStartFailure(Throwable error) {
		List<HttpServerListener> snapshot = new ArrayList<>(listeners);
		for (HttpServerListener listener : snapshot) {
			try {
				listener.onStartFailure(error);
			} catch (Exception e) {
				System.err.println("HttpServerListener.onStartFailure() threw exception: " + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "HttpServerSupervisor{" +
				"serverRunning=" + serverRunning +
				", serverAddress=" + getServerAddress() +
				'}';
	}
}

