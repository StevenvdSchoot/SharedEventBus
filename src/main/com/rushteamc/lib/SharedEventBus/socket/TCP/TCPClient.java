package com.rushteamc.lib.SharedEventBus.socket.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class TCPClient extends Thread
{
	private final Socket socket;
	private final ObjectOutputStream outputStream;
	private final Set<TCPClientDisconnect> TCPClientDisconnectHandlers = new HashSet<TCPClientDisconnect>();
	private final Set<TCPEvent> TCPEventHandlers = new HashSet<TCPEvent>();
	private boolean running = true;
	
	public TCPClient(InetSocketAddress address) throws IOException
	{
		this.socket = new Socket();
		socket.connect(address);
		outputStream = new ObjectOutputStream(socket.getOutputStream());
		
		// TODO: Detect if server actually is SharedEventBus server.
		
		start();
		System.out.println("Opened client.");
	}

	public TCPClient(Socket socket) throws IOException
	{
		this.socket = socket;
		outputStream = new ObjectOutputStream(socket.getOutputStream());
		start();
		System.out.println("Opened client.");
	}
	
	public void addTCPClientDisconnectHandler(TCPClientDisconnect handler)
	{
		TCPClientDisconnectHandlers.add(handler);
	}

	// Should only be called from this thread to avoid unintended blocking!
	private void callTCPClientDisconnectHandlers()
	{
		for( TCPClientDisconnect TCPClientDisconnectHandler : TCPClientDisconnectHandlers )
		{
			TCPClientDisconnectHandler.onTCPClientDisconnect();
		}
	}
	
	public void addTCPEventHandler(TCPEvent handler)
	{
		TCPEventHandlers.add(handler);
	}

	// Should only be called from this thread to avoid unintended blocking!
	private void callTCPEventHandlers(Serializable event)
	{
		for( TCPEvent TCPEventHandler : TCPEventHandlers )
			TCPEventHandler.onTCPEvent(event);
	}

	// Should only be called from this thread to avoid unintended blocking!
	private void closeSocket()
	{
		if(running)
		{
			running = false;
			callTCPClientDisconnectHandlers();
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Closed client.");
		}
	}
	
	public void run()
	{
		ObjectInputStream inputStream;
		try {
			inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
			closeSocket();
			return;
		}
		
		while(running)
		{
			Object obj;
			try {
				obj = inputStream.readObject();
				if(!(obj instanceof Serializable))
				{
					// WTH happend here...
					System.err.println("Received non serializable object!");
					continue;
				}
				Serializable event = (Serializable)obj;
				callTCPEventHandlers(event);
			} catch (IOException e) {
				// TODO: Better handle this event. It may indicate a broken connection. Try reconnect etc..
				closeSocket();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		closeSocket();
	}
	
	public void expire()
	{
		running = false;
	}
	
	public void sendEvent(Serializable event)
	{
		try {
			outputStream.writeObject(event);
		} catch (IOException e) {
			// TODO: Better handle this event. It may indicate a broken connection.
			e.printStackTrace();
		}
	}
	
	public InetSocketAddress getInetSocketAddress()
	{
		return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
	}
	
	public InetAddress getInetAddress()
	{
		return socket.getInetAddress();
	}
	
	public int getPort()
	{
		return socket.getPort();
	}
}
