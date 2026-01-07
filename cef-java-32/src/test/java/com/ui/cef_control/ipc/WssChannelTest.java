package com.anca.appl.fw.gui.cef_control.ipc;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WssChannelTest {

	private MockWssServer mockServer;
	private String validToken;

	@Before
	public void setUp() {
		validToken = "test-session-token";
		mockServer = new MockWssServer();
	}

	@Test
	public void testConnectToWssEndpoint() throws Exception {
		mockServer.start();
		URI endpoint = new URI("wss://localhost:" + mockServer.getPort());

		WssChannel channel = new WssChannel(endpoint, validToken);
		CountDownLatch connected = new CountDownLatch(1);

		channel.addConnectionListener(new ConnectionListener() {
			public void onConnected() {
				connected.countDown();
			}
			public void onDisconnected() {}
			public void onError(Throwable error) {}
		});

		channel.connect();

		assertTrue("Should connect within 5 seconds",
				connected.await(5, TimeUnit.SECONDS));

		channel.close();
		mockServer.stop();
	}

	@Test
	public void testTlsHandshakeSucceedsUsingOsTrustStore() throws Exception {
		mockServer.startWithSelfSignedCert();
		URI endpoint = new URI("wss://localhost:" + mockServer.getPort());

		WssChannel channel = new WssChannel(endpoint, validToken);
		CountDownLatch connected = new CountDownLatch(1);
		CountDownLatch errorLatch = new CountDownLatch(1);

		channel.addConnectionListener(new ConnectionListener() {
			public void onConnected() {
				connected.countDown();
			}
			public void onDisconnected() {}
			public void onError(Throwable error) {
				errorLatch.countDown();
			}
		});

		channel.connect();

		// With self-signed cert not in trust store, should fail
		assertTrue("Should fail TLS handshake",
				errorLatch.await(5, TimeUnit.SECONDS));

		channel.close();
		mockServer.stop();
	}

	@Test
	public void testHelloSentImmediatelyOnConnect() throws Exception {
		mockServer.start();
		URI endpoint = new URI("wss://localhost:" + mockServer.getPort());

		WssChannel channel = new WssChannel(endpoint, validToken);
		CountDownLatch messageSent = new CountDownLatch(1);

		mockServer.onMessageReceived((msg) -> {
			if (msg.contains("HELLO") && msg.contains(validToken)) {
				messageSent.countDown();
			}
		});

		channel.connect();

		assertTrue("HELLO should be sent immediately after connect",
				messageSent.await(5, TimeUnit.SECONDS));

		channel.close();
		mockServer.stop();
	}

	@Test
	public void testReconnectOnTransientFailure() throws Exception {
		mockServer.start();
		URI endpoint = new URI("wss://localhost:" + mockServer.getPort());

		WssChannel channel = new WssChannel(endpoint, validToken);
		CountDownLatch firstConnect = new CountDownLatch(1);
		CountDownLatch reconnect = new CountDownLatch(2);

		channel.addConnectionListener(new ConnectionListener() {
			public void onConnected() {
				firstConnect.countDown();
				reconnect.countDown();
			}
			public void onDisconnected() {}
			public void onError(Throwable error) {}
		});

		channel.connect();
		assertTrue("Should connect initially",
				firstConnect.await(5, TimeUnit.SECONDS));

		// Simulate transient failure
		mockServer.closeAllConnections();

		assertTrue("Should reconnect after transient failure",
				reconnect.await(10, TimeUnit.SECONDS));

		channel.close();
		mockServer.stop();
	}

	@Test
	public void testCleanShutdown() throws Exception {
		mockServer.start();
		URI endpoint = new URI("wss://localhost:" + mockServer.getPort());

		WssChannel channel = new WssChannel(endpoint, validToken);
		CountDownLatch connected = new CountDownLatch(1);
		CountDownLatch disconnected = new CountDownLatch(1);

		channel.addConnectionListener(new ConnectionListener() {
			public void onConnected() {
				connected.countDown();
			}
			public void onDisconnected() {
				disconnected.countDown();
			}
			public void onError(Throwable error) {}
		});

		channel.connect();
		assertTrue("Should connect", connected.await(5, TimeUnit.SECONDS));

		channel.close();

		assertTrue("Should disconnect cleanly",
				disconnected.await(5, TimeUnit.SECONDS));

		mockServer.stop();
	}

	@Test
	public void testSendMessageAfterConnect() throws Exception {
		mockServer.start();
		URI endpoint = new URI("wss://localhost:" + mockServer.getPort());

		WssChannel channel = new WssChannel(endpoint, validToken);
		CountDownLatch connected = new CountDownLatch(1);
		CountDownLatch messageReceived = new CountDownLatch(1);

		String testMessage = "{\"type\":\"TEST\",\"data\":\"hello\"}";

		mockServer.onMessageReceived((msg) -> {
			if (msg.equals(testMessage)) {
				messageReceived.countDown();
			}
		});

		channel.addConnectionListener(new ConnectionListener() {
			public void onConnected() {
				connected.countDown();
			}
			public void onDisconnected() {}
			public void onError(Throwable error) {}
		});

		channel.connect();
		assertTrue("Should connect", connected.await(5, TimeUnit.SECONDS));

		channel.send(testMessage);

		assertTrue("Message should be received by server",
				messageReceived.await(5, TimeUnit.SECONDS));

		channel.close();
		mockServer.stop();
	}

	@Test
	public void testCannotSendBeforeConnect() throws Exception {
		URI endpoint = new URI("wss://localhost:9999");
		WssChannel channel = new WssChannel(endpoint, validToken);

		try {
			channel.send("test");
			fail("Should throw exception when sending before connect");
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage().contains("not connected") ||
					e.getMessage().contains("connection"));
		}
	}

	@Test
	public void testRejectInsecureWsEndpoint() throws Exception {
		URI endpoint = new URI("ws://localhost:9999");

		try {
			WssChannel channel = new WssChannel(endpoint, validToken);
			fail("Should reject insecure ws:// endpoint");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("wss") ||
					e.getMessage().contains("secure"));
		}
	}

	@Test
	public void testMultipleListenersReceiveEvents() throws Exception {
		mockServer.start();
		URI endpoint = new URI("wss://localhost:" + mockServer.getPort());

		WssChannel channel = new WssChannel(endpoint, validToken);
		CountDownLatch listener1Connected = new CountDownLatch(1);
		CountDownLatch listener2Connected = new CountDownLatch(1);

		channel.addConnectionListener(new ConnectionListener() {
			public void onConnected() {
				listener1Connected.countDown();
			}
			public void onDisconnected() {}
			public void onError(Throwable error) {}
		});

		channel.addConnectionListener(new ConnectionListener() {
			public void onConnected() {
				listener2Connected.countDown();
			}
			public void onDisconnected() {}
			public void onError(Throwable error) {}
		});

		channel.connect();

		assertTrue("Listener 1 should receive event",
				listener1Connected.await(5, TimeUnit.SECONDS));
		assertTrue("Listener 2 should receive event",
				listener2Connected.await(5, TimeUnit.SECONDS));

		channel.close();
		mockServer.stop();
	}

	// Mock WebSocket server for testing
	private static class MockWssServer {
		private int port = 8765;
		private boolean running = false;
		private List<MockConnection> connections = new ArrayList<>();
		private MessageHandler messageHandler;

		public void start() {
			running = true;
			port = 8765;
		}

		public void startWithSelfSignedCert() {
			running = true;
			port = 8766;
		}

		public void stop() {
			running = false;
			connections.clear();
		}

		public int getPort() {
			return port;
		}

		public void onMessageReceived(MessageHandler handler) {
			this.messageHandler = handler;
		}

		public void closeAllConnections() {
			for (MockConnection conn : connections) {
				conn.close();
			}
			connections.clear();
		}

		void simulateMessageReceived(String message) {
			if (messageHandler != null) {
				messageHandler.handle(message);
			}
		}

		interface MessageHandler {
			void handle(String message);
		}

		private static class MockConnection {
			void close() {}
		}
	}
}