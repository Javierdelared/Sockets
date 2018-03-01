package com.sockets;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.Map;

public class Client {

	public static void main(String[] args) {

		ClientFrame frame = new ClientFrame();
		frame.setVisible(true);

	}

}

class ClientFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static final String CONNECT_EXCEPTION_MESSAGE = "Fallo de conexión";
	private static final String CONNECTED_MESSAGE = "Conectado";

	private ServerSocket server;
	private Socket socketIn, socketOut;
	
	private JLabel serverMessage;
	
	private String nickClient;

	public ClientFrame() {

		setTitle("Aplicación cliente");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(300, 200, 300, 400);

		add(new DisplayPanel());
		addWindowListener(new CloseWindow());

	}

	class CloseWindow extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent event) {

			try {

				Message inactive = new Message(Message.MESSAGE_CLIENT, nickClient, Message.STATE_0);
				sendMessageToServer(inactive);

			} catch (ConnectException e) {

				serverMessage.setText(Message.colourText(CONNECT_EXCEPTION_MESSAGE, "red"));

			} catch (IOException e) {

				e.printStackTrace();

			}
		}
	}

	class DisplayPanel extends JPanel implements Runnable {

		private static final long serialVersionUID = 1L;

		private JComboBox<String> ip;
		private JButton send;
		private JLabel nickLabel;
		private JTextArea areaMessages;
		private JTextField textMessage;

		private Thread thread;

		public DisplayPanel() {

			nickClient = JOptionPane.showInputDialog("Elija su nick");
			nickLabel = new JLabel("Nick: --");
			add(nickLabel);

			add(new JLabel("Clientes: "));
			ip = new JComboBox<>(new DefaultComboBoxModel<>());
			add(ip);

			areaMessages = new JTextArea(15,20);
			add(areaMessages);

			textMessage = new JTextField(25);
			add(textMessage);

			send = new JButton("enviar");
			send.addActionListener(new ButtonClicked());
			send.setEnabled(false);
			add(send);
			
			serverMessage = new JLabel("");
			add(serverMessage);

			thread = new Thread(this);
			thread.start();

		}

		private class ButtonClicked implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if("".equals(textMessage.getText())) return ;
				
				Message messageOut = packMessage();

				try {

					sendMessageToServer(messageOut);

					showMessage(messageOut);

					textMessage.setText("");

				} catch (IOException e1) {

					e1.printStackTrace();

				}
			}

			public Message packMessage() {

				return new Message(nickClient, ip.getSelectedItem().toString(), textMessage.getText());

			}
		}

		@Override
		public void run() {

			connectWithServer();
			reciveMessages();

		}

		public void connectWithServer() {

			Boolean isNotConnected = Boolean.TRUE;
			Message active = new Message(Message.MESSAGE_CLIENT, nickClient, Message.STATE_1);

			while (isNotConnected) {

				try {

					Thread.sleep(1000);
					sendMessageToServer(active);
					send.setEnabled(true);
					serverMessage.setText(Message.colourText(CONNECTED_MESSAGE, "green"));
					isNotConnected = Boolean.FALSE;

				} catch (ConnectException e) {
					
					serverMessage.setText(Message.colourText(CONNECT_EXCEPTION_MESSAGE, "red"));

				} catch (IOException e) {

					e.printStackTrace();

				} catch (InterruptedException e) {

					e.printStackTrace();

				}
			}
		}

		public void reciveMessages() {

			try {

				server = new ServerSocket(Message.OUT_PORT);

				while (true) {

					Message messageIn = reciveMessage();

					if (isMessageFromServer(messageIn)) {

						handleMessageFromServer(messageIn);

					} else {

						showMessage(messageIn);

					}
				}

			} catch (IOException e) {

				e.printStackTrace();

			} catch (ClassNotFoundException e) {

				e.printStackTrace();

			}
		}

		public void handleMessageFromServer(Message messageIn) {

			String message = messageIn.getMensaje();

			switch (message) {

			case Message.LIST_CLIENTS:
				
				Map<String, String> clientList = messageIn.getclientList();
				ip.removeAllItems();
				clientList.keySet().stream().forEach(s -> ip.addItem(s));
				break;

			case Message.STATE_0:
				send.setEnabled(false);
				connectWithServer();
				break;

			case Message.NICK_STATE_0:
				nickClient = JOptionPane.showInputDialog("Elija otro nick");
				send.setEnabled(false);
				connectWithServer();
			case Message.NICK_STATE_1:
				nickLabel.setText("Nick: " + nickClient);
				break;

			default:
				break;
			}
		}

		public boolean isMessageFromServer(Message paqueteEntrada) {

			return Message.MESSAGE_SERVER.equals(paqueteEntrada.getNick());

		}
		
		public void showMessage(Message message) {
			
			String nickMessage = message.getNick();
			
			if(nickClient.equals(nickMessage)) {
				
				areaMessages.append("\n Tú (" + nickMessage + "): " + message.getMensaje());
			
			} else {
				
				areaMessages.append("\n" + nickMessage + ": " + message.getMensaje());
			
			}

		}
	}

	public void sendMessageToServer(Message message) throws UnknownHostException, IOException {

		socketOut = new Socket(Message.SERVER_IP, Message.IN_PORT);
		ObjectOutputStream streamOut = new ObjectOutputStream(socketOut.getOutputStream());
		streamOut.writeObject(message);
		streamOut.close();

	}

	public Message reciveMessage() throws IOException, ClassNotFoundException {

		socketIn = server.accept();
		ObjectInputStream streamIn = new ObjectInputStream(socketIn.getInputStream());
		Message message = (Message) streamIn.readObject();
		streamIn.close();
		return message;

	}
}