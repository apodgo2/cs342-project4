package client;

import server.SharedProtocol;

//This class reperesents a conversation that's going on between client and server.
//It keeps track of states like the number of messages sent and received, for ex.
//And it uses these metrics to determine what to send next.

public class ClientProtocol extends SharedProtocol {
	
	String username;
	
	public ClientProtocol(String username) {
		if (username.equals("") || username.isEmpty()) {this.username = "!default!";}
		this.username = username;
	}
	
	//RX FORMATS
	@Override
	public String handleUpdate(String message) {
		//TODO: display this message in the message window as a specially formatted message (e.g., italics, grey text, bold or something, be creative)
		return null;
	}
	@Override
	public String handleListusers(String message) {
		//TODO: use the usernames included after the : and seperated by , to build a list of users, and display them on GUI window
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