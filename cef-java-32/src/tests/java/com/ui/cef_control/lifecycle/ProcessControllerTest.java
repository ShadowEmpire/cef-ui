package com.anca.appl.fw.gui.cef_control.lifecycle;

import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.List;

public class ProcessControllerTest extends TestCase {

	private ProcessController controller;
	private TestLifecycleListener listener;

	protected void setUp() {
		controller = new ProcessController();
		listener = new TestLifecycleListener();
	}

	public void testStartEmitsOnStartedExactlyOnce() {
		controller.addListener(listener);

		controller.start();

		assertEquals(1, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
		assertEquals(0, listener.errorCount);
	}

	public void testStartRejectsDoubleStart() {
		controller.start();

		try {
			controller.start();
			fail("Expected IllegalStateException on double-start");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("already") ||
					ex.getMessage().contains("started"));
		}
	}

	public void testStopEmitsOnStoppingThenOnStopped() {
		controller.addListener(listener);
		controller.start();

		controller.stop();

		assertEquals(1, listener.startedCount);
		assertEquals(1, listener.stoppingCount);
		assertEquals(1, listener.stoppedCount);
		assertTrue("onStopping must be called before onStopped",
				listener.stoppingIndex < listener.stoppedIndex);
	}

	public void testStopWithoutStartDoesNothing() {
		controller.addListener(listener);

		controller.stop();

		assertEquals(0, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
	}

	public void testRestartAllowedAfterStopped() {
		controller.addListener(listener);
		controller.start();
		controller.stop();

		controller.start();

		assertEquals(2, listener.startedCount);
	}

	public void testRestartNotAllowedWhileRunning() {
		controller.start();

		try {
			controller.start();
			fail("Expected IllegalStateException when restarting while running");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("already") ||
					ex.getMessage().contains("started"));
		}
	}

	public void testOnErrorTransitionsToStoppedState() {
		controller.addListener(listener);
		controller.start();

		Throwable error = new RuntimeException("Test error");
		controller.onError(error);

		assertEquals(1, listener.errorCount);
		assertEquals(error, listener.lastError);
		assertEquals(1, listener.stoppedCount);
	}

	public void testRestartAllowedAfterError() {
		controller.addListener(listener);
		controller.start();
		controller.onError(new RuntimeException("Test error"));

		controller.start();

		assertEquals(2, listener.startedCount);
	}

	public void testMultipleListenersReceiveEventsInOrder() {
		TestLifecycleListener listener1 = new TestLifecycleListener();
		TestLifecycleListener listener2 = new TestLifecycleListener();
		TestLifecycleListener listener3 = new TestLifecycleListener();

		controller.addListener(listener1);
		controller.addListener(listener2);
		controller.addListener(listener3);

		controller.start();

		assertEquals(1, listener1.startedCount);
		assertEquals(1, listener2.startedCount);
		assertEquals(1, listener3.startedCount);
		assertTrue("Listeners called in order",
				listener1.startedIndex < listener2.startedIndex);
		assertTrue("Listeners called in order",
				listener2.startedIndex < listener3.startedIndex);
	}

	public void testListenerInvocationOrderForStop() {
		TestLifecycleListener listener1 = new TestLifecycleListener();
		TestLifecycleListener listener2 = new TestLifecycleListener();

		controller.addListener(listener1);
		controller.addListener(listener2);
		controller.start();

		controller.stop();

		assertTrue("listener1.onStopping before listener2.onStopping",
				listener1.stoppingIndex < listener2.stoppingIndex);
		assertTrue("listener1.onStopped before listener2.onStopped",
				listener1.stoppedIndex < listener2.stoppedIndex);
	}

	public void testOnErrorEmitsBeforeOnStopped() {
		controller.addListener(listener);
		controller.start();

		controller.onError(new RuntimeException("Test"));

		assertTrue("onError must be called before onStopped",
				listener.errorIndex < listener.stoppedIndex);
	}

	public void testStartOnlyEmitsOnStartedNotOtherEvents() {
		controller.addListener(listener);

		controller.start();

		assertEquals(1, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
		assertEquals(0, listener.errorCount);
	}

	public void testStopOnlyEmitsStoppingAndStoppedNotOtherEvents() {
		controller.addListener(listener);
		controller.start();
		listener.reset();

		controller.stop();

		assertEquals(0, listener.startedCount);
		assertEquals(1, listener.stoppingCount);
		assertEquals(1, listener.stoppedCount);
		assertEquals(0, listener.errorCount);
	}

	public void testOnErrorBeforeStartDoesNotEmitEvents() {
		controller.addListener(listener);

		controller.onError(new RuntimeException("Test"));

		assertEquals(0, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
		assertEquals(0, listener.errorCount);
	}

	public void testDoubleStopDoesNotEmitAdditionalEvents() {
		controller.addListener(listener);
		controller.start();
		controller.stop();
		listener.reset();

		controller.stop();

		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
	}

	public void testRemoveListenerPreventsNotifications() {
		controller.addListener(listener);
		controller.removeListener(listener);

		controller.start();

		assertEquals(0, listener.startedCount);
	}

	public void testStateTransitionSequence() {
		controller.addListener(listener);

		// Initial -> Started
		controller.start();
		assertEquals("STARTED", controller.getState().toString());

		// Started -> Stopping -> Stopped
		controller.stop();
		assertEquals("STOPPED", controller.getState().toString());

		// Stopped -> Started
		controller.start();
		assertEquals("STARTED", controller.getState().toString());

		// Started -> Error -> Stopped
		controller.onError(new RuntimeException("Test"));
		assertEquals("STOPPED", controller.getState().toString());
	}

	// Helper class for testing
	private static class TestLifecycleListener implements LifecycleListener {
		int startedCount = 0;
		int stoppingCount = 0;
		int stoppedCount = 0;
		int errorCount = 0;
		Throwable lastError = null;

		int startedIndex = -1;
		int stoppingIndex = -1;
		int stoppedIndex = -1;
		int errorIndex = -1;

		private static int globalEventCounter = 0;

		public void reset() {
			startedCount = 0;
			stoppingCount = 0;
			stoppedCount = 0;
			errorCount = 0;
			lastError = null;
			startedIndex = -1;
			stoppingIndex = -1;
			stoppedIndex = -1;
			errorIndex = -1;
		}

		public void onStarted() {
			startedCount++;
			startedIndex = globalEventCounter++;
		}

		public void onStopping() {
			stoppingCount++;
			stoppingIndex = globalEventCounter++;
		}

		public void onStopped() {
			stoppedCount++;
			stoppedIndex = globalEventCounter++;
		}

		public void onError(Throwable error) {
			errorCount++;
			lastError = error;
			errorIndex = globalEventCounter++;
		}
	}
}
