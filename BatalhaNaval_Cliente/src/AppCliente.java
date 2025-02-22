import java.util.Scanner;

/**
 * Classe que implementa o m�todo main. Recupera informa��es de IP e porta do
 * jogador e inicia jogo enviando as informa��es por par�metro.
 * 
 * @author Israel Deorce
 *
 */
public class AppCliente {
	private static Scanner in;

	public static void main(String args[]) throws Exception {
		in = new Scanner(System.in);
		System.out.println("Informe o IP do servidor: ");
		String ip = in.nextLine();
		System.out.println("Informe a porta: ");
		int porta = Integer.parseInt(in.nextLine());
		ClienteJogo jogo = new ClienteJogo(ip, porta);
		jogo.start();
	}

}