package com.rushteamc.lib.SharedEventBus.socket;

import java.io.Serializable;

public interface Communicator
{
	public boolean isConnected();
	
	public void subscribeReceivedEventCallback(ReceivedEventCallback callback);

	public void shareEvent(Serializable event);
	
	public void close();
	public void close(boolean blocking);
}
