package com.ui.cef_control.ipc;

/**
 * Message channel contract defining outbound message direction.
 *
 * Phase 3 defines the contract only. Implementation is provided in Phase 4.
 * No implementations of this interface exist in Phase 3.
 */
public interface IMessageChannel {
	/**
	 * Sends a message through this channel.
	 *
	 * @param message the message to send
	 */
	void send(String message);

	void close();
}