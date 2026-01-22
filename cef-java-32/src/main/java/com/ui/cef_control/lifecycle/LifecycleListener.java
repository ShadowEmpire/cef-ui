package com.ui.cef_control.lifecycle;

public interface LifecycleListener {

	void onStarted();

	void onStopping();

	void onStopped();

	void onError(Throwable error);
}
