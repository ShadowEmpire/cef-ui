package com.anca.appl.fw.gui.cef_control.ipc;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WssChannel implements IMessageChannel {

	private final URI endpoint;
	private final String sessionToken;
	private final List<ConnectionListener> listeners;
	private final AtomicReference<WebSocket> webSocket;
	private final AtomicBoolean connected;
	private final AtomicBoolean closed;
	private final ScheduledExecutorService reconnectExecutor;
	private final HttpClient httpClient;

	private static final int RECONNECT_DELAY_MS = 2000;
	private static final int MAX_RECONNECT_ATTEMPTS = 5;

	public WssChannel(URI endpoint, String sessionToken) {
		if (endpoint == null) {
			throw new IllegalArgumentException("Endpoint cannot be null");
		}

		if (!"wss".equals(endpoint.getScheme())) {
			throw new IllegalArgumentException(
					"Only secure WebSocket (wss://) endpoints are allowed"
			);
		}

		if (sessionToken == null || sessionToken.isEmpty()) {
			throw new IllegalArgumentException("Session token cannot be null or empty");
		}

		this.endpoint = endpoint;
		this.sessionToken = sessionToken;
		this.listeners = new ArrayList<>();
		this.webSocket = new AtomicReference<>();
		this.connected = new AtomicBoolean(false);
		this.closed = new AtomicBoolean(false);
		this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
		this.httpClient = createHttpClient();
	}

	private HttpClient createHttpClient() {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(
					TrustManagerFactory.getDefaultAlgorithm()
			);
			tmf.init((KeyStore) null);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);

			return HttpClient.newBuilder()
					.sslContext(sslContext)
					.build();
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize SSL context", e);
		}
	}

	public void connect() {
		if (closed.get()) {
			throw new IllegalStateException("Channel is closed");
		}

		doConnect(0);
	}

	private void doConnect(int attemptCount) {
		CompletableFuture<WebSocket> wsFuture = httpClient.newWebSocketBuilder()
				.buildAsync(endpoint, new WebSocketListener());

		wsFuture.whenComplete((ws, error) -> {
			if (error != null) {
				notifyError(error);

				if (attemptCount < MAX_RECONNECT_ATTEMPTS && !closed.get()) {
					reconnectExecutor.schedule(
							() -> doConnect(attemptCount + 1),
							RECONNECT_DELAY_MS,
							TimeUnit.MILLISECONDS
					);
				}
			} else {
				webSocket.set(ws);
				connected.set(true);
				notifyConnected();
				sendHello();
			}
		});
	}

	private void sendHello() {
		String helloMessage = String.format(
				"{\"type\":\"%s\",\"sessionToken\":\"%s\"}",
				MessageTypes.HELLO,
				sessionToken
		);

		try {
			send(helloMessage);
		} catch (Exception e) {
			notifyError(e);
		}
	}

	public void send(String message) {
		if (!connected.get()) {
			throw new IllegalStateException("Cannot send message: not connected");
		}

		WebSocket ws = webSocket.get();
		if (ws == null) {
			throw new IllegalStateException("WebSocket is not available");
		}

		ws.sendText(message, true);
	}

	public void close() {
		if (closed.compareAndSet(false, true)) {
			connected.set(false);

			WebSocket ws = webSocket.get();
			if (ws != null) {
				ws.sendClose(WebSocket.NORMAL_CLOSURE, "").join();
			}

			reconnectExecutor.shutdown();
			notifyDisconnected();
		}
	}

	public void addConnectionListener(ConnectionListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeConnectionListener(ConnectionListener listener) {
		listeners.remove(listener);
	}

	public boolean isConnected() {
		return connected.get();
	}

	private void notifyConnected() {
		for (ConnectionListener listener : listeners) {
			try {
				listener.onConnected();
			} catch (Exception e) {
				// Ignore listener exceptions
			}
		}
	}

	private void notifyDisconnected() {
		for (ConnectionListener listener : listeners) {
			try {
				listener.onDisconnected();
			} catch (Exception e) {
				// Ignore listener exceptions
			}
		}
	}

	private void notifyError(Throwable error) {
		for (ConnectionListener listener : listeners) {
			try {
				listener.onError(error);
			} catch (Exception e) {
				// Ignore listener exceptions
			}
		}
	}

	private void handleConnectionLost() {
		if (!closed.get()) {
			connected.set(false);
			notifyDisconnected();

			reconnectExecutor.schedule(
					() -> doConnect(0),
					RECONNECT_DELAY_MS,
					TimeUnit.MILLISECONDS
			);
		}
	}

	private class WebSocketListener implements WebSocket.Listener {

		@Override
		public void onOpen(WebSocket webSocket) {
			webSocket.request(1);
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			webSocket.request(1);
			return null;
		}

		@Override
		public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
			webSocket.request(1);
			return null;
		}

		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			handleConnectionLost();
			return null;
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			notifyError(error);
			handleConnectionLost();
		}
	}
}