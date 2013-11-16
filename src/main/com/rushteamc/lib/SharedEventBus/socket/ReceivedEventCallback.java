package com.rushteamc.lib.SharedEventBus.socket;

import java.io.Serializable;

public interface ReceivedEventCallback
{
	public void onReceivedEvent(Serializable event);
}
