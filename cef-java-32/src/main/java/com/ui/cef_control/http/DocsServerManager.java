package com.ui.cef_control.http;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class DocsServerManager {
    private final String docsPath;
    private DocsHttpServer server;
    private DocsState state;
    private final List<DocsRestartListener> listeners;

    public interface DocsRestartListener {
        void onBeforeRestart();
        void onAfterRestart(String baseUrl);
        void onStartFailure(Throwable error);
    }

    public DocsServerManager(String docsPath) {
        this.docsPath = docsPath;
        this.state = DocsState.STOPPED;
        this.listeners = new ArrayList<>();
    }

    public synchronized void start() throws IOException {
        if (state == DocsState.RUNNING) {
            throw new IllegalStateException("Server already running");
        }

        state = DocsState.STARTING;
        notifyBeforeRestart();

        try {
            server = new DocsHttpServer(docsPath, 0);
            server.start();
            state = DocsState.RUNNING;
            notifyAfterRestart(server.getBaseUrl());
        } catch (IOException e) {
            state = DocsState.STOPPED;
            notifyStartFailure(e);
            throw e;
        }
    }

    public synchronized void stop() {
        if (state != DocsState.RUNNING) {
            throw new IllegalStateException("Server not running");
        }
        if (server != null) {
            server.stop();
        }
        state = DocsState.STOPPED;
    }

    public synchronized void restart() throws IOException {
        stop();
        start();
    }

    public synchronized DocsState getState() {
        return state;
    }

    public synchronized String getBaseUrl() {
        if (server == null) {
            return null;
        }
        return server.getBaseUrl();
    }

    public synchronized int getBoundPort() {
        if (server == null) {
            return -1;
        }
        return server.getBoundPort();
    }

    public void addListener(DocsRestartListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DocsRestartListener listener) {
        listeners.remove(listener);
    }

    private void notifyBeforeRestart() {
        List<DocsRestartListener> snapshot = new ArrayList<>(listeners);
        for (DocsRestartListener listener : snapshot) {
            try {
                listener.onBeforeRestart();
            } catch (Exception e) {
                System.err.println("Listener onBeforeRestart error: " + e);
            }
        }
    }

    private void notifyAfterRestart(String baseUrl) {
        List<DocsRestartListener> snapshot = new ArrayList<>(listeners);
        for (DocsRestartListener listener : snapshot) {
            try {
                listener.onAfterRestart(baseUrl);
            } catch (Exception e) {
                System.err.println("Listener onAfterRestart error: " + e);
            }
        }
    }

    private void notifyStartFailure(Throwable error) {
        List<DocsRestartListener> snapshot = new ArrayList<>(listeners);
        for (DocsRestartListener listener : snapshot) {
            try {
                listener.onStartFailure(error);
            } catch (Exception e) {
                System.err.println("Listener onStartFailure error: " + e);
            }
        }
    }
}

