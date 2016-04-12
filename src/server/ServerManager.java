package server;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.*;

import java.net.*;
import java.io.*;

public class ServerManager implements Runnable {

	private ConcurrentHashMap<Integer, Server> serverList = new ConcurrentHashMap<Integer, Server>();
	private int hashmapCounter = 0;
	private Messager m = new Messager(true);
	private int connections = 0;
	private boolean serverRunning = false;
	private String address;
	private int port;
	private ServerSocket sock;
	private static NetworkedChatServer parent;
	protected final static boolean debug = false;
	protected final static int timeout = 10000;//kill connection if no response in over 10 seconds

	public ServerManager(String address, int port, NetworkedChatServer parent) {
		this.address = address;
		this.port = port;
		ServerManager.parent = parent;
	}
	
	//This is how you start the server to listen to incoming connections.
	@Override
	public void run() {
		ServerSocketFactory ssf = ServerSocketFactory.getDefault();
		m.println("Running in server mode.");
		m.println("Bound to address "+address+"...");
		m.println("Using port "+port+"...");
		
		try {
			if (address == "" || address == null || address.isEmpty()) {
				sock = ssf.createServerSocket(port);
			} else {
				InetAddress ipAddress = InetAddress.getByName(address);
				sock = ssf.createServerSocket(port, 0, ipAddress);
			}
			this.serverRunning = true;
			while (serverRunning) {
				m.println("Main thread ready for incoming connections...");
				Socket clientSock = null;
				try {
					clientSock = sock.accept();
				} catch (SocketException e) {
					m.println("NOTIFY: Main server thread attempted to listen on a closed port, probably closed by user.");
					break;
				}
				InetAddress user = clientSock.getInetAddress();
				connections++;
				m.println("Got connection #"+connections+" from "+user.getHostName()+"("+user.getHostAddress()+"). Creating handler.");
				Server a = new Server(clientSock, this);
				serverList.putIfAbsent(hashmapCounter, a);//TODO: what happens on int rollover? too many servers.... prolly not an issue
				hashmapCounter++;
				new Thread(a).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//This method is called when a connection closes, to register the close event.
	//this should only be called in Server.close()
	public void connectionClosed(Server itself) {
		//TODO: make this more efficient, removing entry by value
		for (Entry<Integer, Server> x : serverList.entrySet()) {
			if (x.getValue() == itself) {serverList.remove(x.getKey());}
		}
		connections--;
		m.println("Got a connection closed message. Number of connections is now: "+connections, true);
	}

	//called by user input, shuts down server.
	public void shutdownServers() {
		if (serverRunning) {
			m.println("CLOSING ALL CONNECTIONS!");
			if (!serverList.isEmpty()) {
				for (Entry<Integer, Server> x : serverList.entrySet()) {
					try {
						x.getValue().close();
					} catch (IOException e) {
						m.println("IOException shutting down servers.");
						e.printStackTrace();
					}
				}
			}
			serverRunning = false;
			m.println("Goodbye World!");
			try {
				sock.close();
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			m.println("COMMAND EXECUTION ERROR: Server already shutdown.");
		}
	}

	public int serverListSize() {
		return serverList.size();
	}

	//kills a connection
	public void closeServer(int index) {
		try {
			Server killme = serverList.get(index);
			killme.close();
			m.println("Closed server at index "+index+", client "+killme.getInetAddress().getHostName()+"("+killme.getInetAddress().getHostAddress()+")");
		} catch (IOException e) {
			m.println("IOException");
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			m.println("Index not in server list!");
		}
	}

	//lists all currently running connections
	public void listServers() {
		m.println("Currently Running Server Threads:");
		for (Entry<Integer, Server> x : serverList.entrySet()) {
			m.println(x.getKey()+":\tSH#"+x.getValue().getID()+"\t"+x.getValue().getInetAddress().getHostName()+"\t"+x.getValue().getInetAddress().getHostAddress());
		}
	}
	public ConcurrentHashMap<Integer, Server> getServerList() {
		return serverList;
	}
	
	public boolean isRunning() {
		return serverRunning;
	}
	
	public static NetworkedChatServer getParent() {
		return parent;
	}
}