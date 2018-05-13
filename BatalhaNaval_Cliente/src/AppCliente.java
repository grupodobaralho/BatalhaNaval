import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class AppCliente 
{
	public static void main(String args[]) throws Exception
	{
		Scanner in = new Scanner(System.in);		
		System.out.println("Informe o IP do servidor: ");
		String ip = in.nextLine();
		System.out.println("Informe a porta: ");
		int port = Integer.parseInt(in.nextLine());
		ClienteJogo jogo = new ClienteJogo(ip, port);
		jogo.start();
	}

}