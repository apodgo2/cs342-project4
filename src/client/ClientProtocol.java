package client;

import server.SharedProtocol;

//This class reperesents a conversation that's going on between client and server.
//It keeps track of states like the number of messages sent and received, for ex.
//And it uses these metrics to determine what to send next.

public class ClientProtocol extends SharedProtocol {
	
	String username;
	Client parent;
	
	public ClientProtocol(String username, Client parent) {
		if (username.equals("") || username.isEmpty()) {this.username = "!default!";}
		this.username = username;
		this.parent = parent;
	}
	
	//RX FORMATS
	@Override
	public String handleUpdate(String message) {
		//TODO: display this message in the message window as a specially formatted message (e.g., italics, grey text, bold or something, be creative)
		parent.getParent().updateClientList(getClients(message));//update client list, JIC the update is disconnect/connect
		return null;
	}
	@Override
	public String handleListusers(String message) {
		//uses the usernames included after the : and seperated by , to build a list of users, and display them on GUI window
		parent.getParent().updateClientList(getClients(message));
		return null;
	}
	//TX FORMATS
	@Override
	public String goodbye() {
		return goodbyePrefix+"-"+username+":Goodbye!";
	}
	@Override
	public String heartbeat() {
		return heartbeatPrefix+":Heartbeat!";
	}
}