package com.anca.appl.fw.gui.cef_control.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public final class AppConfig {

	private final int ipcPort;
	private final String sessionToken;
	private final String startUrl;
	private final String windowId;

	private static final int MIN_PORT = 1024;
	private static final int MAX_PORT = 65535;
	private static final Set<String> REQUIRED_FLAGS = Set.of(
			"--ipcPort",
			"--sessionToken",
			"--startUrl",
			"--windowId"
	);

	private AppConfig(int ipcPort, String sessionToken, String startUrl, String windowId) {
		this.ipcPort = ipcPort;
		this.sessionToken = sessionToken;
		this.startUrl = startUrl;
		this.windowId = windowId;
	}

	public static AppConfig fromArgs(String[] args) {
		Map<String, String> parsed = parseArgs(args);
		validateAllRequiredPresent(parsed);

		int ipcPort = parseIpcPort(parsed.get("--ipcPort"));
		String sessionToken = validateNonEmpty(parsed.get("--sessionToken"), "sessionToken");
		String startUrl = validateNonEmpty(parsed.get("--startUrl"), "startUrl");
		String windowId = validateNonEmpty(parsed.get("--windowId"), "windowId");

		return new AppConfig(ipcPort, sessionToken, startUrl, windowId);
	}

	private static Map<String, String> parseArgs(String[] args) {
		Map<String, String> parsed = new HashMap<>();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (!arg.startsWith("--")) {
				throw new InvalidConfigException("Invalid argument format: " + arg);
			}

			if (i + 1 >= args.length) {
				throw new InvalidConfigException(
						"Flag " + arg + " requires a value"
				);
			}

			if (!REQUIRED_FLAGS.contains(arg)) {
				throw new InvalidConfigException("Unknown flag: " + arg);
			}

			parsed.put(arg, args[i + 1]);
			i++;
		}

		return parsed;
	}

	private static void validateAllRequiredPresent(Map<String, String> parsed) {
		for (String flag : REQUIRED_FLAGS) {
			if (!parsed.containsKey(flag)) {
				String paramName = flag.substring(2);
				throw new InvalidConfigException(
						"Required parameter missing: " + paramName
				);
			}
		}
	}

	private static int parseIpcPort(String value) {
		int port;
		try {
			port = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidConfigException(
					"ipcPort must be a valid integer",
					e
			);
		}

		if (port < MIN_PORT || port > MAX_PORT) {
			throw new InvalidConfigException(
					"ipcPort must be in range " + MIN_PORT + "-" + MAX_PORT +
							", got: " + port
			);
		}

		return port;
	}

	private static String validateNonEmpty(String value, String paramName) {
		if (value == null || value.isEmpty()) {
			throw new InvalidConfigException(
					paramName + " cannot be empty"
			);
		}
		return value;
	}

	public int getIpcPort() {
		return ipcPort;
	}

	public String getSessionToken() {
		return sessionToken;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public String getWindowId() {
		return windowId;
	}
}
