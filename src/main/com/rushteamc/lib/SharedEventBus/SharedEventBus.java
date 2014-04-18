package com.rushteamc.lib.SharedEventBus;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.SecretKey;

import com.rushteamc.lib.SharedEventBus.Secure.SecureEvent;
import com.rushteamc.lib.SharedEventBus.Secure.SecureEventMessage;
import com.rushteamc.lib.SharedEventBus.Secure.SecureEventSerializer;
import com.rushteamc.lib.SharedEventBus.socket.Communicator;
import com.rushteamc.lib.SharedEventBus.socket.ReceivedEventCallback;
import com.rushteamc.lib.SharedEventBus.socket.TCP.TCPCommunicator;
import com.rushteamc.lib.SharedEventBus.socket.UDP.UDPCommunicator;

public class SharedEventBus
{
	private final InetSocketAddress[] addressList;
	private final Communicator communicator;
	private final ConcurrentLinkedQueue<Serializable> eventSendQueue = new ConcurrentLinkedQueue<Serializable>();
	private final ConcurrentLinkedQueue<Serializable> eventQueue = new ConcurrentLinkedQueue<Serializable>();
	private final StoppableThread senderThread;
	private final StoppableThread handlerThread;

	private final List<Object> handlers = new LinkedList<Object>();
	private final Map<Class<?>, List<HandlerCallList>> eventHandlerMethods = new HashMap<Class<?>, List<HandlerCallList>>();
	private final SecureEventSerializer secureEventSerializer = new SecureEventSerializer();
	
	public enum Protocol
	{
		@Deprecated
		UDP,
		TCP
	}
	
	public SharedEventBus()
	{
		this(new InetSocketAddress[0]);
	}
	
	public SharedEventBus(Collection<InetSocketAddress> adressList)
	{
		this(adressList.toArray(new InetSocketAddress[adressList.size()]));
	}

	public SharedEventBus(InetSocketAddress[] adressList)
	{
		this(adressList, Protocol.TCP);
	}
	
	public SharedEventBus(Collection<InetSocketAddress> adressList, Protocol protocol)
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
		this.communicator = (protocol == Protocol.UDP)?( new UDPCommunicator(addressList) ):( new TCPCommunicator(addressList) );
		
		communicator.subscribeReceivedEventCallback(new ReceivedEventCallback() {
			@Override
			public void onReceivedEvent(Serializable event)
			{
				try {
					if(event instanceof SecureEventMessage)
					{
						event = secureEventSerializer.deserialize((SecureEventMessage) event);
					}
					eventQueue.add(event);
				} catch (IllegalArgumentException e) {
					;
				} catch (ClassCastException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InvalidClassException e) {
					e.printStackTrace();
				} catch (StreamCorruptedException e) {
					e.printStackTrace();
				} catch (OptionalDataException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Throwable e) { // Read object is known to be able to throw exceptions that are not documented!
					System.err.print("Cought undocumented exception:");
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
					{
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else
					{
						if(event instanceof SecureEvent)
						{
							SecureEvent secureEvent = (SecureEvent) event;
							handleEvent(secureEvent.getGroup(), secureEvent.getEvent());
						}
						else
							handleEvent(null, event);
					}
				}
			}
		};
		handlerThread.start();
		
		secureEventSerializer.setup();
	}

	public void fire(Serializable event)
	{
		fire(event, true);
	}

	public void fire(Serializable event, boolean intern)
	{
		Serializable sendEvent = event;
		if(sendEvent instanceof SecureEvent)
		{
			try {
				sendEvent = secureEventSerializer.serialize((SecureEvent) sendEvent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		eventSendQueue.add(sendEvent);
		
		if(intern)
			eventQueue.add(event);
	}
	
	public void postEvent(Serializable event)
	{
		postEvent(event, true);
	}
	
	public void postEvent(Serializable event, boolean intern)
	{
		eventSendQueue.add(event);
		
		if(intern)
			eventQueue.add(event);
	}
	
	public void postGroupEvent(String group, Serializable event)
	{
		postGroupEvent(group, event, true);
	}
	
	public void postGroupEvent(String group, Serializable event, boolean intern)
	{
		SecureEventMessage sendEvent = null;
		try {
			sendEvent = secureEventSerializer.serialize(group, event);
		} catch (IOException e) {
			e.printStackTrace();
		}
		eventSendQueue.add(sendEvent);
		
		if(intern)
			eventQueue.add(new SecureEvent(group, event));
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
				Class<?> parameter;
				if(parameters.length == 1)
				{
					parameter = parameters[0];
				}
				else if(parameters.length == 2)
				{
					if(!parameters[0].isAssignableFrom(String.class))
						throw new IllegalArgumentException("@Subscribe presented to method " + method.getName() + " with two paramters: parameter one has to be of final type String");
					parameter = parameters[1];
				}
				else
				{
					throw new IllegalArgumentException("@Subscribe presented to method " + method.getName() + " with an invalid amount of paramter.");
				}
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
	
	public void removeHandler(Object handler)
	{
		for( Entry<Class<?>, List<HandlerCallList>> entry : eventHandlerMethods.entrySet() )
		{
			for( HandlerCallList handlerCallList : entry.getValue() )
			{
				if(handlerCallList.obj.equals(handler))
					entry.getValue().remove(handlerCallList);
			}
		}
	}
	
	private void handleEventInterfaces(final Collection<MethodCaller> collection, int level, String group, Serializable event, Class<?> cls)
	{
		try {
			Class<?>[] types = cls.getInterfaces();
			
			for( Class<?> type : types )
			{
				List<HandlerCallList> list = eventHandlerMethods.get(type);

				if(list != null)
				{
					for( HandlerCallList handlerCallList : list )
					{
						handlerCallList.callInstanceOf(collection, level, group, event, type);
					}
				}

				handleEventInterfaces(collection, level + 1, group, event, type);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	private void handleEventInstanceOf(final Collection<MethodCaller> collection, int level, String group, Serializable event, Class<?> cls)
	{
		List<HandlerCallList> list = eventHandlerMethods.get(cls);

		if(list != null)
		{
			for( HandlerCallList handlerCallList : list )
			{
				handlerCallList.callInstanceOf(collection, level, group, event, cls);
			}
		}
		
		Class<?> superClass = cls.getSuperclass();
		if(superClass != null)
		{
			if(!(superClass instanceof Serializable))
			{
				handleEventInstanceOf(collection, level + 1, group, event, superClass);
			}
		}
		
		handleEventInterfaces(collection, level + 1, group, event, cls);
	}
	
	private void handleEvent(String group, Serializable event)
	{
		List<MethodCaller> collection = new ArrayList<MethodCaller>();
		
		Class<? extends Serializable> cls = event.getClass();
		
		Class<?> superClass = cls.getSuperclass();
		if(superClass != null)
			handleEventInstanceOf(collection, 1, group, event, superClass);
		
		handleEventInterfaces(collection, 1, group, event, cls);

		List<HandlerCallList> list = eventHandlerMethods.get(cls);

		if(list != null)
		{
			for( HandlerCallList handlerCallList : list )
			{
				handlerCallList.call(collection, group, event);
			}
		}
		
		Collections.sort(collection, new CustomComparator());
		
		for( MethodCaller item : collection )
			item.call(group, event);
	}
	
	public class CustomComparator implements Comparator<MethodCaller> {
	    @Override
	    public int compare(MethodCaller o1, MethodCaller o2)
	    {
	    	int comp = o1.getPriority().compareTo(o2.getPriority());
	    	
	    	if (comp == 0)
	    		return ((Integer)o1.getLevel()).compareTo(o2.getLevel());
	    	
	    	return comp;
	    }
	}
	
	public InetSocketAddress[] getAddressList() {
		return addressList;
	}
	
	public void close()
	{
		close(false);
	}
	
	public void close(boolean blocking)
	{
		senderThread.running = false;
		handlerThread.running = false;
		
		communicator.close(blocking);
		
		if(blocking)
		{
			try {
				senderThread.join();
			} catch (InterruptedException e1) { }
			try {
				handlerThread.join();
			} catch (InterruptedException e) { }
		}
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
		
		public void call(final Collection<MethodCaller> collection, String group, Object event)
		{
			for( Method method : methods )
			{
				collection.add(new MethodCaller(obj, method, 0, method.getAnnotation(Subscribe.class).priority(), event.getClass()));
			}
		}
		
		public void callInstanceOf(final Collection<MethodCaller> collection, int level, String group, Object event, Class<?> type)
		{
			for( Method method : methods )
			{
				if( method.getAnnotation(Subscribe.class).instanceOf() )
				{
					collection.add(new MethodCaller(obj, method, level, method.getAnnotation(Subscribe.class).priority(), type));
				}
			}
		}
	}
	
	class MethodCaller
	{
		private final Object obj;
		private final Method method;
		private final int level;
		private final Subscribe.Priority priority;
		private final Class<?> type;
		
		public MethodCaller(Object obj, Method method, int level, Subscribe.Priority priority, Class<?> type)
		{
			this.obj = obj;
			this.method = method;
			this.level = level;
			this.priority = priority;
			this.type = type;
		}

		/**
		 * @return the obj
		 */
		public Object getObj() {
			return obj;
		}

		/**
		 * @return the method
		 */
		public Method getMethod() {
			return method;
		}

		/**
		 * @return the level
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * @return the priority
		 */
		public Subscribe.Priority getPriority() {
			return priority;
		}
		
		/**
		 * @return the type
		 */
		public Class<?> getType()
		{
			return type;
		}
		
		public void call(String group, Serializable event)
		{
			try {
				if(method.getParameterTypes().length == 1)
					method.invoke(obj, type.cast(event));
				else
					method.invoke(obj, group, type.cast(event));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean addGroup(String group, String password)
	{
		return addGroup(group, password.toCharArray());
	}
	
	public boolean addGroup(String group, char[] password)
	{
		try {
			secureEventSerializer.addGroup(group, password);
			return true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean addGroup(String group, SecretKey groupKey)
	{
		secureEventSerializer.addGroup(group, groupKey);
		return true;
	}
	
	public boolean removeGroup(String group)
	{
		return secureEventSerializer.removeGroup(group);
	}
	
	public Set<String> getGroups()
	{
		return secureEventSerializer.getGroups();
	}

	public SecretKey getGroupKey(String groupname)
	{
		return secureEventSerializer.getGroupKey(groupname);
	}
	
}
