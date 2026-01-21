package com.anca.appl.fw.gui.cef_control.config;

import static org.junit.Assert.*;
import org.junit.Test;

public class AppConfigTest {

	@Test
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

	@Test
	public void testFromArgsRejectsMissingIpcPort() {
		String[] args = {
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
		}
	}

	@Test
	public void testFromArgsRejectsMissingSessionToken() {
		String[] args = {
				"--ipcPort", "8080",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("sessionToken"));
		}
	}

	@Test
	public void testFromArgsRejectsMissingStartUrl() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("startUrl"));
		}
	}

	@Test
	public void testFromArgsRejectsMissingWindowId() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("windowId"));
		}
	}

	@Test
	public void testFromArgsRejectsUnknownFlag() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001",
				"--unknownFlag", "value"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("unknownFlag"));
		}
	}

	@Test
	public void testFromArgsRejectsIpcPortBelowMinimum() {
		String[] args = {
				"--ipcPort", "1023",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
			assertTrue(ex.getMessage().contains("1024") || ex.getMessage().contains("range"));
		}
	}

	@Test
	public void testFromArgsRejectsIpcPortAboveMaximum() {
		String[] args = {
				"--ipcPort", "65536",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
			assertTrue(ex.getMessage().contains("65535") || ex.getMessage().contains("range"));
		}
	}

	@Test
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

	@Test
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

	@Test
	public void testFromArgsRejectsNonNumericIpcPort() {
		String[] args = {
				"--ipcPort", "notANumber",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("ipcPort"));
		}
	}

	@Test
	public void testFromArgsRejectsEmptySessionToken() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "",
				"--startUrl", "https://example.com",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("sessionToken"));
			assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("blank"));
		}
	}

	@Test
	public void testFromArgsRejectsEmptyStartUrl() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "",
				"--windowId", "win-001"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("startUrl"));
			assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("blank"));
		}
	}

	@Test
	public void testFromArgsRejectsEmptyWindowId() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId", ""
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("windowId"));
			assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("blank"));
		}
	}

	@Test
	public void testFromArgsRejectsFlagWithoutValue() {
		String[] args = {
				"--ipcPort", "8080",
				"--sessionToken", "abc123",
				"--startUrl", "https://example.com",
				"--windowId"
		};

		try {
			assertThrows(InvalidConfigException.class, () -> AppConfig.fromArgs(args));
		} catch (InvalidConfigException ex) {
			assertTrue(ex.getMessage().contains("windowId") || ex.getMessage().contains("value"));
		}
	}

	@Test
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
