package com.rushteamc.lib.SharedEventBus;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.rushteamc.lib.SharedEventBus.Secure.SecureEvent;
import com.rushteamc.lib.SharedEventBus.Secure.SecureEventMessage;
import com.rushteamc.lib.SharedEventBus.Secure.SecureEventSerializer;
import com.rushteamc.lib.SharedEventBus.socket.Communicator;
import com.rushteamc.lib.SharedEventBus.socket.ReceivedEventCallback;
import com.rushteamc.lib.SharedEventBus.socket.TCP.TCPCommunicator;

public class SharedEventBus
{
	private final InetSocketAddress[] addressList;
	private final Communicator communicator;
	private final ConcurrentLinkedQueue<Serializable> eventSendQueue = new ConcurrentLinkedQueue<Serializable>();
	private final ConcurrentLinkedQueue<Serializable> eventQueue = new ConcurrentLinkedQueue<Serializable>();
	private final Thread senderThread;
	private final Thread handlerThread;

	private final List<Object> handlers = new LinkedList<Object>();
	private final Map<Class<?>, List<HandlerCallList>> eventHandlerMethods = new HashMap<Class<?>, List<HandlerCallList>>();

	public enum Protocol
	{
		UDP,
		TCP
	}
	
	public SharedEventBus()
	{
		this(new InetSocketAddress[0]);
	}
	
	public SharedEventBus(Set<InetSocketAddress> adressList)
	{
		this(adressList.toArray(new InetSocketAddress[adressList.size()]));
	}
	
	public SharedEventBus(List<InetSocketAddress> adressList)
	{
		this(adressList.toArray(new InetSocketAddress[adressList.size()]));
	}
	
	public SharedEventBus(InetSocketAddress[] adressList)
	{
		this(adressList, Protocol.TCP);
	}
	
	public SharedEventBus(Set<InetSocketAddress> adressList, Protocol protocol)
	{
		this(adressList.toArray(new InetSocketAddress[adressList.size()]), protocol);
	}
	
	public SharedEventBus(List<InetSocketAddress> adressList, Protocol protocol)
	{
		this(adressList.toArray(new InetSocketAddress[adressList.size()]), protocol);
	}
	
	public SharedEventBus(InetSocketAddress[] addressList, Protocol protocol)
	{
		if(addressList == null)
			addressList = new InetSocketAddress[0];
		
		for( InetSocketAddress address : addressList )
		{
			if(address.isUnresolved())
				throw new IllegalArgumentException("All addresses in addressList need to be resolved.");
		}
		
		this.addressList = addressList;
		this.communicator = (protocol == Protocol.UDP)?( null ):( new TCPCommunicator(addressList) );
		
		communicator.subscribeReceivedEventCallback(new ReceivedEventCallback() {
			@Override
			public void onReceivedEvent(Serializable event)
			{
				try {
					if(event instanceof SecureEventMessage)
					{
							event = SecureEventSerializer.deserialize((SecureEventMessage) event);
					}
					eventQueue.add(event);
				} catch (ClassCastException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		senderThread = new StoppableThread() {
			public void run()
			{
				while(running)
				{
					Serializable event = eventSendQueue.poll();
					if(event == null)
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					else
						communicator.shareEvent(event);
				}
			}
		};
		senderThread.start();
		
		handlerThread = new StoppableThread() {
			public void run()
			{
				while(running)
				{
					Serializable event = eventQueue.poll();
					if(event == null)
						try {
							sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					else
						handleEvent(event);
				}
			}
		};
		handlerThread.start();
	}
	
	public void fire(Serializable event)
	{
		Serializable sendEvent = event;
		if(sendEvent instanceof SecureEvent)
		{
			try {
				sendEvent = SecureEventSerializer.serialize((SecureEvent) sendEvent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		eventSendQueue.add(sendEvent);
		eventQueue.add(event);
	}
	
	public void addHandler(Object handler)
	{
		if(handlers.contains(handler))
			return;
		handlers.add(handler);
		
		Map<Class<?>, List<Method>> map = new HashMap<Class<?>, List<Method>>();
		
		Method[] methods = handler.getClass().getDeclaredMethods();
		for (Method method : methods)
		{
			if(method.isAnnotationPresent(Subscribe.class))
			{
				Class<?>[] parameters = method.getParameterTypes();
				if(parameters.length != 1)
				{
					throw new IllegalArgumentException("@Subscribe presented to method " + method.getName() + " with multiple paramter.");
				}
				Class<?> parameter = parameters[0];
				List<Method> list = map.get(parameter);
				if(list == null)
					list = new LinkedList<Method>();
				list.add(method);
				map.put(parameter, list);
			}
		}
		
		Iterator<Entry<Class<?>, List<Method>>> it = map.entrySet().iterator();
		Method[] dummyArray = new Method[0];
		while( it.hasNext() )
		{
			Entry<Class<?>, List<Method>> entry = it.next();
			Class<?> type = entry.getKey();
			List<HandlerCallList> list = eventHandlerMethods.get(type);
			if(list == null)
				list = new CopyOnWriteArrayList<HandlerCallList>();
			HandlerCallList handlerCallList = new HandlerCallList(handler, entry.getValue().toArray(dummyArray));
			list.add(handlerCallList);
			eventHandlerMethods.put(type, list);
		}
	}
	
	private void handleEvent(Serializable event)
	{
		Class<? extends Serializable> cls = event.getClass();
		List<HandlerCallList> list = eventHandlerMethods.get(cls);
		
		if(list == null)
			return;
		
		for( HandlerCallList handlerCallList : list )
		{
			handlerCallList.call(event);
		}
	}
	
	public InetSocketAddress[] getAddressList() {
		return addressList;
	}

	private class HandlerCallList
	{
		private final Object obj;
		private final Method[] methods;
		
		public HandlerCallList(Object obj, Method[] methods)
		{
			this.obj = obj;
			this.methods = methods;
		}
		
		public void call(Object event)
		{
			for( Method method : methods )
			{
				try {
					method.invoke(obj, event);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
