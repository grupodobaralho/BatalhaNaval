import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ClienteJogo {
	private ClienteIO io;
	private BufferedReader inUsuario; // Buffered Reader para leitura do input do usuário
	private String usuario;
	private boolean jogoAtivo;

	public ClienteJogo(String endereco, int porta) {
		io = new ClienteIO(endereco, porta);
		inUsuario = new BufferedReader(new InputStreamReader(System.in));

	}

	/**
	 * Método que inicia um jogo criando um usuario, informando ao servidor e
	 * recebendo resposta
	 */
	public void start() {
		usuario = criaNome();
		System.out.println("Usuario eh: " + usuario);
		// O nome do usuário é enviado segui o comando join
		io.enviaMensagem(usuario + ":join");
		String mensagem = io.getMensagem();
		// O servidor retorna com uma mensagem de servidor cheio ou de bem vindo e
		// inicia o jogo
		if (mensagem.equals("Servidor cheio")) {
			System.out.println("Servidor cheio, tente novamente mais tarde");
			System.exit(0);
		} else {
			System.out.println(mensagem);
			iniciaJogo();
		}
		// Ao termino do jogo, o cliente informa ao servidor o seu nome e o comando quit
		io.enviaMensagem(usuario + ":quit");
	}

	/**
	 * Método auxiliar para definição de um nome de usuário
	 * @return
	 */
	public String criaNome() {
		while (true) {
			System.out.print("Por favor digite um nome de usuario: ");
			try {
				return inUsuario.readLine();
			} catch (IOException e) {
				System.err.println("Error: Entrada de texto invalida");
			}
		}
	}

	public void iniciaJogo() {
		jogoAtivo = true;
		io.enviaMensagem(usuario + ":" + getNavios());
		processaComando(io.getMensagem());
		while (jogoAtivo) {
			io.enviaMensagem(usuario + ":" + pegaLinha());
			System.out.println("Pacote enviado... Aguardando Replay...");
			processaComando(io.getMensagem());
		}
	}

	/**
	 * Função que aguarda o recebimento de uma posição: "LETRA+NÚMERO" de um usuário
	 * cliente, valida e retorna o comando referente.
	 * 
	 * @return
	 */
	public String pegaLinha() {
		String comando = "";
		while (true) {
			try {
				System.out.println("Por favor digite uma combinação de letra e número. EX: 'a0'");
				comando = inUsuario.readLine();
			} catch (IOException e) {
				System.err.println("Error: Entrada de texto invalida");
			}
			if (comando.matches("[a-jA-J]{1}[0-9]{1}")) {
				return comando.toLowerCase();
			}
			System.out.println("Tente novamente!");
		}
	}

	/**
	 * Método que cria o tabuleiro do jogador com os navios e converte a informação
	 * para String que será enviada via Socket
	 * 
	 * @return
	 */
	public String getNavios() {
		char[][] tabuleiro = new char[10][10];
		for (int i = 0; i < 10; i++) {
			Arrays.fill(tabuleiro[i], '-');
		}
		System.out.println("Por favor escolha a localizacao dos navios que voce deseja alocar");
		String[] navios = new String[5];
		int navioAtual = 0;
		int tamanho = 0;
		while (navioAtual != 5) {
			switch (navioAtual) {
			case 0:
				System.out.println("Alocando porta-avioes(5)!");
				tamanho = 5;
				break;
			case 1:
				System.out.println("Alocando navio-tanque(4)!");
				tamanho = 4;
				break;
			case 2:
				System.out.println("Alocando contratorpedeiros(3)!");
				tamanho = 3;
				break;
			case 3:
				System.out.println("Alocando submarinos(3)!");
				tamanho = 3;
				break;
			case 4:
				System.out.println("Alocando patrulhador(2)!");
				tamanho = 2;
				break;
			}
			apresentaNavios(tabuleiro);
			System.out.println("Informe o primeiro quadrante do navio");
			String navioLoc = pegaLinha();
			if (verificaLoc(navioLoc, tabuleiro)) {
				System.out.println("Local invalido, faz conflito com outro navio");
				continue;
			}
			System.out.println("Informe o ultimo quadrante da localizacao do navio");
			String finalLoc = pegaLinha();
			if (navioLoc.charAt(0) == finalLoc.charAt(0)) {
				if (navioLoc.charAt(1) - finalLoc.charAt(1) < 0) {
					if (Math.abs(navioLoc.charAt(1) - finalLoc.charAt(1) - 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += navioLoc.charAt(0);
						navioLoc += (char) (navioLoc.charAt(1) + j + 1);
					}
				} else {
					if (Math.abs(navioLoc.charAt(1) - finalLoc.charAt(1) + 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += navioLoc.charAt(0);
						navioLoc += (char) (finalLoc.charAt(1) + j + 1);
					}
				}
				navioLoc += finalLoc;
				if (isColliding(navioLoc, tabuleiro)) {
					System.out.println("Local invalido, dois ou mais navios estao colidindo");
					continue;
				}
				navios[navioAtual] = navioLoc;
				for (int i = 0; i < navioLoc.length(); i += 2) {
					String loc = navioLoc.substring(i, i + 2);
					tabuleiro[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				navioAtual++;
				continue;

			}
			if (navioLoc.charAt(1) == finalLoc.charAt(1)) {
				if (navioLoc.charAt(0) - finalLoc.charAt(0) < 0) {
					if (Math.abs(navioLoc.charAt(0) - finalLoc.charAt(0) - 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += (char) (navioLoc.charAt(0) + j + 1);
						navioLoc += navioLoc.charAt(1);
					}
				} else {
					if (Math.abs(navioLoc.charAt(0) - finalLoc.charAt(0) + 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += (char) (finalLoc.charAt(0) + j + 1);
						navioLoc += navioLoc.charAt(1);
					}
				}
				navioLoc += finalLoc;
				if (isColliding(navioLoc, tabuleiro)) {
					System.out.println("Local invalido, dois ou mais navios estao colidindo");
					continue;
				}
				navios[navioAtual] = navioLoc;
				for (int i = 0; i < navioLoc.length(); i += 2) {
					String loc = navioLoc.substring(i, i + 2);
					tabuleiro[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				navioAtual++;
				continue;
			}
			System.out.println("Local invalido, navios devem ser posicionados em linha reta");
		}
		return navios[0] + navios[1] + navios[2] + navios[3] + navios[4];
	}

	public boolean verificaLoc(String Loc, char[][] tabuleiro) {
		return tabuleiro[Loc.charAt(0) - 97][Loc.charAt(1) - 48] == 'S';
	}

	public boolean isColliding(String navioLoc, char[][] tabuleiro) {
		for (int i = 0; i < navioLoc.length(); i += 2) {
			String loc = navioLoc.substring(i, i + 2);
			if (verificaLoc(loc, tabuleiro)) {
				return true;
			}
		}
		return false;
	}

	public void apresentaNavios(char[][] tabuleiro) {
		String out = " abcdefghij\n";
		for (int i = 0; i < 10; i++) {
			out += i;
			for (int j = 0; j < 10; j++) {
				out += tabuleiro[j][i];
			}
			out += '\n';
		}
		System.out.println(out);
	}

	public void processaComando(String comando) {
		String interno = comando.substring(0, comando.indexOf(":"));
		String externo = comando.substring(comando.indexOf(":") + 1, comando.indexOf(0));
		if (interno.equals("win") || interno.equals("lose") || interno.equals("reset")) {
			jogoAtivo = false;
		}
		System.out.println(externo);
	}
}