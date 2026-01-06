package com.anca.appl.fw.gui.cef_control.ipc;

public interface IMessageChannel {

	void send(String message);

	void close();
}