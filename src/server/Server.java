package server;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;


public class Server implements Runnable {
	//TODO: max num of concurrent server threads
	//TODO: only one connection per client
	//TODO: clean up synchronization, make sure we're atomic

	public static volatile AtomicInteger currentID = new AtomicInteger(1);//starting at one, because connections are numbered that way in main
	private int myID = 1;
	private byte[] buffer = new byte[4096];//buffer for reading
	private BufferedInputStream is;
	private BufferedOutputStream os;
	private ServerProtocol proto;
	private long lastReply = 0; //time of last received response.
	private Messager m;
	private Socket clisock;
	private boolean isRunning = true;
	private ServerManager parent;

	public Server(Socket clientSocket, ServerManager parent) {
		myID = currentID.getAndIncrement(); //thread-safe IDs!
		m = new Messager(myID);
		clisock = clientSocket;
		this.parent = parent;
		this.proto = new ServerProtocol(this);

		try {
			clientSocket.setSoTimeout(ServerManager.timeout);
		} catch (SocketException e) {
			m.println("SocketException", false);
			e.printStackTrace();
		}

		lastReply = (int) (System.currentTimeMillis()/1000);
		try {
			is = new BufferedInputStream(clientSocket.getInputStream());
			os = new BufferedOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			m.println("IOException", false);
			e.printStackTrace();
		}
		m.println("successfully created to handle connection to "+clientSocket.getInetAddress().getHostAddress()+".", false);
	}

	public void run() {
		if (isRunning) {
			//thread started, now we're just waiting for replies and responding.
			String myresponse = "!default!";
			try {
				while(isRunning && myresponse != "" && myresponse != null && !myresponse.isEmpty()) {
					//read
					myresponse = this.readResponse(is);
					if (myresponse == null) {
						break;//got null back from readResponse. prolly a timeout. close up shop.
					}

					//write
					this.send(os, myresponse);
					if (!myresponse.isEmpty()) {m.println("Tx: "+ myresponse, true);}
				}
				if ( send(os, proto.goodbye()) ) {m.println("Goodbye!", true);}
				this.close();
			} catch (IOException e) {
				m.println("IOException", false);
				e.printStackTrace();
			}
		}
	}

	//readResponse returns the response from protocol of whatever was sent
	private synchronized String readResponse(BufferedInputStream input) throws IOException {
		buffer = new byte[4096];
		m.println("Reading from socket.", true);
		try {//try for read timeout
			while(isRunning && input.read(buffer, 0, buffer.length) > 0) {
				//read socket, count here or something idk
				break;
			}
		} catch(SocketTimeoutException e) {
			//if we time out, then return null.
			String output;
			if (lastReply >= 0) {output = ((System.currentTimeMillis()-lastReply)/1000) +" seconds ago (max: "+(ServerManager.timeout/1000)+")";}
			else {output = "took longer than "+(ServerManager.timeout/1000)+" seconds!";}
			m.println("Timeout! Last reply "+output, false);
			return null;
		} catch(SocketException e) {
			m.println("Read got SocketException, probably due to close from main.", true);
		}
		lastReply = System.currentTimeMillis();
		String decoded = new String(buffer, "UTF-8").trim();
		m.println("Rx: "+decoded, true);
		return proto.handleTransaction(decoded);
	}

	private synchronized boolean send(BufferedOutputStream output, String out) throws IOException {
		if (isRunning) {
			try{
				output.write(out.getBytes(), 0, out.getBytes().length);
				output.flush();
			} catch (SocketException e) {
				m.println("Write got SocketException");
				return false;
			}
		} else if (out.equals(proto.goodbye())) {
			m.println("Tried to send a goodbye, but we're already closed.", true);
			return false;
		}
		return true;
	}

	public InetAddress getInetAddress() {
		return clisock.getInetAddress();
	}

	//tries to shutdown thread.
	public void close() throws IOException {
		if (isRunning) {
			isRunning = false;
			clisock.close();
			//Close all connections.
			synchronized(this){
				is.close();
				os.close();
			}
			parent.connectionClosed(this);
			m.println("Closed connection to client "+clisock.getInetAddress().getHostName()+"("+clisock.getInetAddress()+")", false);
		}
	}

	public int getID() {
		return myID;
	}

	//hashCode helper function
	private int fromByteArray(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getInt();
	}

	public synchronized int hashCode() {
		return 17+(is.hashCode()+os.hashCode())*31;
		//not guaranteed to be unique. include count?
	}
}