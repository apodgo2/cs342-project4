package server;


import java.text.SimpleDateFormat;
import java.util.Calendar;

//This class used by Servers to write messages to STDOUT.
public class Messager{

	private int serverID; 
	private String prefix = "!DEFAULT!: ";
	private SimpleDateFormat sdf;
	private Calendar cal;
	private boolean main = false;

	public Messager(int serverId) {
		main = false;
		serverID = serverId;
		prefix = "SH#"+serverID+":\t";
		cal = Calendar.getInstance();
		sdf = new SimpleDateFormat("HH:mm:ss");
	}

	public Messager(boolean isMain) {
		main = isMain;
		serverID = -1;
		prefix = "MAIN:\t";
		cal = Calendar.getInstance();
		sdf = new SimpleDateFormat("HH:mm:ss");
	}

	public void println(String message, boolean needsDebugFlag) {
		cal.setTimeInMillis(System.currentTimeMillis());
		if(!needsDebugFlag) {
			ServerManager.getParent().updateStatus(sdf.format(cal.getTime())+" "+prefix+message+"\n");
		} else {
			if (ServerManager.debug) {
				ServerManager.getParent().updateStatus(sdf.format(cal.getTime())+" DEBUG: "+prefix+message+"\n");
			}
		}
	}

	public void println(String message) {//no debug flag given, means no debug.
		cal.setTimeInMillis(System.currentTimeMillis());
		ServerManager.getParent().updateStatus(sdf.format(cal.getTime())+" "+prefix+message+"\n");
	}

	public void print(String message, boolean needsDebugFlag) {
		cal.setTimeInMillis(System.currentTimeMillis());
		if(!needsDebugFlag) {
			ServerManager.getParent().updateStatus(sdf.format(cal.getTime())+" "+prefix+message);
		} else {
			if (ServerManager.debug) {
				ServerManager.getParent().updateStatus(sdf.format(cal.getTime())+" DEBUG: "+prefix+message);
			}
		}
	}

	public void print(String message) {//no debug flag given, means no debug.
		cal.setTimeInMillis(System.currentTimeMillis());
		ServerManager.getParent().updateStatus(sdf.format(cal.getTime())+" "+prefix+message);
	}

}