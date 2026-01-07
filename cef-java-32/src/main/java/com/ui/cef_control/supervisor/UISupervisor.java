package com.ui.cef_control.supervisor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Supervisor for UI process lifecycle.
 *
 * Phase 5: Handles UI startup failures, crashes, and command queueing.
 * Non-blocking, deterministic, treats UI as black box.
 */
public class UISupervisor {

	private final UIProcess uiProcess;
	private final RetryPolicy retryPolicy;
	private final List<SupervisorListener> listeners;
	private final Queue<String> commandQueue;
	private boolean uiRunning;
	private String lastIntent;

	public UISupervisor(UIProcess uiProcess, RetryPolicy retryPolicy) {
		if (uiProcess == null) {
			throw new IllegalArgumentException("UIProcess cannot be null");
		}
		if (retryPolicy == null) {
			throw new IllegalArgumentException("RetryPolicy cannot be null");
		}

		this.uiProcess = uiProcess;
		this.retryPolicy = retryPolicy;
		this.listeners = new ArrayList<>();
		this.commandQueue = new LinkedList<>();
		this.uiRunning = false;
		this.lastIntent = null;
	}

	public void start() {
		int attempt = 1;

		while (true) {
			try {
				uiProcess.start();

				// Snapshot ONCE for this lifecycle event
				List<SupervisorListener> snapshot = new ArrayList<>(listeners);
				for (SupervisorListener l : snapshot) {
					l.onUiAvailable();
				}
				return; // success

			} catch (Exception e) {

				// Snapshot ONCE for this failure event
				List<SupervisorListener> snapshot = new ArrayList<>(listeners);
				for (SupervisorListener l : snapshot) {
					l.onUiCrashed(e);
				}

				if (!retryPolicy.shouldRetry(attempt, e)) {

					// Snapshot ONCE for terminal event
					snapshot = new ArrayList<>(listeners);
					for (SupervisorListener l : snapshot) {
						l.onUiUnavailable();
					}
					return; // retries exhausted
				}

				attempt++;
			}
		}
	}


	public void stop() {
		if (uiRunning) {
			uiProcess.stop();
			uiRunning = false;
		}
		commandQueue.clear();
	}

	public void sendCommand(String command) {
		if (command == null || command.isEmpty()) {
			return;
		}

		lastIntent = command;

		if (uiRunning) {
			uiProcess.sendCommand(command);
		} else {
			commandQueue.offer(command);
		}
	}

	public void notifyUiCrash(Throwable error) {
		uiRunning = false;
		notifyUiCrashed(error);
	}

	public boolean isUiRunning() {
		return uiRunning;
	}

	public int getQueuedCommandCount() {
		return commandQueue.size();
	}

	public void addListener(SupervisorListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(SupervisorListener listener) {
		listeners.remove(listener);
	}

	private void replayLastIntent() {
		if (lastIntent != null) {
			uiProcess.sendCommand(lastIntent);
		}
	}

	private void flushCommandQueue() {
		while (!commandQueue.isEmpty()) {
			String command = commandQueue.poll();
			if (command != null) {
				uiProcess.sendCommand(command);
			}
		}
	}

	private void notifyUiAvailable() {
		List<SupervisorListener> listenersCopy = new ArrayList<>(listeners);
		for (SupervisorListener listener : listenersCopy) {
			try {
				listener.onUiAvailable();
			} catch (Exception e) {
				// Ignore listener exceptions
			}
		}
	}

	private void notifyUiUnavailable() {
		List<SupervisorListener> listenersCopy = new ArrayList<>(listeners);
		for (SupervisorListener listener : listenersCopy) {
			try {
				listener.onUiUnavailable();
			} catch (Exception e) {
				// Ignore listener exceptions
			}
		}
	}

	private void notifyUiCrashed(Throwable error) {
		List<SupervisorListener> listenersCopy = new ArrayList<>(listeners);
		for (SupervisorListener listener : listenersCopy) {
			try {
				listener.onUiCrashed(error);
			} catch (Exception e) {
				// Ignore listener exceptions
			}
		}
	}
}
