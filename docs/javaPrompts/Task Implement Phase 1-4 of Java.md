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



-----------------------------------------------------------------------
						Commit Messages 
-----------------------------------------------------------------------
✅ Phase 1 — Configuration & Contracts

Commit message

Phase 1: Add immutable runtime configuration and validation (Java control plane)

- Introduced AppConfig with strict argument parsing
- Added InvalidConfigException with clear diagnostics
- Enforced immutability and deterministic behavior
- Covered all validation paths with unit tests
- No IPC, UI, lifecycle, or native assumptions


Why this is good

Signals “foundation only”

Explicitly says what is not included

Safe to cherry-pick or revert independently

✅ Phase 2 — Process Lifecycle Controller

Commit message

Phase 2: Implement deterministic UI subsystem lifecycle controller

- Added ProcessController with explicit state machine
- Defined LifecycleListener event contract
- Enforced start/stop/restart invariants
- Covered error and double-start scenarios via tests
- No OS process control, IPC, threads, or UI knowledge


Why this is good

Emphasizes “modeling”, not execution

Makes it clear native lifecycle is untouched

✅ Phase 3 — IPC Contract (No Transport)

Commit message

Phase 3: Define IPC message contract and handshake validation

- Added message type definitions and schema validation
- Implemented handshake logic with session token verification
- Rejected unknown, malformed, or invalid messages safely
- Ignored extra fields by design
- No network, sockets, transport, or UI coupling


Why this is good

Locks protocol without implying connectivity

Makes Phase 4 review much easier

✅ Phase 4 — Secure IPC Transport (WSS)

Commit message

Phase 4: Add secure WSS transport for control-plane IPC

- Implemented WebSocket transport behind IMessageChannel
- Enforced TLS via OS trust store
- Sent HELLO handshake on successful connect
- Added reconnect and clean shutdown handling
- No UI logic or native assumptions


Why this is good

Clearly states security posture

Separates transport from behavior

✅ Phase 5 — UI Supervision (Phase 5 FINAL / FROZEN)

Commit message

Phase 5: Add UI supervision and resilience (CEF-agnostic)

- Treated UI as external, restartable subsystem
- Allowed fire-and-forget commands during UI downtime
- Preserved last intent for replay after restart
- Handled UI crash and unavailability deterministically
- No UI logic, navigation, JNI, or JS assumptions


Why this is good

Explicitly calls out CEF-agnostic

Makes it very clear Phase 6 is where wiring begins

🧊 Optional Meta Commit (Recommended)

After Phase 5 is frozen:

Freeze Phase 1–5: Java control plane stable and ready for Phase 6

- All Phase 1–5 tests passing
- No native, JNI, JS, or browser coupling
- UI treated as black-box external subsystem
- Ready for IPC ↔ JS ↔ CEF wiring in Phase 6