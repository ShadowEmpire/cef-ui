package com.ui.cef_control.supervisor;

public interface RetryPolicy {

	/**
	 * @param attempt current attempt number (starting from 1)
	 * @param failure last failure
	 * @return true if retry should be attempted
	 */
	boolean shouldRetry(int attempt, Throwable failure);

	default int getRetryDelayMs(int attemptNumber) {
		return 0; // Phase 5: no delay
	}
	// ---------- Static helpers for tests ----------

	static RetryPolicy noRetry() {
		return (attempt, failure) -> false;
	}

	static RetryPolicy alwaysRetry() {
		return (attempt, failure) -> true;
	}

	static RetryPolicy maxRetries(int maxRetries) {
		return (attempt, failure) -> attempt <= maxRetries;
	}
}
