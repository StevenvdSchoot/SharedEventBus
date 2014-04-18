package com.rushteamc.lib.test;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AnotherEvent implements Serializable
{
	private final String str;
	
	public AnotherEvent(String str)
	{
		this.str = str;
	}
	
	public String toString()
	{
		return str;
	}
}
