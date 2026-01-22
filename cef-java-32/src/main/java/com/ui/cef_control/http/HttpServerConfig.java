package com.ui.cef_control.http;

/**
 * Configuration for the VuePress static HTTP server.
 *
 * Phase-6 Section 1: Serves prebuilt VuePress documentation.
 * Binds only to 127.0.0.1 (localhost) on an ephemeral (OS-assigned) port.
 *
 * Design principle: Immutable config, minimal concerns.
 */
public final class HttpServerConfig {

	private final String staticFilesPath;
	private final int port;
	private final String bindAddress;

	/**
	 * Creates configuration for HTTP server.
	 *
	 * @param staticFilesPath Path to prebuilt VuePress static files directory
	 * @param port Port number (0 = ephemeral/OS-assigned)
	 * @param bindAddress Network address (typically 127.0.0.1)
	 */
	public HttpServerConfig(String staticFilesPath, int port, String bindAddress) {
		if (staticFilesPath == null || staticFilesPath.trim().isEmpty()) {
			throw new IllegalArgumentException("staticFilesPath cannot be null or empty");
		}
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("port must be 0-65535, got: " + port);
		}
		if (bindAddress == null || bindAddress.trim().isEmpty()) {
			throw new IllegalArgumentException("bindAddress cannot be null or empty");
		}

		this.staticFilesPath = staticFilesPath;
		this.port = port;
		this.bindAddress = bindAddress;
	}

	public String getStaticFilesPath() {
		return staticFilesPath;
	}

	public int getPort() {
		return port;
	}

	public String getBindAddress() {
		return bindAddress;
	}

	@Override
	public String toString() {
		return "HttpServerConfig{" +
				"staticFilesPath='" + staticFilesPath + '\'' +
				", port=" + port +
				", bindAddress='" + bindAddress + '\'' +
				'}';
	}
}

