Task: Implement Phase 1 ONLY of Java Control Plane TDD.

Rules (mandatory):
- Tests FIRST (JUnit 5).
- Implement ONLY what tests require.
- Do NOT reference CEF, browser, UI, IPC, sockets, processes, threads, timers, or native concepts.
- No placeholders for future phases.
- No static globals, no singletons.

Scope: Implement immutable runtime configuration parsing.

Classes allowed:
- config.AppConfig
- config.InvalidConfigException

Behavior to test and implement:
- AppConfig.fromArgs(String[] args)
	- Requires arguments:
		- --ipcPort (int, 1024–65535)
		- --sessionToken (non-empty string)
		- --startUrl (non-empty string)
		- --windowId (non-empty string)
	- Rejects:
		- Missing arguments
		- Unknown flags
		- Invalid numeric ranges
		- Empty values

- InvalidConfigException
	- Extends RuntimeException
	- Message must clearly identify the failing parameter

Constraints:
- AppConfig must be immutable
- No setters
- No environment variable access
- No defaults

Exit condition:
- All config tests pass
- No other packages touched

-----------------------------------------------------------------------

Task: Implement Phase 2 ONLY of Java Control Plane TDD.

Rules (mandatory):
- Tests FIRST
- No threads
- No timers
- No ProcessBuilder, Runtime.exec, or OS calls
- No IPC, sockets, JNI, UI, browser, or CEF references

Scope: External subsystem lifecycle modeling ONLY.

Classes allowed:
- lifecycle.ProcessController
- lifecycle.LifecycleListener

Behavior to test and implement:
- start()
	- Emits onStarted() exactly once
	- Rejects double-start
- stop()
	- Emits onStopping() then onStopped()
- restart()
	- Allowed only after stopped or error
- onError(Throwable)
	- Transitions to stopped state

Constraints:
- State machine must be explicit
- Events must be deterministic
- Listener invocation order must be testable

Exit condition:
- All lifecycle tests pass
- No process management code exists

-----------------------------------------------------------------------

Task: Implement Phase 3 ONLY of Java Control Plane TDD.

Rules:
- Tests FIRST
- No sockets
- No WebSocket libs
- No threads
- No reflection-based dispatch

Scope: Message schema + validation only.

Classes allowed:
- ipc.IMessageChannel (interface only)
- ipc.Handshake
- ipc.MessageTypes

Behavior to test and implement:
- Accept valid HELLO with correct sessionToken
- Reject invalid token
- Reject unknown message types
- Reject missing required fields
- Ignore extra JSON fields
- Handle malformed JSON without throwing

Constraints:
- Deterministic parsing
- No side effects

Exit condition:
- All IPC contract tests pass
- No transport code exists

-----------------------------------------------------------------------
