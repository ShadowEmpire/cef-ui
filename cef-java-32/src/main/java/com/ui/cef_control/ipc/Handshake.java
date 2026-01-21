package com.anca.appl.fw.gui.cef_control.ipc;

import java.util.HashMap;
import java.util.Map;

//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class Handshake {

	private static final String EXPECTED_SESSION_TOKEN = "test-token-123";

	public static void handle(String json) {
		if (json == null || json.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid JSON");
		}

		Map<String, Object> message = parseJson(json);   // syntax only
		validateMessage(message);                        // semantics only
	}

	private Handshake() {
		// Utility class - prevent instantiation
	}

	private static void validateHello(Map<String, Object> message) {
		Object token = message.get("sessionToken");

		if (!(token instanceof String) || ((String) token).isEmpty()) {
			throw new IllegalArgumentException("Missing or invalid sessionToken");
		}

		if (!EXPECTED_SESSION_TOKEN.equals((String) token)) {
			throw new IllegalArgumentException("Invalid session token");
		}
	}

	private static void validateMessage(Map<String, Object> message) {
		String typeValue = (String) message.get("type");
		if (typeValue == null) {
			throw new IllegalArgumentException("Missing message type");
		}

		MessageTypes type = MessageTypes.from(typeValue);

		switch (type) {
			case HELLO:
				validateHello(message);
				break;
			default:
				throw new IllegalArgumentException("Unsupported message type: " + type);
		}

		// Extra fields are intentionally ignored (Phase 3 IPC contract)
	}

	private static Result validateSemantics(Map<String, Object> parsedMessage, String expectedToken) {
		Object typeObj = parsedMessage.get("type");
		if (typeObj == null) {
			return Result.invalid("Missing required field: type");
		}

		if (!(typeObj instanceof String)) {
			return Result.invalid("Field 'type' must be a string");
		}

		String type = (String) typeObj;
		if (type.isEmpty()) {
			return Result.invalid("Field 'type' cannot be empty");
		}

		String typeValue = (String) parsedMessage.get("type");
		if (typeValue == null) {
			throw new IllegalArgumentException("Missing message type");
		}

		MessageTypes messageType = MessageTypes.from(typeValue);

		// Validate session token
		Object tokenObj = parsedMessage.get("sessionToken");
		if (tokenObj == null) {
			return Result.invalid("Missing required field: sessionToken");
		}

		if (!(tokenObj instanceof String)) {
			return Result.invalid("Field 'sessionToken' must be a string");
		}

		String token = (String) tokenObj;
		if (token.isEmpty()) {
			return Result.invalid("Field 'sessionToken' cannot be empty");
		}

		if (!expectedToken.equals(token)) {
			return Result.invalid("Invalid session token");
		}

		return Result.valid();
	}

	private static Map<String, Object> parseJson(String jsonObj) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(jsonObj);
			Map<String, Object> result = new HashMap<>();

			for (Object key : object.keySet()) {
				result.put((String)key, object.get(key));
			}

			return result;
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid JSON", e);
		}
	}

	private static void parsePair(String pair, Map<String, Object> result) throws IllegalArgumentException {
		if (pair.isEmpty()) {
			return;
		}

		int colonIndex = pair.indexOf(':');
		if (colonIndex == -1) {
			throw new IllegalArgumentException("Invalid key-value pair: " + pair);
		}

		String key = pair.substring(0, colonIndex).trim();
		String value = pair.substring(colonIndex + 1).trim();

		// Remove quotes from key
		if (key.startsWith("\"") && key.endsWith("\"")) {
			key = key.substring(1, key.length() - 1);
		} else {
			throw new IllegalArgumentException("Key must be quoted: " + key);
		}

		// Parse value
		Object parsedValue = parseValue(value);
		result.put(key, parsedValue);
	}

	private static Object parseValue(String value) {
		if (value.equals("null")) {
			return null;
		}

		if (value.equals("true")) {
			return Boolean.TRUE;
		}

		if (value.equals("false")) {
			return Boolean.FALSE;
		}

		if (value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}

		if (value.startsWith("{") || value.startsWith("[")) {
			// Nested object or array - just return as marker
			return value;
		}

		// Try to parse as number
		try {
			if (value.contains(".")) {
				return Double.parseDouble(value);
			} else {
				return Integer.parseInt(value);
			}
		} catch (NumberFormatException e) {
			// Return as string if not a valid number
			return value;
		}
	}

	public static final class Result {
		private final boolean valid;
		private final String error;

		private Result(boolean valid, String error) {
			this.valid = valid;
			this.error = error;
		}

		public static Result valid() {
			return new Result(true, null);
		}

		public static Result invalid(String error) {
			return new Result(false, error);
		}

		public boolean isValid() {
			return valid;
		}

		public String getError() {
			return error;
		}
	}
}