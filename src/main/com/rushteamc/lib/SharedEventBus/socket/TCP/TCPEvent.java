package com.rushteamc.lib.SharedEventBus.socket.TCP;

import java.io.Serializable;

public interface TCPEvent
{
	void onTCPEvent(Serializable event);
}
