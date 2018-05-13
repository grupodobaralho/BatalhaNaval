import java.io.*;
import java.net.*;

class ServerNetWorkIO {
	private DatagramSocket socketServidor;
	private int porta;
	private DatagramPacket pacote;
	private byte[] dados;

	ServerNetWorkIO(int porta) {
		this.porta = porta;
		try {
			socketServidor = new DatagramSocket(porta);
		} catch (SocketException e) {
			System.err.println("Erro: Socket nâo pode ser criado");
			System.exit(1);
		}
	}

	public DatagramPacket getPacote() {
		dados = new byte[1024];
		pacote = new DatagramPacket(dados, dados.length);
		try {
			socketServidor.receive(pacote);
		} catch (IOException e) {
			System.err.println("Erro: Erro durante recebimento de pacote");
			return null;
		}
		return pacote;
	}

	public void enviaPacote(DatagramPacket pacote) {
		try {
			socketServidor.send(pacote);
		} catch (IOException e) {
			System.err.println("Erro: Erro durante envio de pacote");
		}
	}
}