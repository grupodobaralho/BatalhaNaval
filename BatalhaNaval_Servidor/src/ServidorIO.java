import java.io.*;
import java.net.*;

public class ServidorIO {
	private DatagramSocket socketServidor;
	private int porta;
	private DatagramPacket pacote;
	private byte[] dados;

	public ServidorIO(int port) {
		this.porta = port;
		try {
			socketServidor = new DatagramSocket(port);
		} catch (SocketException e) {
			System.err.println("Error: Socket nao pode ser criado");
			System.exit(1);
		}
	}

	public DatagramPacket getPacote() {
		dados = new byte[1024];
		pacote = new DatagramPacket(dados, dados.length);
		try {
			socketServidor.receive(pacote);
		} catch (IOException e) {
			System.err.println("Error: Erro ao receber pacote");
			return null;
		}
		return pacote;
	}

	public void enviaPacote(DatagramPacket pacote) {
		try {
			socketServidor.send(pacote);
		} catch (IOException e) {
			System.err.println("Error: Erro ao enviar pacote");
		}
	}
}