package com.anca.appl.fw.gui.cef_control.ipc;

import junit.framework.TestCase;

public class HandshakeTest extends TestCase {

	private static final String VALID_TOKEN = "test-token-123";

	public void testValidHelloWithCorrectToken() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertTrue(result.isValid());
		assertNull(result.getError());
	}

	public void testRejectInvalidToken() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"wrong-token\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("token") ||
				result.getError().contains("Token"));
	}

	public void testRejectMissingSessionToken() {
		String json = "{\"type\":\"HELLO\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("sessionToken") ||
				result.getError().contains("token"));
	}

	public void testRejectUnknownMessageType() {
		String json = "{\"type\":\"UNKNOWN\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("type") ||
				result.getError().contains("UNKNOWN"));
	}

	public void testRejectMissingMessageType() {
		String json = "{\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("type"));
	}

	public void testIgnoreExtraJsonFields() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"extraField\":\"ignored\",\"anotherExtra\":42}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertTrue(result.isValid());
		assertNull(result.getError());
	}

	public void testHandleMalformedJsonWithoutThrowing() {
		String json = "{invalid json";

		try {
			Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);
			assertFalse(result.isValid());
			assertNotNull(result.getError());
		} catch (Exception e) {
			fail("Should not throw exception on malformed JSON");
		}
	}

	public void testHandleEmptyStringWithoutThrowing() {
		String json = "";

		try {
			Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);
			assertFalse(result.isValid());
			assertNotNull(result.getError());
		} catch (Exception e) {
			fail("Should not throw exception on empty string");
		}
	}

	public void testHandleNullJsonWithoutThrowing() {
		try {
			Handshake.Result result = Handshake.validateMessage(null, VALID_TOKEN);
			assertFalse(result.isValid());
			assertNotNull(result.getError());
		} catch (Exception e) {
			fail("Should not throw exception on null JSON");
		}
	}

	public void testRejectEmptySessionToken() {
		String json = "{\"type\":\"HELLO\",\"sessionToken:\"\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	public void testRejectNullSessionTokenValue() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":null}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	public void testRejectEmptyMessageType() {
		String json = "{\"type\":\"\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	public void testRejectNullMessageType() {
		String json = "{\"type\":null,\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	public void testValidationIsDeterministic() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result1 = Handshake.validateMessage(json, VALID_TOKEN);
		Handshake.Result result2 = Handshake.validateMessage(json, VALID_TOKEN);

		assertEquals(result1.isValid(), result2.isValid());
		if (result1.getError() != null) {
			assertEquals(result1.getError(), result2.getError());
		}
	}

	public void testCaseInsensitiveMessageType() {
		String json = "{\"type\":\"hello\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertTrue(result.getError().contains("type") ||
				result.getError().contains("hello"));
	}

	public void testWhitespaceInJsonIsHandled() {
		String json = "  {  \"type\" : \"HELLO\" , \"sessionToken\" : \"test-token-123\"  }  ";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertTrue(result.isValid());
		assertNull(result.getError());
	}

	public void testResultIsImmutable() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		// Verify no setter methods exist
		try {
			result.getClass().getMethod("setValid", boolean.class);
			fail("Result should not have setter methods");
		} catch (NoSuchMethodException e) {
			// Expected
		}

		try {
			result.getClass().getMethod("setError", String.class);
			fail("Result should not have setter methods");
		} catch (NoSuchMethodException e) {
			// Expected
		}
	}

	public void testTokenComparisonIsCaseSensitive() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"TEST-TOKEN-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, "test-token-123");

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	public void testNestedJsonObjectsDoNotCauseErrors() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"nested\":{\"field\":\"value\"}}";

		try {
			Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);
			assertTrue(result.isValid());
		} catch (Exception e) {
			fail("Should handle nested objects without throwing");
		}
	}

	public void testJsonArraysDoNotCauseErrors() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"array\":[1,2,3]}";

		try {
			Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);
			assertTrue(result.isValid());
		} catch (Exception e) {
			fail("Should handle arrays without throwing");
		}
	}
}