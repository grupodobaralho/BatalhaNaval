import java.util.Scanner;

/**
 * AppServer: Classe que implementa o m�todo main(), pede informa��es de porta
 * do servidor ao administrador e inicia jogo enviando as informa��es por
 * par�metro.
 * 
 * @author Israel Deorce
 *
 */
public class AppServer {
	private static Scanner in;

	public static void main(String args[]) throws Exception {
		in = new Scanner(System.in);
		System.out.println("Informe a porta do Servidor: ");
		int porta = in.nextInt();
		ServidorJogo server = new ServidorJogo(porta);
		server.start();
	}
}
