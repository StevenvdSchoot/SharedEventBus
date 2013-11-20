package com.rushteamc.lib.SharedEventBus.Secure;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SecureEvent implements Serializable
{
	private final String group;
	private final Serializable event;
	
	public SecureEvent(String group, Serializable event)
	{
		this.group = group;
		this.event = event;
	}

	public Serializable getEvent()
	{
		return event;
	}

	public String getGroup() {
		return group;
	}
}
