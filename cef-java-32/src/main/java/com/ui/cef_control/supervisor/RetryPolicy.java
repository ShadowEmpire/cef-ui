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

	/**
	 * Phase-6: Backoff delay in milliseconds before retry.
	 * Default is exponential backoff: 100ms * 2^(attempt-1).
	 *
	 * @param attempt current attempt number (starting from 1)
	 * @return delay in milliseconds
	 */
	default long getBackoffMs(int attempt) {
		// Exponential backoff: 100ms, 200ms, 400ms, 800ms, ...
		return 100L * (1L << (attempt - 1));
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
