package com.rushteamc.lib.SharedEventBus.socket;

import java.io.Serializable;
import java.net.InetSocketAddress;

public interface Communicator
{
	public boolean isConnected();
	
	public void subscribeReceivedEventCallback(ReceivedEventCallback callback);

	public void shareEvent(Serializable event);
}
