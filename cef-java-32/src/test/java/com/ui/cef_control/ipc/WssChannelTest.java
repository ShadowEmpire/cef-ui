package com.ui.cef_control.ipc;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;

public class WssChannelTest {

	private URI validEndpoint;
	private String validToken;

	@Before
	public void setUp() throws Exception {
		validEndpoint = new URI("wss://localhost:8765");
		validToken = "test-session-token";
	}

	@Test
	public void testConnectCalledTwice() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener = new TestConnectionListener();
		channel.addConnectionListener(listener);

		channel.connect();

		try {
			channel.connect();
			// If no exception, verify no duplicate callbacks
			assertTrue("Second connect must not trigger duplicate onConnected",
					listener.connectedCount <= 1);
		} catch (IllegalStateException e) {
			// Alternative: second connect throws exception
			assertTrue("Exception message should mention state",
					e.getMessage().contains("connect") ||
							e.getMessage().contains("state") ||
							e.getMessage().contains("already"));
		}

		channel.close();
	}

	@Test
	public void testSendBeforeConnect() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);

		try {
			channel.send("test message");
			// PASS: fire-and-forget, ignored when not connected
		} catch (Exception e) {
			fail("send() before connect() must not throw in Phase 4");
		}
	}

	@Test
	public void testCloseBeforeConnect() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener = new TestConnectionListener();
		channel.addConnectionListener(listener);

		// close() before connect() must be safe
		channel.close();

		assertEquals("No onDisconnected callback before connection",
				0, listener.disconnectedCount);
		assertEquals("No error callback",
				0, listener.errorCount);
	}

	@Test
	public void testListenerRemoval() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener = new TestConnectionListener();

		channel.addConnectionListener(listener);
		channel.removeConnectionListener(listener);

		channel.connect();
		channel.close();

		assertEquals("Removed listener receives no onConnected",
				0, listener.connectedCount);
		assertEquals("Removed listener receives no onDisconnected",
				0, listener.disconnectedCount);
		assertEquals("Removed listener receives no onError",
				0, listener.errorCount);
	}

	@Test
	public void testConnectThenCloseMultipleTimes() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener = new TestConnectionListener();
		channel.addConnectionListener(listener);

		channel.connect();
		channel.close();

		int firstCloseDisconnectedCount = listener.disconnectedCount;

		channel.close();

		assertEquals("Second close must not trigger duplicate onDisconnected",
				firstCloseDisconnectedCount, listener.disconnectedCount);
	}

	@Test
	public void testRejectInsecureWsEndpoint() {
		try {
			URI insecureEndpoint = new URI("ws://localhost:8765");
			WssChannel channel = new WssChannel(insecureEndpoint, validToken);
			fail("Should reject insecure ws:// endpoint");
		} catch (IllegalArgumentException e) {
			assertTrue("Exception should mention security requirement",
					e.getMessage().contains("wss") ||
							e.getMessage().contains("secure"));
		} catch (Exception e) {
			fail("Wrong exception type: " + e.getClass().getName());
		}
	}

	@Test
	public void testRejectNullEndpoint() {
		try {
			WssChannel channel = new WssChannel(null, validToken);
			fail("Should reject null endpoint");
		} catch (IllegalArgumentException e) {
			assertTrue("Exception should mention endpoint",
					e.getMessage().contains("endpoint") ||
							e.getMessage().contains("null"));
		}
	}

	@Test
	public void testRejectNullSessionToken() {
		try {
			WssChannel channel = new WssChannel(validEndpoint, null);
			fail("Should reject null session token");
		} catch (IllegalArgumentException e) {
			assertTrue("Exception should mention token",
					e.getMessage().contains("token") ||
							e.getMessage().contains("null"));
		}
	}

	@Test
	public void testRejectEmptySessionToken() {
		try {
			WssChannel channel = new WssChannel(validEndpoint, "");
			fail("Should reject empty session token");
		} catch (IllegalArgumentException e) {
			assertTrue("Exception should mention token",
					e.getMessage().contains("token") ||
							e.getMessage().contains("empty"));
		}
	}

	@Test
	public void testMultipleListenersReceiveEvents() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener1 = new TestConnectionListener();
		TestConnectionListener listener2 = new TestConnectionListener();

		channel.addConnectionListener(listener1);
		channel.addConnectionListener(listener2);

		channel.connect();

		// Both listeners should be notified (if connection succeeds or fails)
		assertTrue("Listener 1 should receive events",
				listener1.connectedCount > 0 || listener1.errorCount > 0);
		assertTrue("Listener 2 should receive events",
				listener2.connectedCount > 0 || listener2.errorCount > 0);

		channel.close();
	}

	@Test
	public void testAddNullListenerIsIgnored() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);

		// Should not throw exception
		channel.addConnectionListener(null);

		channel.close();
	}

	@Test
	public void testRemoveNonExistentListenerIsIgnored() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener = new TestConnectionListener();

		// Should not throw exception
		channel.removeConnectionListener(listener);

		channel.close();
	}

	@Test
	public void testSendImplementsIMessageChannel() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);

		// Verify WssChannel implements IMessageChannel
		assertTrue("WssChannel must implement IMessageChannel",
				channel instanceof IMessageChannel);

		// Verify send() method exists (compile-time check)
		IMessageChannel messageChannel = channel;

		try {
			messageChannel.send("test");
			// PASS: fire-and-forget, ignored when not connected
		} catch (Exception e) {
			fail("send() must not throw when not connected in Phase 4");
		}

		channel.close();
	}

	@Test
	public void testCloseIsIdempotent() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener = new TestConnectionListener();
		channel.addConnectionListener(listener);

		channel.close();
		channel.close();
		channel.close();

		// Should not throw exceptions
		// Listener should receive at most one disconnect
		assertTrue("Disconnect count should be 0 or 1",
				listener.disconnectedCount <= 1);
	}

	@Test
	public void testAddSameListenerTwiceOnlyNotifiesOnce() {
		WssChannel channel = new WssChannel(validEndpoint, validToken);
		TestConnectionListener listener = new TestConnectionListener();

		channel.addConnectionListener(listener);
		channel.addConnectionListener(listener);

		channel.connect();

		// Listener should only be notified once
		assertTrue("Same listener added twice should only be notified once",
				listener.connectedCount <= 1);

		channel.close();
	}

	// Test helper class
	private static class TestConnectionListener implements ConnectionListener {
		int connectedCount = 0;
		int disconnectedCount = 0;
		int errorCount = 0;
		Throwable lastError = null;

		public void onConnected() {
			connectedCount++;
		}

		public void onDisconnected() {
			disconnectedCount++;
		}

		public void onError(Throwable error) {
			errorCount++;
			lastError = error;
		}
	}
}
