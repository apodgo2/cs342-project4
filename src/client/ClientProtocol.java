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
		parent.getParent().updateStatus("SERVER: "+message.substring(message.indexOf(':')));
		parent.getParent().updateClientList(getClients(message));//update client list, JIC the update is disconnect/connect
		return null;
	}
	@Override
	public String handleListusers(String message) {
		//uses the usernames included after the : and seperated by , to build a list of users, and display them on GUI window
		parent.getParent().updateClientList(getClients(message));
		return null;
	}
	@Override
	public String handleHeartbeat(String message) {
		//don't do anything, we don't really care about heartbeats as a client
		//what would be cool, is if we tracked the server's ping, or had a lil indicator on the GUI showing that the server is connected if lastheartbeat<10s ago
		return null;
	}
	@Override
	public String handleGoodbye(String message) {
		parent.getParent().updateStatus("Server disconnected.");
		parent.disconnect();
		return null;
	}
	@Override
	public String handleMessage(String message) {
		parent.getParent().updateStatus(getUsername(message)+": "+message.substring(message.indexOf(':')));
		return null;
	}
	//TX FORMATS
	@Override
	public String goodbye() {
		if (debug) {System.out.println("TX: "+goodbyePrefix+"-"+username+":Goodbye!");}
		return goodbyePrefix+"-"+username+":Goodbye!";
	}
	@Override
	public String heartbeat() {
		if (debug) {System.out.println("TX: "+heartbeatPrefix+":Heartbeat!");}
		return heartbeatPrefix+":Heartbeat!";
	}
	@Override
	public String pm(String message, String[] recipients) {
		i++;
		//format: prefix-usernameofsender+listofrecipients:message
		if (debug) {System.out.println("TX: "+pmPrefix+"-"+username+arrayOfUsersToString(recipients)+message);}
		return pmPrefix+"-"+username+arrayOfUsersToString(recipients)+message;
	}
}