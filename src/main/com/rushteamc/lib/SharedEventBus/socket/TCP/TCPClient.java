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
	}

	public TCPClient(Socket socket) throws IOException
	{
		this.socket = socket;
		outputStream = new ObjectOutputStream(socket.getOutputStream());
		start();
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
				closeSocket();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (Throwable e) { // Read object is known to be able to throw exceptions that are not documented (e.g. IllegalStateException)
				System.err.print("Cought undocumented exception:");
				e.printStackTrace();
			}
		}
		
		closeSocket();
	}
	
	public void sendEvent(Serializable event)
	{
		if(!running)
			return;
		
		try {
			outputStream.writeObject(event);
		} catch (IOException e) {
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
	
	public void close()
	{
		close(false);
	}
	
	public void close(boolean blocking)
	{
		running = false;
		try {
			socket.close();
		} catch (IOException e) { }
		
		if(blocking)
			try {
				join();
			} catch (InterruptedException e) { }
	}
}
