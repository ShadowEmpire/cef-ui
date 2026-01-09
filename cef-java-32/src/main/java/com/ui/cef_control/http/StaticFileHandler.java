package com.ui.cef_control.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * HTTP request handler for serving static VuePress files.
 *
 * Phase-6 Section 1: Simple static file serving.
 *
 * Routing logic:
 * - GET /path/to/file.ext -> Serve the file if it exists
 * - GET /path/that/is/not/a/file -> Serve index.html (SPA fallback)
 * - Other methods -> 405 Method Not Allowed
 *
 * Content-Type:
 * - Automatically determined from file extension
 *
 * No caching, compression, range requests, or security headers (Phase-7).
 */
class StaticFileHandler implements HttpHandler {

	private final Path staticRoot;

	StaticFileHandler(Path staticRoot) {
		this.staticRoot = staticRoot;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// Only support GET requests
		if (!"GET".equals(exchange.getRequestMethod())) {
			exchange.sendResponseHeaders(405, 0); // 405 Method Not Allowed
			exchange.close();
			return;
		}

		try {
			String path = exchange.getRequestURI().getPath();
			if (path.startsWith("/")) {
				path = path.substring(1);
			}

			// Prevent directory traversal attacks
			if (path.contains("..")) {
				exchange.sendResponseHeaders(400, 0); // 400 Bad Request
				exchange.close();
				return;
			}

			Path filePath = staticRoot.resolve(path);

			// Check if requested path is a file
			if (Files.isRegularFile(filePath)) {
				serveFile(exchange, filePath);
			} else {
				// Fallback to index.html for SPA routing
				Path indexFile = staticRoot.resolve("index.html");
				if (Files.isRegularFile(indexFile)) {
					serveFile(exchange, indexFile);
				} else {
					// index.html not found
					exchange.sendResponseHeaders(404, 0); // 404 Not Found
					exchange.close();
				}
			}
		} catch (Exception e) {
			// Any unexpected error -> 500
			try {
				exchange.sendResponseHeaders(500, 0);
				exchange.close();
			} catch (IOException ignore) {
				// Already closed or write failed
			}
		}
	}

	/**
	 * Serves a file with appropriate content-type header.
	 *
	 * Phase-6: Only send Content-Type and Content-Length.
	 * Phase-7: Add Cache-Control, ETag, If-Modified-Since, compression, etc.
	 */
	private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
		byte[] content = Files.readAllBytes(filePath);
		String contentType = guessContentType(filePath);

		exchange.getResponseHeaders().set("Content-Type", contentType);
		exchange.sendResponseHeaders(200, content.length); // 200 OK

		OutputStream os = exchange.getResponseBody();
		os.write(content);
		os.close();
	}

	/**
	 * Guesses MIME type based on file extension.
	 * Uses Files.probeContentType() as fallback.
	 */
	private String guessContentType(Path filePath) {
		String fileName = filePath.getFileName().toString();
		String extension = fileName.contains(".") ?
				fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() :
				"";

		// Common VuePress file types
		switch (extension) {
			case "html":
				return "text/html; charset=utf-8";
			case "css":
				return "text/css";
			case "js":
				return "application/javascript";
			case "json":
				return "application/json";
			case "svg":
				return "image/svg+xml";
			case "png":
				return "image/png";
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "gif":
				return "image/gif";
			case "webp":
				return "image/webp";
			case "woff":
				return "font/woff";
			case "woff2":
				return "font/woff2";
			case "ttf":
				return "font/ttf";
			case "eot":
				return "application/vnd.ms-fontobject";
			default:
				// Fallback to system probe
				try {
					String probed = Files.probeContentType(filePath);
					return probed != null ? probed : "application/octet-stream";
				} catch (IOException e) {
					return "application/octet-stream";
				}
		}
	}
}

