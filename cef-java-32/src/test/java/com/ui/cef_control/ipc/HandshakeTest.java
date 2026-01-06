package com.anca.appl.fw.gui.cef_control.ipc;

import org.junit.Test;
import static org.junit.Assert.*;

public class HandshakeTest {

	private static final String VALID_TOKEN = "test-token-123";

	@Test
	public void testValidHelloWithCorrectTokenIsAccepted() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertTrue("Valid HELLO with correct token must be accepted", result.isValid());
		assertNull(result.getError());
	}

	@Test
	public void testInvalidJsonInputReturnsError() {
		String json = "{invalid json";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse("Invalid JSON must be rejected", result.isValid());
		assertNotNull("Error message must be provided", result.getError());
		assertTrue("Error must mention JSON",
				result.getError().contains("JSON") || result.getError().contains("format"));
	}

	@Test
	public void testUnknownMessageTypeIsRejected() {
		String json = "{\"type\":\"UNKNOWN\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse("Unknown message type must be rejected", result.isValid());
		assertNotNull("Error message must be provided", result.getError());
		assertTrue("Error must mention unknown type",
				result.getError().contains("Unknown") || result.getError().contains("type"));
	}

	@Test
	public void testMissingRequiredFieldTypeIsRejected() {
		String json = "{\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse("Missing required field 'type' must be rejected", result.isValid());
		assertNotNull("Error message must be provided", result.getError());
		assertTrue("Error must mention missing type", result.getError().contains("type"));
	}

	@Test
	public void testMissingRequiredFieldSessionTokenIsRejected() {
		String json = "{\"type\":\"HELLO\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse("Missing required field 'sessionToken' must be rejected", result.isValid());
		assertNotNull("Error message must be provided", result.getError());
		assertTrue("Error must mention sessionToken", result.getError().contains("sessionToken"));
	}

	@Test
	public void testExtraJsonFieldsAreIgnored() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"extraField\":\"ignored\",\"anotherExtra\":42,\"nested\":{\"obj\":true}}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertTrue("Extra fields must be ignored", result.isValid());
		assertNull("No error for extra fields", result.getError());
	}

	@Test
	public void testRejectInvalidToken() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"wrong-token\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("token") ||
				result.getError().contains("Token"));
	}

	@Test
	public void testRejectMissingSessionToken() {
		String json = "{\"type\":\"HELLO\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("sessionToken") ||
				result.getError().contains("token"));
	}

	@Test
	public void testRejectUnknownMessageType() {
		String json = "{\"type\":\"UNKNOWN\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("type") ||
				result.getError().contains("UNKNOWN"));
	}

	@Test
	public void testRejectMissingMessageType() {
		String json = "{\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
		assertTrue(result.getError().contains("type"));
	}

	@Test
	public void testIgnoreExtraJsonFields() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"extraField\":\"ignored\",\"anotherExtra\":42}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertTrue(result.isValid());
		assertNull(result.getError());
	}

	@Test
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

	@Test
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

	@Test
	public void testHandleNullJsonWithoutThrowing() {
		try {
			Handshake.Result result = Handshake.validateMessage(null, VALID_TOKEN);
			assertFalse(result.isValid());
			assertNotNull(result.getError());
		} catch (Exception e) {
			fail("Should not throw exception on null JSON");
		}
	}

	@Test
	public void testRejectEmptySessionToken() {
		String json = "{\"type\":\"HELLO\",\"sessionToken:\"\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	@Test
	public void testRejectNullSessionTokenValue() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":null}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	@Test
	public void testRejectEmptyMessageType() {
		String json = "{\"type\":\"\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	@Test
	public void testRejectNullMessageType() {
		String json = "{\"type\":null,\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	@Test
	public void testValidationIsDeterministic() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result1 = Handshake.validateMessage(json, VALID_TOKEN);
		Handshake.Result result2 = Handshake.validateMessage(json, VALID_TOKEN);

		assertEquals(result1.isValid(), result2.isValid());
		if (result1.getError() != null) {
			assertEquals(result1.getError(), result2.getError());
		}
	}

	@Test
	public void testCaseInsensitiveMessageType() {
		String json = "{\"type\":\"hello\",\"sessionToken\":\"test-token-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertFalse(result.isValid());
		assertTrue(result.getError().contains("type") ||
				result.getError().contains("hello"));
	}

	@Test
	public void testWhitespaceInJsonIsHandled() {
		String json = "  {  \"type\" : \"HELLO\" , \"sessionToken\" : \"test-token-123\"  }  ";

		Handshake.Result result = Handshake.validateMessage(json, VALID_TOKEN);

		assertTrue(result.isValid());
		assertNull(result.getError());
	}

	@Test
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

	@Test
	public void testTokenComparisonIsCaseSensitive() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"TEST-TOKEN-123\"}";

		Handshake.Result result = Handshake.validateMessage(json, "test-token-123");

		assertFalse(result.isValid());
		assertNotNull(result.getError());
	}

	@Test
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

	@Test
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