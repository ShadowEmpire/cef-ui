package com.anca.appl.fw.gui.cef_control.lifecycle;

import java.util.ArrayList;
import java.util.List;

public class ProcessController {

	private State state;
	private final List<LifecycleListener> listeners;

	public enum State {
		INITIAL,
		STARTED,
		STOPPING,
		STOPPED
	}

	public ProcessController() {
		this.state = State.INITIAL;
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
		if (state == State.STARTED) {
			throw new IllegalStateException(
					"ProcessController is already started"
			);
		}

		state = State.STARTED;
		notifyStarted();
	}

	public void stop() {
		if (state != State.STARTED) {
			return;
		}

		state = State.STOPPING;
		notifyStopping();

		state = State.STOPPED;
		notifyStopped();
	}

	public void onError(Throwable error) {
		if (state != State.STARTED) {
			return;
		}

		notifyError(error);

		state = State.STOPPED;
		notifyStopped();
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
