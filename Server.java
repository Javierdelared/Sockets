package com.sockets;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class Server {

	public static void main(String[] args) {

		ServerFrame frame = new ServerFrame();
		frame.setVisible(Boolean.TRUE);

	}

}

class ServerFrame extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private ServerSocket sever;
	private Socket socketIn;
	private Socket socketOut;

	private Map<String, String> clientList;

	public ServerFrame() {

		setTitle("Aplicación servidor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(800, 200, 300, 400);

		add(new DisplayPanel());
		setVisible(Boolean.TRUE);

		clientList = new HashMap<>();

		addWindowListener(new CloseWindow());

		Thread thread = new Thread(this);
		thread.start();
	}

	class CloseWindow extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent event) {

			Message inactive = new Message(Message.MESSAGE_SERVER, null, Message.STATE_0);

			clientList.entrySet().forEach(e -> sendMessage(e.getValue(), inactive));

		}

	}

	private class DisplayPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public DisplayPanel() {

			add(new JLabel("SERVIDOR", JLabel.CENTER));

		}
	}

	@Override
	public void run() {

		try {

			sever = new ServerSocket(Message.IN_PORT);

			while (Boolean.TRUE) {

				Message message = reciveMessage();

				if (Message.MESSAGE_CLIENT.equals(message.getNick())) {

					updateClientList(message);

					Message clients = new Message(Message.MESSAGE_SERVER, Message.LIST_CLIENTS, clientList);
					clientList.entrySet().forEach(e -> sendMessage(e.getValue(), clients));

				} else {

					sendMessage(clientList.get(message.getIp()), message);

				}
			}

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public void updateClientList(Message message) {

		String hostAddress = socketIn.getInetAddress().getHostAddress();

		String nick = message.getIp();

		if (Message.STATE_1.equals(message.getMensaje())) {

			if (isNickValid(nick)) {

				clientList.put(nick, hostAddress);
				Message nickOk = new Message(Message.MESSAGE_SERVER, null, Message.NICK_STATE_1);
				sendMessage(hostAddress, nickOk);

			} else {

				Message changeNick = new Message(Message.MESSAGE_SERVER, null, Message.NICK_STATE_0);
				sendMessage(hostAddress, changeNick);

			}

		} else if (Message.STATE_0.equals(message.getMensaje())) {

			clientList.remove(nick);

		}
	}

	public Boolean isNickValid(String nick) {

		return clientList.entrySet().stream().noneMatch(e -> e.getKey().equals(nick));

	}

	public void sendMessage(String ip, Message message) {

		try {

			socketOut = new Socket(ip, Message.OUT_PORT);
			ObjectOutputStream streamOut = new ObjectOutputStream(socketOut.getOutputStream());
			streamOut.writeObject(message);
			streamOut.close();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}

	public Message reciveMessage() throws IOException {
		
		try {
			
			socketIn = sever.accept();
			ObjectInputStream streamIn = new ObjectInputStream(socketIn.getInputStream());
			Message message = (Message) streamIn.readObject();
			streamIn.close();
			return message;
			
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
			return null;
			
		}
	}
}
