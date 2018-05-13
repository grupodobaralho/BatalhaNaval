import java.io.*;
import java.net.*;

public class ServerNetWorkIO {
	private DatagramSocket serverSocket;
	private int port;
	private DatagramPacket Packet;
	private byte[] Data;

	ServerNetWorkIO(int port) {
		this.port = port;
		try {
			serverSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.err.println("Error: Socket nao pode ser criado");
			System.exit(1);
		}
	}

	public DatagramPacket getPacket() {
		Data = new byte[1024];
		Packet = new DatagramPacket(Data, Data.length);
		try {
			serverSocket.receive(Packet);
		} catch (IOException e) {
			System.err.println("Error: Erro ao receber pacote");
			return null;
		}
		return Packet;
	}

	public void sendPacket(DatagramPacket packet) {
		try {
			serverSocket.send(packet);
		} catch (IOException e) {
			System.err.println("Error: Erro ao enviar pacote");
		}
	}
}