package com.ui.cef_control.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Embedded HTTP server for serving prebuilt VuePress static documentation.
 *
 * Phase-6 Section 1: Core HTTP server implementation.
 *
 * Responsibilities:
 * - Start/stop the server
 * - Serve static files from configured path
 * - Bind exclusively to 127.0.0.1 (localhost)
 * - Use ephemeral port (0 = OS-assigned)
 * - Notify listeners of lifecycle events (before/after restart, failure)
 * - Simple routing: serve index.html for non-file paths
 *
 * Design constraints:
 * - No HTTPS (Phase-7)
 * - No caching headers (Phase-7)
 * - No gzip/compression (Phase-7)
 * - No security logic (Phase-7)
 * - No authentication (Phase-7)
 * - No rate limiting (Phase-7)
 *
 * Dependencies:
 * - JDK built-in com.sun.net.httpserver.HttpServer (no external HTTP library)
 * - Standard Java NIO for file serving
 *
 * Lifecycle:
 * 1. Create instance with config
 * 2. Add listeners (optional)
 * 3. Call start() -> server binds, accepts requests, notifies listeners
 * 4. Server serves files and handles restarts independently
 * 5. Call stop() -> server stops, cleans up resources
 */
public class VuePressHttpServer {

	private final HttpServerConfig config;
	private final List<HttpServerListener> listeners;
	private com.sun.net.httpserver.HttpServer httpServer;
	private boolean running;

	/**
	 * Creates an HTTP server instance (not started yet).
	 *
	 * @param config Configuration with paths and port
	 */
	public VuePressHttpServer(HttpServerConfig config) {
		if (config == null) {
			throw new IllegalArgumentException("config cannot be null");
		}
		this.config = config;
		this.listeners = new ArrayList<>();
		this.httpServer = null;
		this.running = false;
	}

	/**
	 * Starts the HTTP server.
	 *
	 * Binds to 127.0.0.1:{port} where port is from config.
	 * If config.port == 0, OS assigns an ephemeral port.
	 * Sets up static file handler and index.html fallback.
	 * Notifies listeners after successful start.
	 *
	 * @throws IOException if bind fails or static files path is invalid
	 * @throws IllegalStateException if server is already running
	 */
	public void start() throws IOException {
		if (running) {
			throw new IllegalStateException("Server is already running");
		}

		// Validate static files path exists
		Path staticPath = Paths.get(config.getStaticFilesPath());
		if (!Files.isDirectory(staticPath)) {
			throw new IOException("Static files path does not exist or is not a directory: " + staticPath);
		}

		// Create server bound to localhost only
		InetSocketAddress bindAddress = new InetSocketAddress(config.getBindAddress(), config.getPort());
		this.httpServer = com.sun.net.httpserver.HttpServer.create(bindAddress, 0);

		// Set up static file handler
		StaticFileHandler handler = new StaticFileHandler(staticPath);
		this.httpServer.createContext("/", handler);

		// Start accepting connections
		this.httpServer.start();
		this.running = true;

		// Get the actual bound address (useful if ephemeral port was used)
		String boundAddress = getActualAddress();

		// Notify listeners of successful start
		notifyAfterRestart(boundAddress);
	}

	/**
	 * Stops the HTTP server.
	 *
	 * Performs graceful shutdown, allowing in-flight requests to complete.
	 * Cleans up all resources.
	 *
	 * @throws IllegalStateException if server is not running
	 */
	public void stop() {
		if (!running) {
			throw new IllegalStateException("Server is not running");
		}

		if (this.httpServer != null) {
			this.httpServer.stop(0); // 0 = wait indefinitely for graceful shutdown
			this.httpServer = null;
		}

		this.running = false;
	}

	/**
	 * Returns the address where the server is currently listening.
	 *
	 * Useful after start() if an ephemeral port was requested.
	 *
	 * @return Address in format "127.0.0.1:PORT" or null if not running
	 */
	public String getActualAddress() {
		if (!running || httpServer == null) {
			return null;
		}

		InetSocketAddress addr = httpServer.getAddress();
		if (addr == null) {
			return null;
		}

		return addr.getHostName() + ":" + addr.getPort();
	}

	/**
	 * Returns the actual bound port (useful if ephemeral port was requested).
	 *
	 * @return Port number, or -1 if not running
	 */
	public int getActualPort() {
		if (!running || httpServer == null) {
			return -1;
		}

		InetSocketAddress addr = httpServer.getAddress();
		if (addr == null) {
			return -1;
		}

		return addr.getPort();
	}

	/**
	 * Checks if the server is currently running.
	 *
	 * @return true if running, false otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Adds a lifecycle listener.
	 *
	 * @param listener The listener to add (ignored if null)
	 */
	public void addListener(HttpServerListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a lifecycle listener.
	 *
	 * @param listener The listener to remove
	 */
	public void removeListener(HttpServerListener listener) {
		listeners.remove(listener);
	}

	// Notification methods (called internally)

	private void notifyBeforeRestart() {
		List<HttpServerListener> snapshot = new ArrayList<>(listeners);
		for (HttpServerListener listener : snapshot) {
			try {
				listener.onBeforeRestart();
			} catch (Exception e) {
				// Listener exceptions must not propagate
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
				// Listener exceptions must not propagate
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
				// Listener exceptions must not propagate
				System.err.println("HttpServerListener.onStartFailure() threw exception: " + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "VuePressHttpServer{" +
				"config=" + config +
				", running=" + running +
				", actualAddress=" + getActualAddress() +
				'}';
	}
}

