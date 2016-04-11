package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextPane;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.event.ActionEvent;

public class NetworkedChatClient {

	private NetworkedChatClient currentInstance = this;
	private JFrame frame;
	private JTextField txtUsername;
	private JTextField txtServerIP;
	private JTextField txtServerPort;
	private JTextField txtUserInput;
	private JTextPane txtpnMessages;
	private JScrollPane scrlpnMessages;
	private Client client;
	
	String message;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NetworkedChatClient window = new NetworkedChatClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NetworkedChatClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel control_panel = new JPanel();
		splitPane.setLeftComponent(control_panel);
		control_panel.setLayout(new GridLayout(1, 0, 0, 0));
		
		JLabel lblUsername = new JLabel("Username: ");
		control_panel.add(lblUsername);
		
		txtUsername = new JTextField();
		txtUsername.setText("User");
		control_panel.add(txtUsername);
		txtUsername.setColumns(10);
		
		JLabel lblServerIP = new JLabel("Server IP: ");
		control_panel.add(lblServerIP);
		
		txtServerIP = new JTextField();
		txtServerIP.setToolTipText("IP Address to server");
		txtServerIP.setText("127.0.0.1");
		control_panel.add(txtServerIP);
		txtServerIP.setColumns(10);
		
		JLabel lblServerPort = new JLabel("Server Port: ");
		control_panel.add(lblServerPort);
		
		txtServerPort = new JTextField();
		txtServerPort.setText("4242");
		control_panel.add(txtServerPort);
		txtServerPort.setColumns(10);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					InetAddress ipAddress = InetAddress.getByName(txtServerIP.getText().trim());
					int port = Integer.parseInt(txtServerPort.getText().trim());
					String username = txtUsername.getText().trim();
					client = new Client(new Socket(ipAddress, port), username, currentInstance);
					updateStatus("Attempting to connect to "+txtServerIP.getText().trim()+"\n");
				} catch (ConnectException ex) {
					updateStatus("ERROR: Could not connect to specified address/port pair.\n");
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		control_panel.add(btnConnect);
		
		JSplitPane messages_splitPane = new JSplitPane();
		messages_splitPane.setResizeWeight(0.9);
		messages_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setRightComponent(messages_splitPane);
		
		txtpnMessages = new JTextPane();
		txtpnMessages.setText(" ");
		scrlpnMessages = new JScrollPane(txtpnMessages);
		messages_splitPane.setLeftComponent(scrlpnMessages);
		
		JPanel messageEntry_panel = new JPanel();
		messages_splitPane.setRightComponent(messageEntry_panel);
		messageEntry_panel.setLayout(new BorderLayout(0, 0));
		
		JSplitPane messageEntry_splitPane = new JSplitPane();
		messageEntry_splitPane.setResizeWeight(0.8);
		messageEntry_panel.add(messageEntry_splitPane);
		
		txtUserInput = new JTextField();
		txtUserInput.setText("Type your message here.");
		messageEntry_splitPane.setLeftComponent(txtUserInput);
		txtUserInput.setColumns(10);
		txtUserInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				message = txtUserInput.getText();
				txtUserInput.setText("");
			}
		});
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: send message to the server
				//proto.respondString(text);
			}
		});
		messageEntry_splitPane.setRightComponent(btnSend);
	}
	
	public void updateStatus(String line) {
		if (txtpnMessages != null && line != null && line != "") {
			txtpnMessages.setText(txtpnMessages.getText()+line);
		}
	}

}
