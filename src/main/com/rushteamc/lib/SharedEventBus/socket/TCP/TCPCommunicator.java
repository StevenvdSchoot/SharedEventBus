package com.rushteamc.lib.SharedEventBus.socket.TCP;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

import com.rushteamc.lib.SharedEventBus.socket.Communicator;
import com.rushteamc.lib.SharedEventBus.socket.ReceivedEventCallback;

public class TCPCommunicator implements Communicator
{
	private final Set<ReceivedEventCallback> receivedEventCallbackList = new HashSet<ReceivedEventCallback>();
	private final InetSocketAddress[] addressList;
	private TCPServer server;
	private TCPClient client;
	
	private boolean connected = false;
	
	public TCPCommunicator(InetSocketAddress[] addressList)
	{
		this.addressList = addressList;
		connect();
	}
	
	private void connect()
	{
		int localhostIndex = -1;
		for(int i = 0; i < addressList.length; i++)
		{
			if(addressList[i].getAddress().isLoopbackAddress())
			{
				if(localhostIndex != -1)
					throw new IllegalArgumentException("Cannot have multiple localhost addresses");
				localhostIndex = i;
			}
		}
		
		if(localhostIndex != -1)
		{
			try {
				ServerSocket socket = new ServerSocket(addressList[localhostIndex].getPort());
				server = new TCPServer(socket);
				server.addTCPEventHandler(new TCPEvent() {
					@Override
					public void onTCPEvent(Serializable event)
					{
						publishEvent(event);
					}
				});
			} catch (IOException e) {
				localhostIndex = -1;
			}
		}
		
		for(int i = 0; i < addressList.length; i++)
		{
			if(i == localhostIndex)
				continue;
			
			try {
				newClient(addressList[i]);
				break;
			} catch (IOException e) { }
		}
	}
	
	private void newClient(InetSocketAddress address) throws IOException
	{
		client = new TCPClient(address);
		client.addTCPEventHandler(new TCPEvent() {
			@Override
			public void onTCPEvent(Serializable event)
			{
				if(server != null)
				{
					System.out.println("Sending event to server...");
					server.sendEvent(event);
				}
				publishEvent(event);
			}
		});
		
		if(client.getInetAddress().isLoopbackAddress())
			client.addTCPClientDisconnectHandler(new TCPClientDisconnect() {
				@Override
				public void onTCPClientDisconnect()
				{
					try {
						ServerSocket socket = new ServerSocket(client.getPort());
						server = new TCPServer(socket);
						server.addTCPEventHandler(new TCPEvent() {
							@Override
							public void onTCPEvent(Serializable event)
							{
								publishEvent(event);
							}
						});
					} catch (IOException e) {
						try {
							newClient(client.getInetSocketAddress());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		else
			client.addTCPClientDisconnectHandler(new TCPClientDisconnect() {
				@Override
				public void onTCPClientDisconnect()
				{
					try {
						Thread.sleep(200); // Give the server some time to reestablish.
					} catch (InterruptedException e1) { } 
					try {
						newClient(client.getInetSocketAddress());
					} catch (IOException e) { }
				}
			});
	}

	@Override
	public boolean isConnected()
	{
		return connected;
	}

	@Override
	public void subscribeReceivedEventCallback(ReceivedEventCallback callback)
	{
		receivedEventCallbackList.add(callback);
	}
	
	protected void publishEvent(Serializable event)
	{
		for( ReceivedEventCallback receivedEventCallback : receivedEventCallbackList )
			receivedEventCallback.onReceivedEvent(event);
	}

	@Override
	public void shareEvent(Serializable event)
	{
		if(server != null)
			server.sendEvent(event);
		
		if( client != null )
			client.sendEvent(event);
	}

	@Override
	public void close()
	{
		close(false);
	}

	@Override
	public void close(boolean blocking)
	{
		if(client != null)
		{
			client.close(true);
			client = null;
		}
		
		if(server != null)
		{
			server.close(true);
			server = null;
		}
	}
}
