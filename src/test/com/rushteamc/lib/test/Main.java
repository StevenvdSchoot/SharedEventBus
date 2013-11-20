package com.rushteamc.lib.test;

import java.net.InetSocketAddress;
import java.util.Scanner;

import com.rushteamc.lib.SharedEventBus.SharedEventBus;
import com.rushteamc.lib.SharedEventBus.Subscribe;

public class Main
{
	public static Main main;
	
	public static void main(String[] args)
	{
		main = new Main();
	}
	
	public Main()
	{
		InetSocketAddress[] inetSocketAddress = new InetSocketAddress[]{ new InetSocketAddress("localhost", 8081)};
		
		SharedEventBus eventBus1 = new SharedEventBus(inetSocketAddress);
		//SharedEventBus eventBus2 = new SharedEventBus(inetSocketAddress);
		//SharedEventBus eventBus3 = new SharedEventBus(inetSocketAddress);
		
		eventBus1.addHandler(new Handler1());
		//eventBus2.addHandler(new Handler2());
		//eventBus2.addHandler(new Handler3());
		
		try {
			Thread.sleep(1000); // Give the eventbusses some time to connect.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//eventBus1.fire("test 1");
		//eventBus2.fire("test 2");
		//eventBus3.fire("test 3");
		
		Scanner scanner = new Scanner(System.in);
		while(true)
		{
			System.out.print("Fire string on eventbus: ");
			eventBus1.fire(scanner.nextLine());
			try {
				Thread.sleep(100); // Give the eventbusses some time to connect.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class Handler1
	{
		@Subscribe
		public void onString(String str)
		{
			// TODO Auto-generated catch block
			System.out.println("Handler 1 got string: " + str);
		}
	}
	
	public class Handler2
	{
		@Subscribe
		public void onString(String group, String str)
		{
			System.out.println("Handler 2 got string: " + str);
		}
	}
	
	public class Handler3
	{
		@Subscribe
		public void onString(String str)
		{
			System.out.println("Handler 3 got string: " + str);
		}
	}

}
