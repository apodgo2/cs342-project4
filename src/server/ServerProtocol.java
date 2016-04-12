package server;

import java.util.List;
import java.util.Map.Entry;

//This class reperesents a conversation that's going on between client and server.
//It keeps track of states like the number of messages sent and received, for ex.
//And it uses these metrics to determine what to send next.

/*
 * THE ONLY THING this class does is format messages, and respond to message events.
 * If you want to send a String, you pass it to this protocol,
 * it returns a network-formatted string for you to push down the pipe
 */

public class ServerProtocol extends SharedProtocol {
	
	private Server parent;
	
	public ServerProtocol(Server parent) {
		this.parent = parent;
	}
	
	//RX FORMATS
	@Override
	public String handleMessage(String message) {
		//TODO: have this forward messages to every client, use the sendMessage() in Server, see handlePm for example
		//return a heartbeat
		parent.getMessager().println(message);
		for (Entry<Integer, Server> entry : parent.getParent().getServerList().entrySet()) {
			if (entry.getValue().hashCode() != parent.hashCode()) {//as long as it isn't our parent
					entry.getValue().forwardMessage(message.substring(message.indexOf(':')), getUsername(message));
			}
		}
		return heartbeat();
	}
	@Override
	public String handleGoodbye(String message) {
		//TODO: client disconnected, get username from message, tell ServerManager to close that connection if not already closed. See handleHello()
		//return null so that the Server breaks off the connection
		parent.setUsername(getUsername(message));
		for (Entry<Integer, Server> entry : parent.getParent().getServerList().entrySet()) {
			if (entry.getValue().hashCode() != parent.hashCode()) {//as long as it isn't our parent
				entry.getValue().forwardUpdate(getUsername(message)+" has disconnected.");
			}
		}
		parent.getMessager().println("Goodbye!");
		return null;
	}
	@Override
	public String handleHeartbeat(String message) {
		//this updates a variable in server showing last time the server received a heartbeat (use lastReply)
		//return a heartbeat
		parent.updateLastReply();
		parent.getMessager().println("heartbeat!");
		return heartbeat();
	}
	@Override
	public String handleHello(String message) {
		//this updates a username variable in server with the client username, and tells other clients of connection
		//return a list of users
		parent.setUsername(getUsername(message));
		for (Entry<Integer, Server> entry : parent.getParent().getServerList().entrySet()) {
			parent.getMessager().println(getUsername(message)+" has connected.");
			if (entry.getValue().hashCode() != parent.hashCode()) {//as long as it isn't our parent
				entry.getValue().forwardUpdate(getUsername(message)+" has connected.");//this tells the client the users connected, and forces them to update their client list
				
			}
		}
		return listusers();
	}
	@Override
	public String handlePM(String message) {
		//this forwards a PM to it's intended recipients
		List<String> recipients = getRecipients(message);
		for (Entry<Integer, Server> entry : parent.getParent().getServerList().entrySet()) {
			if (entry.getValue().hashCode() != parent.hashCode()) {//as long as it isn't our parent
				if (recipients.contains(entry.getValue().getUsername())) {//if that Server is on the recipient list
					entry.getValue().forwardMessage(message.substring(message.indexOf(':')), getUsername(message));
				}
			}
		}
		return heartbeat();
	}
	//TX FORMATS
	@Override
	public String heartbeat() {
		if (debug) {System.out.println("TX: "+"heartbeat");}
		return heartbeatPrefix+":Heartbeat!";
	}
	@Override
	public String goodbye() {
		if (debug) {System.out.println("TX: "+"goodbye");}
		return goodbyePrefix+":Goodbye!";
	}
	@Override
	public String listusers() {
		//iterate through all servers in ServerManager, get their usernames and make a comma seperated list
		String output = listusersPrefix+":";
		for (Entry<Integer, Server> entry : parent.getParent().getServerList().entrySet()) {
			if (entry.getValue().getUsername() != null && entry.getValue().getUsername() != "null") {
				output+= entry.getValue().getUsername()+",";
				System.out.println(output);
			}
		}
		if (debug) {System.out.println("TX: "+output.substring(0,output.length()-1));}
		return output.substring(0,output.length()-1);//ommit last comma.
	}
}
