package com.rushteamc.lib.SharedEventBus.socket.TCP;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rushteamc.lib.SharedEventBus.SharedEventBus;

public class TCPServer extends Thread
{
	private final ServerSocket socket;
	private boolean running = true;
	private final List<TCPClient> clientList = new LinkedList<TCPClient>();
	private final Set<TCPEvent> TCPEventHandlers = new HashSet<TCPEvent>();
	
	public TCPServer(ServerSocket socket)
	{
		this.socket = socket;
		start();
	}
	
	public void addTCPEventHandler(TCPEvent handler)
	{
		TCPEventHandlers.add(handler);
	}
	
	private void callTCPEventHandlers(Serializable event)
	{
		for( TCPEvent TCPEventHandler : TCPEventHandlers )
			TCPEventHandler.onTCPEvent(event);
	}
	
	public void run()
	{
		while(running)
		{
			try {
				Socket clientSocket = socket.accept();
				System.out.println("new server client!");
				final TCPClient client = new TCPClient(clientSocket);
				client.addTCPClientDisconnectHandler(new TCPClientDisconnect(){
					@Override
					public void onTCPClientDisconnect()
					{
						clientList.remove(client);
					}
				});
				client.addTCPEventHandler(new TCPEvent() {
					@Override
					public void onTCPEvent(Serializable event)
					{
						callTCPEventHandlers(event);
						for( TCPClient tcpclient : clientList )
							if( tcpclient != client )
								tcpclient.sendEvent(event);
					}
				});
				clientList.add(client);
			} catch (IOException e) {
				e.printStackTrace();
				try {
					sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void expire()
	{
		running = false;
	}

	public void sendEvent(Serializable event)
	{
		for( TCPClient client : clientList )
			client.sendEvent(event);
	}
}
