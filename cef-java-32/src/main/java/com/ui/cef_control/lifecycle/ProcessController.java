package com.anca.appl.fw.gui.cef_control.lifecycle;

import java.util.ArrayList;
import java.util.List;

public class ProcessController {

	public enum State {
		NEW,        // never started
		STARTING,   // start requested
		RUNNING,    // fully started
		STOPPING,  // controlled shutdown
		STOPPED,   // cleanly stopped
		ERROR       // crashed
	}

	private State state = State.NEW;
	private final List<LifecycleListener> listeners;

	public ProcessController() {
		this.state = State.NEW;
		this.listeners = new ArrayList<>();
	}

	public void addListener(LifecycleListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(LifecycleListener listener) {
		listeners.remove(listener);
	}

	public void start() {
		if (state != State.NEW && state != State.STOPPED) {
			throw new IllegalStateException(
					"Cannot start in state " + state
			);
		}

		state = State.STARTING;
		notifyStarted();
		state = State.RUNNING;
	}

	public void stop() {
		if (state != State.RUNNING) {
			throw new IllegalStateException(
					"Cannot stop in state " + state
			);
		}

		state = State.STOPPING;
		notifyStopping();

		state = State.STOPPED;
		notifyStopped();
	}

	public void onError(Throwable error) {
		if (state == State.ERROR || state == State.STOPPED) {
			return; // idempotent
		}

		state = State.ERROR;
		notifyError(error);

		state = State.STOPPED;
		notifyStopped();
	}

	public void restart() {
		if (state != State.STOPPED && state != State.ERROR) {
			throw new IllegalStateException(
					"Cannot restart in state " + state
			);
		}

		state = State.STARTING;
		notifyStarted();
		state = State.RUNNING;
	}

	public State getState() {
		return state;
	}

	private void notifyStarted() {
		for (LifecycleListener listener : listeners) {
			listener.onStarted();
		}
	}

	private void notifyStopping() {
		for (LifecycleListener listener : listeners) {
			listener.onStopping();
		}
	}

	private void notifyStopped() {
		for (LifecycleListener listener : listeners) {
			listener.onStopped();
		}
	}

	private void notifyError(Throwable error) {
		for (LifecycleListener listener : listeners) {
			listener.onError(error);
		}
	}
}
