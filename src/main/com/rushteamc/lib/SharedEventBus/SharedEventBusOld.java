package com.rushteamc.lib.SharedEventBus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public class SharedEventBusOld
{
	private final InetAddress host;
	private final int port;
	private final Protocol protocol;
	
	private final List<Object> handlers = new LinkedList<Object>();
	private final Map<Class<?>, List<HandlerCallList>> eventHandlerMethods = new HashMap<Class<?>, List<HandlerCallList>>();
	
	private BusConnection busConnection;
	
	public enum Protocol
	{
		TCP,
		UDP
	}
	
	public SharedEventBusOld(int port) throws UnknownHostException
	{
		this("localhost", port, Protocol.UDP);
	}
	public SharedEventBusOld(String host, int port, Protocol protocol) throws UnknownHostException
	{
		this.host = InetAddress.getByName(host);
		this.port = port;
		this.protocol = protocol;
		
		try {
			busConnection = new UDPBusConnection(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
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
		Method[] dummyArray = null;
		while( it.hasNext() )
		{
			Entry<Class<?>, List<Method>> entry = it.next();
			Class<?> type = entry.getKey();
			List<HandlerCallList> list = eventHandlerMethods.get(type);
			if(list == null)
				list = new CopyOnWriteArrayList<HandlerCallList>();
			list.add(new HandlerCallList(handler, entry.getValue().toArray(dummyArray)));
			eventHandlerMethods.put(type, list);
		}
	}
	
	private void handleEvent(Object event)
	{
		List<HandlerCallList> list = eventHandlerMethods.get(event.getClass());
		for( HandlerCallList handlerCallList : list )
		{
			handlerCallList.call(event);
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
	
	public void fire(Serializable event)
	{
		busConnection.sendEvent(event);
	}
	
	private interface BusConnection
	{
		public void sendEvent(Serializable event);
	}
	
	private class UDPBusConnection implements BusConnection
	{
		private final DatagramSocket socket;
		private final UDPBusConnectionReceiver receiver;
		
		public UDPBusConnection(int port) throws SocketException
		{
			socket = new DatagramSocket(port);
			receiver = new UDPBusConnectionReceiver();
			receiver.start();
		}
		
		@Override
		public void sendEvent(Serializable event)
		{
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(bos);
		        out.writeUnshared(event);
		        byte[] buf = bos.toByteArray();
		        
		        long key = ((long)event.hashCode()) ^ (new Date()).getTime();
		        int length = -((-buf.length) / UDPBusConnectionDatagramPacket.dataSize);
		        
		        for(int i = 0; i < length - 1; i++)
		        {
		        	int offset = UDPBusConnectionDatagramPacket.dataSize * i;
		        	int end = UDPBusConnectionDatagramPacket.dataSize * (i+1);
		        	if(end > buf.length)
		        		end = buf.length;
		        	
		        	byte[] data = new byte[end - offset];
		        	for(int b = offset; b < end; b++)
		        	{
		        		data[b - offset] = buf[b];
		        	}
		        	
					DatagramPacket packet = new UDPBusConnectionDatagramPacket(i, buf.length, key, data).getPacket();
					try {
						socket.send(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }

				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private class UDPBusConnectionReceiver extends Thread
		{
			private boolean running = true;
			private final List<UDPBusConnectionPacket> packetCollectorList = new LinkedList<UDPBusConnectionPacket>();
			
			public void run()
			{
				while(running)
				{
					byte[] buf = new byte[UDPBusConnectionDatagramPacket.packetSize];
					DatagramPacket p = new DatagramPacket(buf, buf.length);
					
					try {
						socket.receive(p);
						UDPBusConnectionDatagramPacket packet = new UDPBusConnectionDatagramPacket(p);
						
						boolean found = false;
						for( UDPBusConnectionPacket packetCollector : packetCollectorList )
						{
							if(packetCollector.feedPacket(packet))
							{
								found = true;
								break;
							}
						}
						
						if(!found)
							packetCollectorList.add(new UDPBusConnectionPacket(packet));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			
			public void terminate()
			{
				running = false;
			}
		}
		
		private class UDPBusConnectionPacket
		{
			private final byte[] data;
			private final long key;
			private int remaining;
			
			public UDPBusConnectionPacket(UDPBusConnectionDatagramPacket packet) throws ClassNotFoundException, IOException
			{
				this.key = packet.getKey();
				this.data = new byte[packet.getSize()];
				this.remaining = -((-packet.getSize()) / UDPBusConnectionDatagramPacket.dataSize) - 1;
				readData(packet);
				if(remaining == 0)
					fire();
			}
			
			public boolean feedPacket(UDPBusConnectionDatagramPacket packet) throws ClassNotFoundException, IOException
			{
				
				if(key == packet.getKey())
				{
					readData(packet);
					
					remaining--;
					if(remaining == 0)
						fire();
					
					return true;
				}
				return false;
			}
			
			private void readData(UDPBusConnectionDatagramPacket packet)
			{
				int offset = packet.getFolowup() * packet.dataSize;
				int max = (packet.getFolowup() + 1) * packet.dataSize;
				if(max > packet.getSize())
					max = packet.getSize();
				
				for(int i = offset; i < max; i++)
				{
					this.data[i] = packet.getData()[i - offset];
				}
			}
			
			private void fire() throws IOException, ClassNotFoundException
			{
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				ObjectInputStream in = new ObjectInputStream(bis);
				
				Object event = in.readObject();
				
				handleEvent(event);
			}
		}
		
		public class UDPBusConnectionDatagramPacket
		{
			public final static int packetSize = 1024;
			public final static int dataSize = packetSize - 4 - 4 - 8;
			
			private final int folowup;
			private final int size;
			private final long key;
			private final byte[] data;
			
			public UDPBusConnectionDatagramPacket(DatagramPacket p) throws IllegalArgumentException, IOException
			{
				byte[] data = p.getData();
				
				if(data.length != packetSize)
					throw new IllegalArgumentException("");
				
				this.data = new byte[dataSize];
				
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				DataInputStream dis = new DataInputStream(bis);
				
				folowup = dis.readInt();
				size = dis.readInt();
				key = dis.readLong();
				dis.read(this.data);
				
				bis.close();
				dis.close();
			}
			
			public UDPBusConnectionDatagramPacket(int folowup, int size, long key, byte[] data)
			{
				this.data = data;
				this.folowup = folowup;
				this.size = size;
				this.key = key;
			}
			
			public byte[] getData()
			{
				return data;
			}
			
			public int getFolowup()
			{
				return folowup;
			}

			public int getSize()
			{
				return size;
			}
			
			public long getKey()
			{
				return key;
			}
			
			public DatagramPacket getPacket() throws IOException
			{
		        ByteArrayOutputStream bos = new ByteArrayOutputStream();
		        DataOutputStream dos = new DataOutputStream(bos);
		        dos.writeInt(folowup);
		        dos.writeInt(size);
		        dos.writeLong(key);
		        dos.write(data);
		        
		        byte[] buf = bos.toByteArray();
		        
				DatagramPacket packet = new DatagramPacket(buf, buf.length, host, port);

				dos.close();
				bos.close();
				
				return packet;
			}
		}
	}
}
