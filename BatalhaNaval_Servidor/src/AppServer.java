import java.util.Scanner;

public class AppServer 
{
	private static Scanner in;

	public static void main(String args[]) throws Exception
	{
		in = new Scanner(System.in);
		System.out.println("Informe a porta do Servidor: ");
		int porta = in.nextInt();
		ServidorJogo Server = new ServidorJogo(porta);
		Server.start();
	}
}
