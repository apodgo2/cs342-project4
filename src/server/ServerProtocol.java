package server;


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
		//TODO: have this forward messages to every client
		//TODO: add a method in server to send a String to it's client using sendString, so we can call it from handleMessage()
		//return a heartbeat
		return heartbeat();
	}
	@Override
	public String handleGoodbye(String message) {
		//TODO: client disconnected, get username from message, tell ServerManager to close that connection if not already closed.
		//return null so that the Server breaks off the connection
		return null;
	}
	@Override
	public String handleHeartbeat(String message) {
		//TODO: have this update a variable in server showing last time the server received a heartbeat (use lastReply)
		//return a heartbeat
		return heartbeat();
	}
	@Override
	public String handleHello(String message) {
		//TODO: have this update a username variable in server with the client username, and tell other clients of connection
		//return a heartbeat
		return heartbeat();
	}
	//TX FORMATS
	@Override
	public String heartbeat() {
		return heartbeatPrefix+":Heartbeat!";
	}
	@Override
	public String goodbye() {
		return goodbyePrefix+":Goodbye!";
	}
}