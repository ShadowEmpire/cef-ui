package com.ui.cef_control.http;

import com.ui.cef_control.supervisor.RetryPolicy;
//import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class HttpServerSupervisorTest {

	private HttpServerSupervisor supervisor;
	private VuePressHttpServer server;
	private HttpServerConfig config;
	private Path tempStaticDir;
	private TestHttpServerListener listener;

	@Before
	public void setUp() throws IOException {
		// Create temporary directory for static files
		tempStaticDir = Files.createTempDirectory("http_supervisor_test_");
		Path indexPath = tempStaticDir.resolve("index.html");
		Files.write(indexPath, "<html><body>Test</body></html>".getBytes());

		// Create config with ephemeral port
		config = new HttpServerConfig(
				tempStaticDir.toAbsolutePath().toString(),
				0,
				"127.0.0.1"
		);

		server = new VuePressHttpServer(config);
		supervisor = new HttpServerSupervisor(server, RetryPolicy.noRetry());
		listener = new TestHttpServerListener();
	}

	@Test
	public void testSupervisorStartsServer() {
		supervisor.addListener(listener);

		supervisor.start();

		assertTrue(supervisor.isServerRunning());
		assertNotNull(supervisor.getServerAddress());
		assertTrue(supervisor.getServerPort() > 0);
		assertEquals(1, listener.afterRestartCount);
	}

	@Test
	public void testSupervisorNotifiesBeforeRestart() {
		supervisor.addListener(listener);

		supervisor.start();

		assertEquals(1, listener.beforeRestartCount);
	}

	@Test
	public void testSupervisorStopsServer() {
		supervisor.start();

		supervisor.stop();

		assertFalse(supervisor.isServerRunning());
	}

	@Test
	public void testSupervisorThrowsOnDoubleStart() {
		supervisor.start();

		try {
			supervisor.start();
			fail("Expected RuntimeException on double start");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().contains("already running"));
		}
	}

	@Test
	public void testSupervisorThrowsOnStopWhenNotRunning() {
		try {
			supervisor.stop();
			fail("Expected IllegalStateException");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("not running"));
		}
	}

	@Test
	public void testGetServerAddressReturnsActualAddress() {
		supervisor.start();

		String address = supervisor.getServerAddress();
		assertNotNull(address);
		assertTrue(address.contains("127.0.0.1"));
	}

	@Test
	public void testGetServerPortReturnsActualPort() {
		supervisor.start();

		int port = supervisor.getServerPort();
		assertTrue(port > 0);
	}

	@Test
	public void testRetryPolicyIsInvokedOnStartFailure() {
		// Create a mock retry policy that allows one retry
		TestRetryPolicy retryPolicy = new TestRetryPolicy(true, false);

		// Create server with bad config
		HttpServerConfig badConfig = new HttpServerConfig(
				"/nonexistent/path",
				0,
				"127.0.0.1"
		);
		VuePressHttpServer badServer = new VuePressHttpServer(badConfig);
		HttpServerSupervisor badSupervisor = new HttpServerSupervisor(badServer, retryPolicy);
		badSupervisor.addListener(listener);

		try {
			badSupervisor.start();
			fail("Expected RuntimeException");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().contains("Failed to start"));
		}

		// Verify retry was consulted
		assertTrue(retryPolicy.shouldRetryWasCalled);
		assertEquals(1, listener.startFailureCount);
	}

	@Test
	public void testListenerRemovedPreventsNotifications() {
		supervisor.addListener(listener);
		supervisor.removeListener(listener);

		supervisor.start();

		assertEquals(0, listener.beforeRestartCount);
		assertEquals(0, listener.afterRestartCount);
	}

	// Helper listener
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

	// Helper retry policy for testing
	private static class TestRetryPolicy implements RetryPolicy {
		private final boolean firstResult;
		private final boolean secondResult;
		boolean shouldRetryWasCalled = false;

		TestRetryPolicy(boolean firstResult, boolean secondResult) {
			this.firstResult = firstResult;
			this.secondResult = secondResult;
		}

		@Override
		public boolean shouldRetry(int attempt, Throwable failure) {
			shouldRetryWasCalled = true;
			if (attempt == 1) {
				return firstResult;
			}
			return secondResult;
		}
	}
}

