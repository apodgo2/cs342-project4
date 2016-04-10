package server;


//This class reperesents a conversation that's going on between client and server.
//It keeps track of states like the number of messages sent and received, for ex.
//And it uses these metrics to determine what to send next.

public class Protocol {

	@SuppressWarnings("unused")
	private static boolean isServer = true; //different protocol for client.
	private int i = 0;

	public Protocol(boolean isServer) {
		Protocol.isServer = isServer;
	}

	//parses input, decides what to send back, and then returns that.
	//if this returns null, or otherwise empty string, the connection will close.
	public String respondString(String input) {
		i++;
		if (i > 5) {
			return "";
		}
		if (input == "" || input == null || input.isEmpty()) {
			return null;
		}
		return "#"+i+":"+input+" to you too, sir!";
	}

	public String goodbye() {
		return "Goodbye!";
	}
}