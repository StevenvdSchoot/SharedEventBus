package com.rushteamc.lib.SharedEventBus.socket.UDP;

import java.io.Serializable;
import java.net.InetSocketAddress;

import com.rushteamc.lib.SharedEventBus.socket.Communicator;
import com.rushteamc.lib.SharedEventBus.socket.ReceivedEventCallback;

public class UDPCommunicator implements Communicator
{
	public UDPCommunicator(InetSocketAddress[] addressList)
	{
		;
	}

	@Override
	public boolean isConnected()
	{
		return false;
	}

	@Override
	public void subscribeReceivedEventCallback(ReceivedEventCallback callback)
	{
		;
	}

	@Override
	public void shareEvent(Serializable event)
	{
		;
	}

	@Override
	public void close()
	{
		;
	}

	@Override
	public void close(boolean blocking)
	{
		;
	}

}
