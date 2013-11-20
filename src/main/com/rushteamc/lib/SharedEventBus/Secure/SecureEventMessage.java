package com.rushteamc.lib.SharedEventBus.Secure;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SecureEventMessage implements Serializable
{
	private final String group;
	private final byte[] iv;
	private final byte[] data;
	
	public SecureEventMessage(String group, byte[] iv, byte[] data)
	{
		this.group = group;
		this.iv = iv;
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public byte[] getIv() {
		return iv;
	}

	public String getGroup() {
		return group;
	}
}
