package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	private byte[] buffer = new byte[4096];
	private BufferedInputStream is;
	private BufferedOutputStream os;
	private int count = 0;
	private static Scanner scan = new Scanner(System.in);
	private Protocol prot = new Protocol(false);

	public Client(Socket sock) {
		try {
			is = new BufferedInputStream(sock.getInputStream());
			os = new BufferedOutputStream(sock.getOutputStream());
		} catch (IOException ex) {
			System.out.println("IOException in "+this.hashCode());
		}
		System.out.println("New Client "+this.hashCode()+" successfully created.");
		String response = "!default!";
		while(response != "" && response != null && !response.isEmpty()) {
			this.respond();
			response = this.listen(is);
			if (response.contains(prot.goodbye())) {
				System.out.println("Server says goodbye, closes connection.");
				break;
			} else {
				System.out.println("Received reply from server: \n"+response);
			}
		}
	}

	private void respond() {
		count++;
		System.out.println("Type message #"+count+", you are go:");
		this.send(os, (String) scan.nextLine());
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

	public static void disconnect() {

	}

}