package com.anca.appl.fw.gui.cef_control.lifecycle;

public interface LifecycleListener {

	void onStarted();

	void onStopping();

	void onStopped();

	void onError(Throwable error);
}
