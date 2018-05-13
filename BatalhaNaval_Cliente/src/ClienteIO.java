import java.io.*;
import java.net.*;

public class ClienteIO
{
	DatagramSocket clientSocket;
	int porta;
	DatagramPacket pacote;
	InetAddress enderecoIpServidor;
	byte[] dados;

	public ClienteIO(String endereco, int porta)
	{
		this.porta = porta;
		try
		{
			enderecoIpServidor = InetAddress.getByName(endereco);
		}
		catch(UnknownHostException e)
		{
			System.err.println("Error: Host invalido");
			System.exit(1);
		}
		try
		{
			clientSocket = new DatagramSocket();
		}
		catch(SocketException e)
		{
			System.err.println("Error: Socket nao pode ser criado");
			System.exit(1);
		}
	}

	public String getMensagem()
	{
		dados = new byte[1024];
		pacote = new DatagramPacket(dados, dados.length);
		try
		{
			clientSocket.receive(pacote);
		}
		catch(IOException e)
		{
			System.err.println("Error: Erro ao receber pacote");
			return "";
		};
		return new String(pacote.getData());
	}

	public void sendMensagem(String linha)
	{
		dados = linha.getBytes();
		pacote = new DatagramPacket(dados, dados.length, enderecoIpServidor, porta);
		try
		{;
			clientSocket.send(pacote);
		}
		catch(IOException e)
		{
			System.err.println("Error: Erro ao enviar pacote");
		}
	}
}