package server;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.Timer;

import javax.swing.UIManager;
import java.awt.Font;
import javax.swing.JSeparator;

public class NetworkedChatServer {

	private NetworkedChatServer currentInstance = this;
	private JFrame frame;
	private JTextField txtServerIP;
	private JTextField txtServerPort;
	private JLabel lblServerStatusIndicator;
	private static ServerManager server;
	private Timer statusUpdate;
	private JTextPane txtpnStatus;
	private JScrollPane scrlpnStatus;
	private JTextField txtConnection;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NetworkedChatServer window = new NetworkedChatServer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		//shutdown hook to make sure our server is properly disconnected
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("Shutting down...");
				server.shutdownServers();
			}
		}, "Shutdown-thread"));
	}

	/**
	 * Create the application.
	 */
	public NetworkedChatServer() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 700, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel controls_panel = new JPanel();
		splitPane.setLeftComponent(controls_panel);
		controls_panel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JLabel lblServerIP = new JLabel("Server IP Address: ");
		controls_panel.add(lblServerIP);
		
		txtServerIP = new JTextField();
		txtServerIP.setToolTipText("Specify server IP to bind to.");
		txtServerIP.setText("127.0.0.1");
		controls_panel.add(txtServerIP);
		txtServerIP.setColumns(10);
		
		JLabel lblServerPort = new JLabel("Server Port: ");
		controls_panel.add(lblServerPort);
		
		txtServerPort = new JTextField();
		txtServerPort.setToolTipText("Specify server port.");
		txtServerPort.setText("4242");
		controls_panel.add(txtServerPort);
		txtServerPort.setColumns(10);
		
		JLabel lblStartServer = new JLabel("Start Server: ");
		controls_panel.add(lblStartServer);
		
		JButton btnStartServer = new JButton("Start Server");
		btnStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (server != null && server.isRunning()) {
					updateStatus("COMMAND EXECUTION WARNING: Server already running!\n");
					return;
				}
				String address = txtServerIP.getText().trim(); //TODO: I don't know how to validate the IP, so I certainly hope they don't type a misformatted one.
				int port = Integer.parseInt(txtServerPort.getText());
				if (port < 1024 || port > 65535) {
					System.err.println("USER SPECIFIED INVALID PORT NUMBER!");
					updateStatus("COMMAND EXECUTION ERROR: Specified invalid port number!!\n");
				}
				server = new ServerManager(address, port, currentInstance);
				updateStatus("Created new server bound to address "+address+" and port "+port+"\n");
				new Thread(server).start();
				
				if (server.isRunning())
	    			lblServerStatusIndicator.setText("ONLINE");
				//start our update timer
				statusUpdate.start();
			}
		});
		btnStartServer.setToolTipText("Start the server.");
		controls_panel.add(btnStartServer);
		
		JLabel lblStopServer = new JLabel("Stop Server: ");
		controls_panel.add(lblStopServer);
		
		JButton btnStopServer = new JButton("Stop Server");
		btnStopServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (server != null && server.isRunning()) {
					server.shutdownServers();
				} else {
					updateStatus("COMMAND EXECUTION ERROR: Server already stopped!\n");
					return;
				}
			}
		});
		btnStopServer.setToolTipText("Stop the server.");
		controls_panel.add(btnStopServer);
		
		JLabel lblServerStatus = new JLabel("Server Status: ");
		controls_panel.add(lblServerStatus);
		
		lblServerStatusIndicator = new JLabel("OFFLINE");
		lblServerStatusIndicator.setToolTipText("Current server status.");
		lblServerStatusIndicator.setFont(new Font("Tahoma", Font.BOLD, 14));
		controls_panel.add(lblServerStatusIndicator);
		
		JSeparator separator = new JSeparator();
		controls_panel.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		controls_panel.add(separator_1);
		
		JLabel lblListConnections = new JLabel("List Connections: ");
		controls_panel.add(lblListConnections);
		
		JButton btnListConnections = new JButton("List Connections");
		btnListConnections.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				server.listServers();
			}
		});
		btnListConnections.setToolTipText("List connections to the server, if applicable");
		controls_panel.add(btnListConnections);
		
		JLabel lblCloseConnection = new JLabel("Close Connection: ");
		controls_panel.add(lblCloseConnection);
		
		JPanel closeconnection_panel = new JPanel();
		controls_panel.add(closeconnection_panel);
		closeconnection_panel.setLayout(new GridLayout(1, 0, 0, 0));
		
		txtConnection = new JTextField();
		txtConnection.setToolTipText("Enter Connection index here");
		txtConnection.setText("0");
		closeconnection_panel.add(txtConnection);
		txtConnection.setColumns(10);
		
		JButton btnCloseConnection = new JButton("Close Connection");
		btnCloseConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = Integer.parseInt(txtConnection.getText());
				if (index < 0 || index >= server.serverListSize()) {//needs to be valid index
					updateStatus(("COMMAND EXECUTION ERROR: Invalid connection index. Please specify a valid connection.\n"));
				} else {
					server.closeServer(index);
				}
			}
		});
		closeconnection_panel.add(btnCloseConnection);
		
		JPanel status_panel = new JPanel();
		splitPane.setRightComponent(status_panel);
		status_panel.setLayout(new BorderLayout(0, 0));
		
		txtpnStatus = new JTextPane();
		txtpnStatus.setFont(UIManager.getFont("TextArea.font"));
		txtpnStatus.setEditable(false);
		scrlpnStatus = new JScrollPane(txtpnStatus);
		status_panel.add(scrlpnStatus, BorderLayout.CENTER);
		
		//Status update timer
		statusUpdate = new Timer(1000, new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
		    	//make sure stuff exists in the first place
		    	if (lblServerStatusIndicator != null && server != null) {
		    		//check server status
		    		if (server.isRunning())
		    			lblServerStatusIndicator.setText("ONLINE");
		    		else
		    			lblServerStatusIndicator.setText("OFFLINE");
		    	} else if (lblServerStatusIndicator != null) {//label exists, server not instantiated yet.
		    		lblServerStatusIndicator.setText("OFFLINE");
		    	}
		    }
		});
	}
	
	public void updateStatus(String line) {
		if (txtpnStatus != null && line != null && line != "") {
			txtpnStatus.setText(txtpnStatus.getText()+line);
		}
	}

}
