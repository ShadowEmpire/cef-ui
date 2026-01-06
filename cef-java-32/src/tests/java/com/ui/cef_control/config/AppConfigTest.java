package com.anca.appl.fw.gui.cef_control.config;

import junit.framework.TestCase;

public class AppConfigTest extends TestCase {

	public void testFromArgsWithValidArgumentsCreatesConfig() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		AppConfig config = AppConfig.fromArgs(args);

		assertEquals(8080, config.getIpcPort());
		assertEquals("abc123", config.getSessionToken());
		assertEquals("https://example.com", config.getStartUrl());
		assertEquals("win-001", config.getWindowId());
	}

	public void testFromArgsRejectsMissingIpcPort() {
		String[] args = {
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
		}
	}

	public void testFromArgsRejectsMissingSessionToken() {
		String[] args = {
				"--ipcPort", "8080",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("sessionToken"));
		}
	}

	public void testFromArgsRejectsMissingStartUrl() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("startUrl"));
		}
	}

	public void testFromArgsRejectsMissingWindowId() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("windowId"));
		}
	}

	public void testFromArgsRejectsUnknownFlag() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001",
				"--unknownFlag", "value"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("unknownFlag"));
		}
	}

	public void testFromArgsRejectsIpcPortBelowMinimum() {
		String[] args = {
				"--ipcPort", "1023",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
			assertTrue(ex.getMessage().contains("1024") || ex.getMessage().contains("range"));
		}
	}

	public void testFromArgsRejectsIpcPortAboveMaximum() {
		String[] args = {
				"--ipcPort", "65536",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
			assertTrue(ex.getMessage().contains("65535") || ex.getMessage().contains("range"));
		}
	}

	public void testFromArgsAcceptsIpcPortAtLowerBoundary() {
		String[] args = {
				"--ipcPort", "1024",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		AppConfig config = AppConfig.fromArgs(args);

		assertEquals(1024, config.getIpcPort());
	}

	public void testFromArgsAcceptsIpcPortAtUpperBoundary() {
		String[] args = {
				"--ipcPort", "65535",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		AppConfig config = AppConfig.fromArgs(args);

		assertEquals(65535, config.getIpcPort());
	}

	public void testFromArgsRejectsNonNumericIpcPort() {
		String[] args = {
				"--ipcPort", "notANumber",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
		}
	}

	public void testFromArgsRejectsEmptySessionToken() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("sessionToken"));
			assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("blank"));
		}
	}

	public void testFromArgsRejectsEmptyStartUrl() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "",
				"--windowId", "win-001"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("startUrl"));
			assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("blank"));
		}
	}

	public void testFromArgsRejectsEmptyWindowId() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", ""
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("windowId"));
			assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("blank"));
		}
	}

	public void testFromArgsRejectsFlagWithoutValue() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId"
		};

		try {
			AppConfig.fromArgs(args);
			fail("Expected InvalidConfigException");
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("windowId") || ex.getMessage().contains("value"));
		}
	}

	public void testAppConfigIsImmutableNoSetters() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		AppConfig config = AppConfig.fromArgs(args);

		// Verify no setter methods exist
		try {
			config.getClass().getMethod("setIpcPort", int.class);
			fail("AppConfig should not have setter methods");
		} catch (NoSuchMethodException e) {
			// Expected
		}

		try {
			config.getClass().getMethod("setSessionToken", String.class);
			fail("AppConfig should not have setter methods");
		} catch (NoSuchMethodException e) {
			// Expected
		}
	}
}
