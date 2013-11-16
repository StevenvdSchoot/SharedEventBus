package com.rushteamc.lib.SharedEventBus;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class EventInetSocketAddressList implements Serializable
{
	private final InetSocketAddress[] socketList;
	
	public EventInetSocketAddressList(InetSocketAddress[] socketList)
	{
		this.socketList = socketList;
	}

	public InetSocketAddress[] getSocketList() {
		return socketList;
	}
}
