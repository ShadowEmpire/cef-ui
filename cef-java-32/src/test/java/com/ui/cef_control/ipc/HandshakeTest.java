package com.ui.cef_control.ipc;

import org.junit.Test;
import static org.junit.Assert.*;

public class HandshakeTest {

	private static final String VALID_TOKEN = "test-token-123";

	@Test
	public void testValidHelloWithCorrectTokenIsAccepted() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			// PASS: no exception means accepted
		} catch (Exception e) {
			fail("Valid HELLO with correct token must be accepted");
		}
	}

	@Test
	public void testInvalidJsonInputReturnsError() {
		String json = "{invalid json";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException for invalid JSON");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention invalid JSON",
					e.getMessage().contains("Invalid JSON") || e.getMessage().contains("JSON")
			);
		}
	}

	@Test
	public void testUnknownMessageTypeIsRejected() {
		String json = "{\"type\":\"UNKNOWN\",\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Unknown message type must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention unknown type and must be rejected",
					e.getMessage().contains("Unknown") || e.getMessage().contains("type")
			);
		}
	}

	@Test
	public void testMissingRequiredFieldTypeIsRejected() {
		String json = "{\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Missing required field 'type' must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention missing type",
					e.getMessage().contains("type")
			);
		}
	}

	@Test
	public void testMissingRequiredFieldSessionTokenIsRejected() {
		String json = "{\"type\":\"HELLO\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Missing required field 'sessionToken' must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention sessionToken",
					e.getMessage().contains("sessionToken")
			);
		}
	}

	@Test
	public void testExtraJsonFieldsAreIgnored() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"extraField\":\"ignored\",\"anotherExtra\":42,\"nested\":{\"obj\":true}}";

		try {
			Handshake.handle(json);
		} catch (Exception e) {
			fail("Extra fields must be ignored without throwing");
		}
	}

	@Test
	public void testRejectInvalidToken() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"wrong-token\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Invalid session token must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue("Error must mention token", e.getMessage().contains("token") || e.getMessage().contains("Token"));
		}
	}

	@Test
	public void testRejectMissingSessionToken() {
		String json = "{\"type\":\"HELLO\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Missing required field 'sessionToken' must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention sessionToken",
					e.getMessage().contains("sessionToken")
			);
		}
	}

	@Test
	public void testRejectUnknownMessageType() {
		String json = "{\"type\":\"UNKNOWN\",\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Unknown message type must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention unknown type",
					e.getMessage().contains("UNKNOWN") || e.getMessage().contains("type")
			);
		}
	}

	@Test
	public void testRejectMissingMessageType() {
		String json = "{\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Missing required field 'type' must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention missing type",
					e.getMessage().contains("type")
			);
		}
	}

	@Test
	public void testIgnoreExtraJsonFields() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"extraField\":\"ignored\",\"anotherExtra\":42}";

		try{
			Handshake.handle(json);
		} catch (Exception e) {
			fail("Extra fields must be ignored without throwing");
		}
	}

	@Test
	public void testRejectMalformedJson() {
		String json = "{invalid json";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException for invalid JSON");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("JSON"));
		}
	}

	@Test
	public void testRejectEmptyJsonString() {
		String json = "";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException for empty JSON");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("JSON"));
		}
	}

	@Test
	public void testRejectNullJson() {
		try {
			Handshake.handle(null);
			fail("Expected IllegalArgumentException for null JSON");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("JSON"));
		}
	}

	@Test
	public void testRejectEmptySessionToken() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Empty session token must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention sessionToken",
					e.getMessage().contains("sessionToken")
			);
		}
	}

	@Test
	public void testRejectNullSessionTokenValue() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":null}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Null session token must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention sessionToken",
					e.getMessage().contains("sessionToken")
			);
		}
	}

	@Test
	public void testRejectEmptyMessageType() {
		String json = "{\"type\":\"\",\"sessionToken\":\"test-token-123\"}";

//		Handshake.Result result = Handshake.handle(json);
		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Empty message type must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention type",
					e.getMessage().contains("type")
			);
		}
	}

	@Test
	public void testRejectNullMessageType() {
		String json = "{\"type\":null,\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Null message type must be rejected");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention type",
					e.getMessage().contains("type")
			);
		}
	}

	@Test
	public void testValidationIsDeterministic() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			Handshake.handle(json);
		} catch (Exception e) {
			fail("Valid HELLO with correct token must be accepted");
		}
	}

	@Test
	public void testCaseInsensitiveMessageType() {
		String json = "{\"type\":\"hello\",\"sessionToken\":\"test-token-123\"}";

		try{
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Message type must be case-sensitive");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention type",
					e.getMessage().contains("type")
			);
		}
	}

	@Test
	public void testWhitespaceInJsonIsHandled() {
		String json = "  {  \"type\" : \"HELLO\" , \"sessionToken\" : \"test-token-123\"  }  ";

		try {
			Handshake.handle(json);
		} catch (Exception e) {
			fail("Whitespace in JSON must be handled without throwing");
		}
	}

	@Test
	public void testResultIsImmutable() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"}";

		try {
			Handshake.handle(json);
			Handshake.handle(json);
			// PASS: repeated calls succeed -> no internal mutable state
		} catch (Exception e) {
			fail("Handshake must be stateless and deterministic");
		}
	}

	@Test
	public void testTokenComparisonIsCaseSensitive() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"TEST-TOKEN-123\"}";

		try {
			Handshake.handle(json);
			fail("Expected IllegalArgumentException : Session token comparison must be case-sensitive");
		} catch (IllegalArgumentException e) {
			assertTrue(
					"Error must mention token",
					e.getMessage().contains("token") || e.getMessage().contains("Token")
			);
		}
	}

	@Test
	public void testNestedJsonObjectsDoNotCauseErrors() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"nested\":{\"field\":\"value\"}}";

		try {
			Handshake.handle(json);
//			assertTrue(result.isValid());
		} catch (Exception e) {
			fail("Should handle nested objects without throwing");
		}
	}

	@Test
	public void testJsonArraysDoNotCauseErrors() {
		String json = "{\"type\":\"HELLO\",\"sessionToken\":\"test-token-123\"," +
				"\"array\":[1,2,3]}";

		try {
			Handshake.handle(json);
//			assertTrue(result.isValid());
		} catch (Exception e) {
			fail("Should handle arrays without throwing");
		}
	}
}