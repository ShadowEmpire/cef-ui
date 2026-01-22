package com.ui.cef_control.http;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class VuePressHttpServerTest {

	private VuePressHttpServer server;
	private HttpServerConfig config;
	private Path tempStaticDir;
	private TestHttpServerListener listener;

	@Before
	public void setUp() throws IOException {
		// Create temporary directory for static files
		tempStaticDir = Files.createTempDirectory("vuepress_test_");

		// Create a sample index.html
		Path indexPath = tempStaticDir.resolve("index.html");
		Files.write(indexPath, "<html><body>Test</body></html>".getBytes());

		// Create config: ephemeral port (0), localhost binding
		config = new HttpServerConfig(
				tempStaticDir.toAbsolutePath().toString(),
				0, // ephemeral port
				"127.0.0.1"
		);

		server = new VuePressHttpServer(config);
		listener = new TestHttpServerListener();
	}

	@Test
	public void testServerStartsSuccessfully() throws IOException {
		server.addListener(listener);

		server.start();

		assertTrue(server.isRunning());
		assertNotNull(server.getActualAddress());
		assertTrue(server.getActualPort() > 0);
		assertEquals(1, listener.afterRestartCount);
	}

	@Test
	public void testServerCannotStartTwice() throws IOException {
		server.start();

		try {
			server.start();
			fail("Expected IllegalStateException on double start");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("already running"));
		}
	}

	@Test
	public void testServerStopsSuccessfully() throws IOException {
		server.start();

		server.stop();

		assertFalse(server.isRunning());
		assertEquals(-1, server.getActualPort());
		assertNull(server.getActualAddress());
	}

	@Test
	public void testServerCannotStopIfNotRunning() {
		try {
			server.stop();
			fail("Expected IllegalStateException");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("not running"));
		}
	}

	@Test
	public void testListenerNotifiedOfStart() throws IOException {
		server.addListener(listener);

		server.start();

		assertEquals(1, listener.afterRestartCount);
		assertNotNull(listener.lastNewAddress);
		assertTrue(listener.lastNewAddress.startsWith("127.0.0.1"));
	}

	@Test
	public void testListenerNotifiedOfFailure() {
		// Create config with invalid path
		HttpServerConfig badConfig = new HttpServerConfig(
				"/nonexistent/path/that/does/not/exist",
				0,
				"127.0.0.1"
		);
		VuePressHttpServer badServer = new VuePressHttpServer(badConfig);
		badServer.addListener(listener);

		try {
			badServer.start();
			fail("Expected IOException");
		} catch (IOException e) {
			// Expected
		}

		assertEquals(1, listener.startFailureCount);
		assertNotNull(listener.lastError);
	}

	@Test
	public void testActualPortAfterEphemeralStart() throws IOException {
		server.start();

		int port = server.getActualPort();
		assertTrue("Port should be assigned by OS", port > 0);
		assertTrue("Port should be valid", port <= 65535);
	}

	@Test
	public void testActualAddressFormat() throws IOException {
		server.start();

		String address = server.getActualAddress();
		assertNotNull(address);
		assertTrue("Address should contain host:port", address.contains(":"));
		assertTrue("Address should start with 127.0.0.1", address.startsWith("127.0.0.1:"));
	}

	@Test
	public void testRemoveListenerPreventsNotification() throws IOException {
		server.addListener(listener);
		server.removeListener(listener);

		server.start();

		assertEquals(0, listener.afterRestartCount);
	}

	@Test
	public void testStaticFilesPathValidation() {
		try {
			new HttpServerConfig("", 0, "127.0.0.1");
			fail("Expected IllegalArgumentException for empty path");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().contains("staticFilesPath"));
		}
	}

	@Test
	public void testPortValidation() {
		try {
			new HttpServerConfig("/tmp", -1, "127.0.0.1");
			fail("Expected IllegalArgumentException for invalid port");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().contains("port"));
		}
	}

	@Test
	public void testBindAddressValidation() {
		try {
			new HttpServerConfig("/tmp", 0, "");
			fail("Expected IllegalArgumentException for empty address");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().contains("bindAddress"));
		}
	}

	// Helper listener for testing
	private static class TestHttpServerListener implements HttpServerListener {
		int beforeRestartCount = 0;
		int afterRestartCount = 0;
		int startFailureCount = 0;
		String lastNewAddress = null;
		Throwable lastError = null;

		@Override
		public void onBeforeRestart() {
			beforeRestartCount++;
		}

		@Override
		public void onAfterRestart(String newAddress) {
			afterRestartCount++;
			this.lastNewAddress = newAddress;
		}

		@Override
		public void onStartFailure(Throwable error) {
			startFailureCount++;
			this.lastError = error;
		}
	}
}

