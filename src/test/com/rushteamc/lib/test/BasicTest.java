package com.rushteamc.lib.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import static org.junit.Assert.fail;

import com.rushteamc.lib.SharedEventBus.SharedEventBus;
import com.rushteamc.lib.SharedEventBus.Subscribe;

public class BasicTest
{
	private static Logger log = Logger.getLogger(BasicTest.class.getName());
	static
	{
		try {
			log.addHandler(new FileHandler("basicTest.log",  false));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static InetSocketAddress[] inetSocketAddress = new InetSocketAddress[]{ new InetSocketAddress("localhost", 8081)};
	
	private SharedEventBus eventbus1;
	private handler handler1;
	private SharedEventBus eventbus2;
	private handler handler2;
	private SharedEventBus eventbus3;
	private handler handler3;
	
	private int continueCounter;
	
	@Test
	public void basicTest()
	{
		log.info("Starting basic test 1...");
		basicTest1();

		log.info("Starting basic test 2...");
		basicTest2();

		log.info("Starting basic test 3...");
		basicTest3();
	}
	
	public void basicTest1()
	{
		log.info("Creating eventbus 1");
		eventbus1 = new SharedEventBus();
		handler1 = new handler("1");
		handler1.setEventGotCallback(new EventGotCallback() {
			@Override
			public void onEventGot(handler h)
			{
				continueCounter++;
			}
		});
		eventbus1.addHandler(handler1);
		
		log.info("Firing String event on eventbus 1...");
		continueCounter = 0;
		eventbus1.fire("String event on eventbus 1");
		
		for(int i = 0; i < 1000 ; i++)
		{
			if(continueCounter == 1)
				break;
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { }
		}
		
		if(continueCounter != 1)
		{
			log.log(Level.SEVERE, "Did not receive event in time! (waited  sec.)");
			fail("Did not receive event in time! (waited  sec.)");
		}
	}
	
	public void basicTest2()
	{
		log.info("Recreating eventbus 1 using localhost:8081");
		eventbus1 = new SharedEventBus(inetSocketAddress);
		handler1 = new handler("1");
		handler1.setEventGotCallback(new EventGotCallback() {
			@Override
			public void onEventGot(handler h)
			{
				continueCounter++;
			}
		});
		eventbus1.addHandler(handler1);
		
		log.info("waiting 100ms to give eventbus 1 time to establish server...");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) { }
		
		log.info("Creating eventbus 2 using localhost:8081");
		eventbus2 = new SharedEventBus(inetSocketAddress);
		handler2 = new handler("2");
		handler2.setEventGotCallback(new EventGotCallback() {
			@Override
			public void onEventGot(handler h)
			{
				continueCounter++;
			}
		});
		eventbus2.addHandler(handler2);
		
		log.info("waiting 100ms to give eventbus 2 time to establish connection with event bus 1...");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) { }
		
		log.info("Firing String event on eventbus 1...");
		continueCounter = 0;
		eventbus1.fire("String event on eventbus 1");
		
		for(int i = 0; i < 1000 ; i++)
		{
			if(continueCounter == 2)
				break;
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { }
		}
		
		if(continueCounter != 2)
		{
			log.log(Level.SEVERE, "Did not receive event in time! (waited  sec.)");
			fail("Did not receive event in time! (waited  sec.)");
		}
	}
	
	public void basicTest3()
	{
		log.info("Using eventbus 1 and 2");
		
		log.info("Creating eventbus 3 using localhost:8081");
		eventbus3 = new SharedEventBus(inetSocketAddress);
		handler3 = new handler("3");
		handler3.setEventGotCallback(new EventGotCallback() {
			@Override
			public void onEventGot(handler h)
			{
				continueCounter++;
			}
		});
		eventbus3.addHandler(handler3);
		
		log.info("waiting 100ms to give eventbus 3 time to establish connection with event bus 1...");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) { }
		
		/*
		 * Initial event fire test
		 */
		
		log.info("Firing String event on eventbus 1...");
		continueCounter = 0;
		eventbus1.fire("String event on eventbus 1");
		
		for(int i = 0; i < 1000 ; i++)
		{
			if(continueCounter == 3)
				break;
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { }
		}
		
		if(continueCounter != 3)
		{
			log.log(Level.SEVERE, "Did not receive event in time! (waited  sec.)");
			fail("Did not receive event in time! (waited  sec.)");
		}
		
		/*
		 * Simulating shutdown / crash of eventbus 1
		 */

		log.info("Simulating shutdown / crash of eventbus 1");
		eventbus1.close(true);
		eventbus1 = null;
		
		/*
		 * Final event fire test
		 */
		
		log.info("Firing String event on eventbus 2...");
		continueCounter = 0;
		eventbus2.fire("String event on eventbus 2");
		
		for(int i = 0; i < 1000 ; i++)
		{
			if(continueCounter == 2)
				break;
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { }
		}
		
		if(continueCounter != 2)
		{
			log.log(Level.SEVERE, "Did not receive event in time! (waited  sec.)");
			fail("Did not receive event in time! (waited  sec.)");
		}
	}
	
	public class handler
	{
		private final String name;
		private EventGotCallback callback;
		
		public handler(String name)
		{
			this.name = name;
		}
		
		@Subscribe
		public void onStringEvent(String event)
		{
			log.info("Handler " + name + " got String event: " + event);
			callback.onEventGot(this);
		}
		
		public void setEventGotCallback(EventGotCallback callback)
		{
			this.callback = callback;
		}
	}
	
	public interface EventGotCallback
	{
		public void onEventGot(handler h);
	}
}
