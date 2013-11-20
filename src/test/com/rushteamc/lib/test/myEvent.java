package com.rushteamc.lib.test;

import java.io.Serializable;

@SuppressWarnings("serial")
public class myEvent implements Serializable
{
	private final String str;
	
	public myEvent(String str)
	{
		this.str = str;
	}
	
	public String toString()
	{
		return str;
	}
}
