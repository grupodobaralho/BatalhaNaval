import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class gameServer 
{
	public static void main(String args[]) throws Exception
	{
		Scanner in = new Scanner(System.in);
		System.out.println("Informe a porta do Servidor: ");
		int port = in.nextInt();
// 		int port = 0;
//		if(args.length < 1)
//		{
//			System.err.println("Error: Por favor, inclue um numero de porta");
//			System.exit(1);
//		}
//		try
//		{
//			port = Integer.parseInt(args[0]);
//		}
//		catch(NumberFormatException e)
//		{
//			System.err.println("Error: Argumento não pode ser convertido em inteiro");
//			System.exit(1);
//		}
		ServerGame Server = new ServerGame(port);
		Server.start();
	}
}
