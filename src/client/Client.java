package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.Timer;

import server.SharedProtocol;

public class Client implements Runnable {

	private byte[] buffer = new byte[4096];
	private NetworkedChatClient parent;
	private BufferedInputStream is;
	private BufferedOutputStream os;
	private int count = 0;
	private Calendar cal = Calendar.getInstance();
	private ClientProtocol proto;
	private boolean isConnected = false;
	private String username;
	private Timer heartbeatTimer;

	public Client(Socket sock, String username, NetworkedChatClient parent) {
		this.parent = parent;
		this.username = username;
		try {
			is = new BufferedInputStream(sock.getInputStream());
			os = new BufferedOutputStream(sock.getOutputStream());
			parent.updateStatus("New Client "+this.hashCode()+" successfully created.");
			isConnected = true;
		} catch (IOException ex) {
			parent.updateStatus("IOException in "+this.hashCode()+", Client unable to connect.");
			ex.printStackTrace();
			isConnected = false;
		}
		proto = new ClientProtocol(username, this);
		if (isConnected) {//say hello!
			send(os, proto.hello(username));
			//start heartbeat timer: we have to heartbeat at least once every 10 seconds or connection will timeout
			heartbeatTimer = new Timer(5000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					send(os, proto.heartbeat());
				}
			});
			heartbeatTimer.start();
		}
	}
	
	@Override
	public void run() {
		if (isConnected) {
			String serverMsg = "";
			while(serverMsg != "" && serverMsg != null && !serverMsg.isEmpty()) {
				serverMsg = this.listen(is);
				proto.handleTransaction(serverMsg);
			}
		}
	}

	private void sendMessage(String textMessage) {
		count++;
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		if (parent.getClientsToMessage().length == 0) {//send to everybody
			this.send(os, (String) proto.message("["+dateFormat.format(cal.getTime())+"] "+textMessage, username));
			parent.updateStatus(username+": "+"["+dateFormat.format(cal.getTime())+"] "+textMessage);
		} else {//send a PM
			this.send(os, (String) proto.pm("["+dateFormat.format(cal.getTime())+"] "+textMessage, parent.getClientsToMessage()));
			parent.updateStatus(username+": PM("+SharedProtocol.arrayOfUsersToString(parent.getClientsToMessage())+") ["+dateFormat.format(cal.getTime())+"] "+textMessage);
		}
	}

	private void send(BufferedOutputStream output, String out) {
		try {
			output.write(out.getBytes(), 0, out.getBytes().length);
			output.flush();
			System.out.println("Sent.");
		} catch (IOException e) {
			System.out.println("Error while sending a message.");
		}
	}

	private String listen(BufferedInputStream input) {
		buffer = new byte[4096];
		try {
			while (input.read(buffer, 0, buffer.length) > 0) {
				//count or something, don't really need to do anything as we read yet
				break;
			}
			return new String(buffer, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public int getCount() {
		return count;
	}
	public NetworkedChatClient getParent() {
		return parent;
	}

	public void disconnect() {
		this.isConnected = false;
		send(os, proto.goodbye());
		heartbeatTimer.stop();
		try {
			os.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}