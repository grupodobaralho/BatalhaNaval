import java.io.*;
import java.net.*;

public class ClienteIO {
	DatagramSocket clienteSocket; // Socket UDP
	DatagramPacket pacote; // Gera um pacote de Datagrama para envio

	InetAddress enderecoIpServidor; // Endereço de IP do Servidor para envio de Datagrama
	int porta; // porta do Servidor

	byte[] dados; // array de bytes que carrega os dados

	/**
	 * Construtor que recebe o endereço e porta do Servidor e armazena nas variaveis
	 * locais
	 * 
	 * @param endereco
	 * @param porta
	 */
	public ClienteIO(String endereco, int porta) {
		this.porta = porta;
		try {
			enderecoIpServidor = InetAddress.getByName(endereco);
		} catch (UnknownHostException e) {
			System.err.println("Error: Host invalido");
			System.exit(1);
		}
		try {
			clienteSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.println("Error: Socket nao pode ser criado");
			System.exit(1);
		}
	}

	/**
	 * Método que prepara pacote para receber dados UDP vindos do Servidor.
	 * É chamado sempre que necessário pelo ClienteJogo
	 * @return
	 */
	public String getMensagem() {
		dados = new byte[1024];
		pacote = new DatagramPacket(dados, dados.length);
		try {
			clienteSocket.receive(pacote);
		} catch (IOException e) {
			System.err.println("Error: Erro ao receber pacote");
			return "";
		}
		return new String(pacote.getData());
	}

	/**
	 * Método que recebe informações em formato String, converte para bytes,
	 * prepara pacote e envia dados via UDP para o Servidor.
	 * É chamado sempre que necessário pelo ClienteJogo
	 * @return
	 */
	public void enviaMensagem(String linha) {
		dados = linha.getBytes();
		pacote = new DatagramPacket(dados, dados.length, enderecoIpServidor, porta);
		try {
			clienteSocket.send(pacote);
		} catch (IOException e) {
			System.err.println("Error: Erro ao enviar pacote");
		}
	}
}