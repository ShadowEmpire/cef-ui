package com.ui.cef_control.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class StaticFileHandlerTest {

	private StaticFileHandler handler;
	private Path tempDir;

	@Before
	public void setUp() throws IOException {
		// Create temporary directory with test files
		tempDir = Files.createTempDirectory("static_handler_test_");

		// Create index.html
		Path indexPath = tempDir.resolve("index.html");
		Files.write(indexPath, "<html><body>Index</body></html>".getBytes());

		// Create a CSS file
		Path cssPath = tempDir.resolve("style.css");
		Files.write(cssPath, "body { color: red; }".getBytes());

		// Create a JS file
		Path jsPath = tempDir.resolve("script.js");
		Files.write(jsPath, "console.log('test');".getBytes());

		// Create a subdirectory with a file
		Path subDir = tempDir.resolve("subdir");
		Files.createDirectory(subDir);
		Path subFilePath = subDir.resolve("page.html");
		Files.write(subFilePath, "<html><body>Subpage</body></html>".getBytes());

		handler = new StaticFileHandler(tempDir);
	}

	@Test
	public void testServeIndexHtml() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("GET", "/");

		handler.handle(exchange);

		assertEquals(200, exchange.getResponseCodeInternal());
		assertTrue(exchange.getResponseBodyString().contains("Index"));
	}

	@Test
	public void testServeStyleSheet() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("GET", "/style.css");

		handler.handle(exchange);

		assertEquals(200, exchange.getResponseCodeInternal());
		assertTrue(exchange.getResponseBodyString().contains("color: red"));
	}

	@Test
	public void testServeJavaScript() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("GET", "/script.js");

		handler.handle(exchange);

		assertEquals(200, exchange.getResponseCodeInternal());
		assertTrue(exchange.getResponseBodyString().contains("console.log"));
	}

	@Test
	public void testFallbackToIndexHtmlForNonExistentPath() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("GET", "/nonexistent/path");

		handler.handle(exchange);

		assertEquals(200, exchange.getResponseCodeInternal());
		assertTrue(exchange.getResponseBodyString().contains("Index"));
	}

	@Test
	public void testServeSubdirectoryFile() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("GET", "/subdir/page.html");

		handler.handle(exchange);

		assertEquals(200, exchange.getResponseCodeInternal());
		assertTrue(exchange.getResponseBodyString().contains("Subpage"));
	}

	@Test
	public void testRejectDirectoryTraversal() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("GET", "/../../../etc/passwd");

		handler.handle(exchange);

		assertEquals(400, exchange.getResponseCodeInternal());
	}

	@Test
	public void testRejectPostRequest() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("POST", "/");

		handler.handle(exchange);

		assertEquals(405, exchange.getResponseCodeInternal());
	}

	@Test
	public void testRejectPutRequest() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("PUT", "/test.txt");

		handler.handle(exchange);

		assertEquals(405, exchange.getResponseCodeInternal());
	}

	@Test
	public void testRejectDeleteRequest() throws IOException {
		TestHttpExchange exchange = new TestHttpExchange("DELETE", "/test.txt");

		handler.handle(exchange);

		assertEquals(405, exchange.getResponseCodeInternal());
	}

	@Test
	public void testResponse404WhenIndexHtmlMissing() throws IOException {
		// Create handler pointing to empty directory
		Path emptyDir = Files.createTempDirectory("empty_");
		StaticFileHandler emptyHandler = new StaticFileHandler(emptyDir);

		TestHttpExchange exchange = new TestHttpExchange("GET", "/");

		emptyHandler.handle(exchange);

		assertEquals(404, exchange.getResponseCodeInternal());
	}

	// Test implementation of HttpExchange
	private static class TestHttpExchange extends HttpExchange {
		private final String method;
		private final URI uri;
		private final Headers responseHeaders;
		private final ByteArrayOutputStream responseBody;
		private int responseCode;

		TestHttpExchange(String method, String path) {
			this.method = method;
			this.uri = URI.create(path);
			this.responseHeaders = new Headers();
			this.responseBody = new ByteArrayOutputStream();
			this.responseCode = -1;
		}

		@Override
		public String getRequestMethod() {
			return method;
		}

		@Override
		public URI getRequestURI() {
			return uri;
		}

		@Override
		public Headers getResponseHeaders() {
			return responseHeaders;
		}

		@Override
		public OutputStream getResponseBody() {
			return responseBody;
		}

		@Override
		public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
			this.responseCode = rCode;
		}

		@Override
		public void close() {
			// No-op for test
		}

		int getResponseCodeInternal() {
			return responseCode;
		}

		String getResponseBodyString() {
			return responseBody.toString();
		}

		// Unused abstract methods
		@Override
		public java.security.Principal getPrincipal() {
			return null;
		}

		@Override
		public java.net.InetSocketAddress getRemoteAddress() {
			return null;
		}

		@Override
		public int getResponseCode() {
			return responseCode;
		}

		@Override
		public java.net.InetSocketAddress getLocalAddress() {
			return null;
		}

		@Override
		public String getProtocol() {
			return "HTTP/1.1";
		}

		@Override
		public Object getAttribute(String name) {
			return null;
		}

		@Override
		public void setAttribute(String name, Object value) {
		}

		@Override
		public void setStreams(java.io.InputStream i, java.io.OutputStream o) {
		}

		@Override
		public Headers getRequestHeaders() {
			return responseHeaders;
		}
	}
}


