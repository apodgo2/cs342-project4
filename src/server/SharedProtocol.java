package server;

import java.util.Arrays;
import java.util.List;

/*
 * THE ONLY THING this class does is format messages, and respond to message events.
 * If you want to send a String, you pass it to this protocol,
 * it returns a network-formatted string for you to push down the pipe
 */


public class SharedProtocol {
	public static final String goodbyePrefix = "X";//goodbye's sent from client or server signify an intentional disconnect
	public static final String messagePrefix = "M";//messages sent from server or client are messages between users
	public static final String updatePrefix = "U";//updates are sent from server, notifying clients of user disconnects or other server stuff
	public static final String listusersPrefix = "L";//this is a list of users sent from the server to populate the user list
	public static final String heartbeatPrefix = "H";//TODO: implement heartbeat
	public static final String helloPrefix = "C";//TODO: Implement hello message so Server's can know their client's username.
	public static final String pmPrefix = "P"; //private messages are messages with a paramater that specifies who they should go to
	
	protected int i = 0;
	
	//This function handles any transaction received by either party by calling the appropriate response handler
	//DO NOT OVERRIDE this function, conform to it's format please
	public String handleTransaction(String message) {
		i++;
		if (message == "" || message == null || message.isEmpty()) {
			return null;
		}
		if (getPrefix(message).equals(goodbyePrefix)) {
			return handleGoodbye(message);
		}
		if (getPrefix(message).equals(messagePrefix)) {
			return handleMessage(message);
		}
		if (getPrefix(message).equals(updatePrefix)) {
			return handleUpdate(message);
		}
		if (getPrefix(message).equals(listusersPrefix)) {
			return handleListusers(message);
		}
		if (getPrefix(message).equals(heartbeatPrefix)) {
			return handleHeartbeat(message);
		}
		if (getPrefix(message).equals(helloPrefix)) {
			return handleHello(message);
		}
		if (getPrefix(message).equals(pmPrefix)) {
			return handlePM(message);
		}
		return null;
	}
	
	//RX FORMATS (override these)
	public String handleGoodbye(String message) {
		return null;
	}
	public String handleMessage(String message) {
		return null;
	}
	public String handleUpdate(String message) {
		return null;
	}
	public String handleListusers(String message) {
		return null;
	}
	public String handleHeartbeat(String message) {
		return heartbeat();
	}
	public String handleHello(String message) {
		return null;
	}
	public String handlePM(String message) {
		return null;
	}
	//TX FORMATS
	public String heartbeat() {
		//format: prefix:Heartbeat!
		return heartbeatPrefix+":Heartbeat!";
	}
	public String goodbye() {
		//format: prefix:Goodbye!
		return goodbyePrefix+":Goodbye!";
	}
	public String message(String input, String username) {
		//format: prefix-usernameofsender:message
		i++;
		if (input == "" || input == null || input.isEmpty()) {
			return null;
		}
		return messagePrefix+"-"+username+":"+input;
	}
	public String update(String input) {
		//format: prefix:message
		i++;
		if (input == "" || input == null || input.isEmpty()) {
			return null;
		}
		return updatePrefix+":"+input;
	}
	//override me
	public String listusers() {
		i++;
		//format: prefix:listofconnectedclients
		return listusersPrefix+":";
	}
	//override me
	public String pm(String input, String[] recipients) {
		i++;
		//format: prefix-usernameofsender+listofrecipients:message
		return pmPrefix+"-!default!+!default!:";
	}
	public String hello(String username) {
		//format: prefix-usernameofsender:Hello!
		i++;
		return helloPrefix+"-"+username+":Hello!";
	}
	
	//PARSE RESPONSES
	public String getUsername(String message) {
		if (!(getPrefix(message).equals(messagePrefix) || getPrefix(message).equals(goodbyePrefix) || getPrefix(message).equals(helloPrefix))) {
			//origin username only matters for messages and goodbyes from clients, not stuff sent from the server program.
			return message.substring(2, message.indexOf(':'));
		}
		return null;
	}
	public String getPrefix(String message) {
		return new String(message.charAt(0)+"");
	}
	//gets the clients from a listupdate
	public String[] getClients(String message) {
		if (!getPrefix(message).equals(listusersPrefix)) {
			return null;
		}
		
		//return a List of elements
		List<String> items = Arrays.asList(message.substring(message.indexOf(':')).split("\\s*,\\s*"));
		return (String[]) items.toArray();
	}
	//gets the intended recipients of a PM
	public List<String> getRecipients(String message) {
		if (!getPrefix(message).equals(pmPrefix)) {
			return null;
		}
		
		List<String> items = Arrays.asList(message.substring(message.indexOf('+'), message.indexOf(':')).split("\\s*,\\s*"));
		return items;
	}
	
	public int getMessageCount() {
		return i;
	}
	
	//helperfunction:
	public static String arrayOfUsersToString(String[] arrayOfUsers) {
		//for example, with the getClientsToMessage() function in NetworkedChatClient
		String output = "";
		for (String s : arrayOfUsers) {
			output += s + ",";
		}
		return output.substring(0,output.length()-1);//ommit last comma.
	}
}
