package com.anca.appl.fw.gui.cef_control.lifecycle;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessControllerTest {

	private ProcessController controller;
	private TestLifecycleListener listener;

	@Before
	public void setUp() {
		controller = new ProcessController();
		listener = new TestLifecycleListener();
	}

	@Test
	public void testStartEmitsOnStartedExactlyOnce() {
		controller.addListener(listener);

		controller.start();

		assertEquals(1, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
		assertEquals(0, listener.errorCount);
	}

	@Test
	public void testStartRejectsDoubleStart() {
		controller.start();

		try {
			controller.start();
			fail("Expected IllegalStateException on double-start");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("Cannot start") ||
					ex.getMessage().contains("state"));
		}
	}

	@Test
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

	@Test
	public void testStopWithoutStartThrowsException() {
		controller.addListener(listener);

		try {
			controller.stop();
			fail("Expected IllegalStateException when stopping without start");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("Cannot stop") ||
					ex.getMessage().contains("state"));
		}

		assertEquals(0, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
	}

	@Test
	public void testRestartAllowedAfterStopped() {
		controller.addListener(listener);
		controller.start();
		controller.stop();

		controller.restart();

		assertEquals(2, listener.startedCount);
	}

	@Test
	public void testRestartNotAllowedWhileRunning() {
		controller.start();

		try {
			controller.restart();
			fail("Expected IllegalStateException when restarting while running");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("Cannot restart") ||
					ex.getMessage().contains("state"));
		}
	}

	@Test
	public void testOnErrorTransitionsToStoppedState() {
		controller.addListener(listener);
		controller.start();

		Throwable error = new RuntimeException("Test error");
		controller.onError(error);

		assertEquals(1, listener.errorCount);
		assertEquals(error, listener.lastError);
		assertEquals(1, listener.stoppedCount);
		assertEquals(ProcessController.State.STOPPED, controller.getState());
	}

	@Test
	public void testRestartAllowedAfterError() {
		controller.addListener(listener);
		controller.start();
		controller.onError(new RuntimeException("Test error"));

		controller.restart();

		assertEquals(2, listener.startedCount);
	}

	@Test
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

	@Test
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

	@Test
	public void testOnErrorEmitsBeforeOnStopped() {
		controller.addListener(listener);
		controller.start();

		controller.onError(new RuntimeException("Test"));

		assertTrue("onError must be called before onStopped",
				listener.errorIndex < listener.stoppedIndex);
	}

	@Test
	public void testStartOnlyEmitsOnStartedNotOtherEvents() {
		controller.addListener(listener);

		controller.start();

		assertEquals(1, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
		assertEquals(0, listener.errorCount);
	}

	@Test
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

	@Test
	public void testOnErrorBeforeStartDoesNotEmitEvents() {
		controller.addListener(listener);

		controller.onError(new RuntimeException("Test"));

		assertEquals(0, listener.startedCount);
		assertEquals(0, listener.stoppingCount);
		assertEquals(1, listener.stoppedCount);
		assertEquals(1, listener.errorCount);
	}

	@Test
	public void testDoubleStopThrowsException() {
		controller.addListener(listener);
		controller.start();
		controller.stop();
		listener.reset();

		try {
			controller.stop();
			fail("Expected IllegalStateException on double stop");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("Cannot stop") ||
					ex.getMessage().contains("state"));
		}

		assertEquals(0, listener.stoppingCount);
		assertEquals(0, listener.stoppedCount);
	}

	@Test
	public void testRemoveListenerPreventsNotifications() {
		controller.addListener(listener);
		controller.removeListener(listener);

		controller.start();

		assertEquals(0, listener.startedCount);
	}

	@Test
	public void testStateTransitionSequence() {
		controller.addListener(listener);

		// NEW -> RUNNING
		assertEquals(ProcessController.State.NEW, controller.getState());
		controller.start();
		assertEquals(ProcessController.State.RUNNING, controller.getState());

		// RUNNING -> STOPPED
		controller.stop();
		assertEquals(ProcessController.State.STOPPED, controller.getState());

		// STOPPED -> RUNNING (via restart)
		controller.restart();
		assertEquals(ProcessController.State.RUNNING, controller.getState());

		// RUNNING -> STOPPED (via error)
		controller.onError(new RuntimeException("Test"));
		assertEquals(ProcessController.State.STOPPED, controller.getState());
	}

	@Test
	public void testOnErrorIsIdempotent() {
		controller.addListener(listener);
		controller.start();

		Throwable error1 = new RuntimeException("Test error 1");
		Throwable error2 = new RuntimeException("Test error 2");

		controller.onError(error1);
		listener.reset();

		controller.onError(error2);

		// Second onError should not emit any events (idempotent)
		assertEquals(0, listener.errorCount);
		assertEquals(0, listener.stoppedCount);
	}

	@Test
	public void testStartFromNewState() {
		assertEquals(ProcessController.State.NEW, controller.getState());

		controller.start();

		assertEquals(ProcessController.State.RUNNING, controller.getState());
	}

	@Test
	public void testRestartOnlyAllowedFromStoppedOrError() {
		controller.start();

		try {
			controller.restart();
			fail("Should not allow restart from RUNNING state");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("Cannot restart"));
		}
	}

	@Test
	public void testRestartAfterErrorState() {
		controller.addListener(listener);
		controller.start();
		controller.onError(new RuntimeException("Test"));

		assertEquals(ProcessController.State.STOPPED, controller.getState());
		listener.reset();

		controller.restart();

		assertEquals(1, listener.startedCount);
		assertEquals(ProcessController.State.RUNNING, controller.getState());
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