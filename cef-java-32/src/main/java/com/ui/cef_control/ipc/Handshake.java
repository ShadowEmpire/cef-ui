package com.anca.appl.fw.gui.cef_control.ipc;

import java.util.HashMap;
import java.util.Map;

public final class Handshake {

	private Handshake() {
		// Utility class - prevent instantiation
	}

	public static Result validateMessage(String json, String expectedToken) {
		if (json == null || json.trim().isEmpty()) {
			return Result.invalid("Message cannot be null or empty");
		}

		Map<String, Object> parsed;
		try {
			parsed = parseJson(json);
		} catch (IllegalArgumentException e) {
			return Result.invalid("Invalid JSON format: " + e.getMessage());
		}

		return validateSemantics(parsed, expectedToken);
	}

	private static Result validateSemantics(Map<String, Object> parsed, String expectedToken) {
		Object typeObj = parsed.get("type");
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

		if (!MessageTypes.HELLO.equals(type)) {
			return Result.invalid("Unknown message type: " + type);
		}

		// Validate session token
		Object tokenObj = parsed.get("sessionToken");
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

	private static Map<String, Object> parseJson(String json) throws IllegalArgumentException {
		json = json.trim();

		if (!json.startsWith("{") || !json.endsWith("}")) {
			throw new IllegalArgumentException("JSON must be an object");
		}

		Map<String, Object> result = new HashMap<>();

		// Remove outer braces
		String content = json.substring(1, json.length() - 1).trim();

		if (content.isEmpty()) {
			return result;
		}

		// Simple JSON parser for string key-value pairs
		int depth = 0;
		int start = 0;
		boolean inString = false;
		boolean escaped = false;

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);

			if (escaped) {
				escaped = false;
				continue;
			}

			if (c == '\\') {
				escaped = true;
				continue;
			}

			if (c == '"') {
				inString = !inString;
				continue;
			}

			if (inString) {
				continue;
			}

			if (c == '{' || c == '[') {
				depth++;
			} else if (c == '}' || c == ']') {
				depth--;
			} else if (c == ',' && depth == 0) {
				parsePair(content.substring(start, i).trim(), result);
				start = i + 1;
			}
		}

		// Parse last pair
		if (start < content.length()) {
			parsePair(content.substring(start).trim(), result);
		}

		return result;
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