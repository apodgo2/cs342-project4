package server;


//This class reperesents a conversation that's going on between client and server.
//It keeps track of states like the number of messages sent and received, for ex.
//And it uses these metrics to determine what to send next.

public class ServerProtocol {

	private int i = 0;

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