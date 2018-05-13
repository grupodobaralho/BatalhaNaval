import java.io.*;
import java.net.*;

public class ServidorIO {
	private DatagramSocket serverSocket;
	private int porta;
	private DatagramPacket packet;
	private byte[] dados;

	ServidorIO(int port) {
		this.porta = port;
		try {
			serverSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.err.println("Error: Socket nao pode ser criado");
			System.exit(1);
		}
	}

	public DatagramPacket getPacket() {
		dados = new byte[1024];
		packet = new DatagramPacket(dados, dados.length);
		try {
			serverSocket.receive(packet);
		} catch (IOException e) {
			System.err.println("Error: Erro ao receber pacote");
			return null;
		}
		return packet;
	}

	public void sendPacket(DatagramPacket packet) {
		try {
			serverSocket.send(packet);
		} catch (IOException e) {
			System.err.println("Error: Erro ao enviar pacote");
		}
	}
}