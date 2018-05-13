import java.io.*;
import java.net.*;
import java.util.Arrays;

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
			System.err.println("Error: Socket could not be created");
			System.exit(1);
		}
	}

	public DatagramPacket getPacket() {
		Data = new byte[1024];
		Packet = new DatagramPacket(Data, Data.length);
		try {
			serverSocket.receive(Packet);
		} catch (IOException e) {
			System.err.println("Error: error while recieving packet");
			return null;
		}
		return Packet;
	}

	public void sendPacket(DatagramPacket packet) {
		try {
			serverSocket.send(packet);
		} catch (IOException e) {
			System.err.println("Error: error while sending packet");
		}
	}
}