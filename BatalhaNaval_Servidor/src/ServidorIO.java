import java.io.*;
import java.net.*;
/**
 * Classe que é responsavel por fazer a gestão de entrada e saída de dados via
 * Socket Datagrama (UDP) pelo lado do servidor.
 * 
 * @author Israel Deorce
 *
 */
public class ServidorIO {
	private DatagramSocket socketServidor; // Socket UDP
	private DatagramPacket pacote; // Gera um pacote de Datagrama para envio

	private int porta;

	private byte[] dados; // array de bytes que carrega os dados

	/**
	 * Construtor que recebe a porta do Servidor, armazena localmente e seta como
	 * porta do socket DatagramaUDP
	 * 
	 * 
	 * @param porta
	 */
	public ServidorIO(int porta) {
		this.porta = porta;
		try {
			socketServidor = new DatagramSocket(porta);
		} catch (SocketException e) {
			System.err.println("Error: Socket nao pode ser criado");
			System.exit(1);
		}
	}

	/**
	 * Cria um pacote e aloca espaço para receber dados do cliente e retorna
	 * 
	 * @return
	 */
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

	/**
	 * Recebe um pacote de datagrama pronto e envia para o cliente via socket
	 * 
	 * @param pacote
	 */
	public void enviaPacote(DatagramPacket pacote) {
		try {
			socketServidor.send(pacote);
		} catch (IOException e) {
			System.err.println("Error: Erro ao enviar pacote");
		}
	}
}