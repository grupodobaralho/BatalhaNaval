import java.io.*;
import java.net.*;

public class ClientNetWorkIO
{
	DatagramSocket clientSocket;
	int port;
	DatagramPacket Packet;
	InetAddress ServerIPAddress;
	byte[] Data;

	ClientNetWorkIO(String address, int port)
	{
		this.port = port;
		try
		{
			ServerIPAddress = InetAddress.getByName(address);
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

	public String getMessage()
	{
		Data = new byte[1024];
		Packet = new DatagramPacket(Data, Data.length);
		try
		{
			clientSocket.receive(Packet);
		}
		catch(IOException e)
		{
			System.err.println("Error: Erro ao receber pacote");
			return "";
		};
		return new String(Packet.getData());
	}

	public void sendMessage(String line)
	{
		Data = line.getBytes();
		Packet = new DatagramPacket(Data, Data.length, ServerIPAddress, port);
		try
		{;
			clientSocket.send(Packet);
		}
		catch(IOException e)
		{
			System.err.println("Error: Erro ao enviar pacote");
		}
	}
}