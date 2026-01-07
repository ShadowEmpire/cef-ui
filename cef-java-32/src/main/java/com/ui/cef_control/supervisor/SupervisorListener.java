package com.anca.appl.fw.gui.cef_control.supervisor;

/**
 * Listener for UI supervisor events.
 *
 * Notifies clients of UI availability changes and crash events.
 */
public interface SupervisorListener {

	/**
	 * Called when the UI becomes available.
	 */
	void onUiAvailable();

	/**
	 * Called when the UI is unavailable (failed to start).
	 */
	void onUiUnavailable();

	/**
	 * Called when the UI crashes.
	 *
	 * @param error the error that caused the crash
	 */
	void onUiCrashed(Throwable error);
}
