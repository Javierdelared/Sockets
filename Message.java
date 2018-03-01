package com.sockets;

import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable {
	
	public static final String SERVER_IP = "192.168.0.158";
	public static final String STATE_0 = "Inactive";
	public static final String STATE_1 = "Active";
	public static final String NICK_STATE_0 = "changeNick";
	public static final String NICK_STATE_1 = "nickOk";
	public static final String MESSAGE_CLIENT = "MessageClient";
	public static final String MESSAGE_SERVER = "MessageServer";
	public static final String LIST_CLIENTS = "listClients";
	
	public static final int IN_PORT = 4040;
	public static final int OUT_PORT = 3030;

	private static final long serialVersionUID = 1L;
	private String nick, ip, mensaje;
	private Map<String, String> listaClientes;


	public Message(String nick, String ip, String mensaje) {
		this.nick = nick;
		this.ip = ip;
		this.mensaje = mensaje;
	}
	public Message(String nick, String mensaje, Map<String, String> listaClientes) {
		this.nick = nick;
		this.mensaje = mensaje;
		this.listaClientes = listaClientes;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Map<String, String> getclientList() {
		return listaClientes;
	}

	public void setListaClientes(Map<String, String> listaClientes) {
		this.listaClientes = listaClientes;
	}
	
	@Override
	public String toString() {
		return "PaqueteMensaje [nick=" + nick + ", ip=" + ip + ", mensaje=" + mensaje + "]";
	}
	
	public static String colourText(String text, String colour) {
		return "<html><font color='" + colour + "'>" + text + "</font></html>";
	}
	
}