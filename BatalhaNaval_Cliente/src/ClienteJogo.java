import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * Classe que administra o Jogo do lado do Cliente. Esta classe utiliza o
 * ClienteIO para mandar e receber dados via Socket, armazena e apresenta as
 * informações recebidas do servidor referentes ao jogo.
 * 
 * @author Israel Deorce
 *
 */
public class ClienteJogo {
	private ClienteIO io; // Variavel de referencia para a classe que administra Entrada e Saida
	private BufferedReader inUsuario; // Buffered Reader para leitura do input do usuário
	private String usuario; // Armazena Nome do Usuário
	private boolean jogoAtivo; // Variavel que controla se o jogo está ativo e controle while(jogoAtivo)

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

		// O nome do usuário é enviado seguido do comando join
		io.enviaMensagem(usuario + ":join");
		String mensagem = io.getMensagem();

		// O servidor retorna com uma mensagem de servidor cheio ou de bem vindo e
		// inicia o jogo
		if (mensagem.equals("Servidor cheio")) {
			System.out.println("Servidor cheio, tente novamente mais tarde");
			System.exit(0);
		} else {
			processaComando(mensagem);
			iniciaJogo();
		}

		// Ao termino do jogo, o cliente informa ao servidor o seu nome e o comando quit
		io.enviaMensagem(usuario + ":quit");
	}

	/**
	 * Método auxiliar para definição de um nome de usuário
	 * 
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

	/**
	 * Loop do jogo que chama criação de navios e fica enviando e recebendo mensagem
	 * do servidor para cada acao
	 */
	public void iniciaJogo() {
		jogoAtivo = true;
		io.enviaMensagem(usuario + ":" + getNavios());
		String m = io.getMensagem();
		processaComando(m);
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
		// Inicia inserindo o caracter '-' em todas as posições da matriz
		for (int i = 0; i < 10; i++) {
			Arrays.fill(tabuleiro[i], '-');
		}

		// Inicia while que rodará até que todos os cinco navios tenham sido criados
		System.out.println("Por favor escolha a localizacao dos navios que voce deseja alocar");
		String[] navios = new String[5];
		int navioAtual = 0; // controla qual navio esta sendo alocado
		int tamanho = 0; // auxilia nas funções matemáticas que validam a posição informada
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

			// Apresenta tabuleiro atualizado para o cliente
			apresentaNavios(tabuleiro);

			// Pede ao cliente uma localização inicial e armazena
			System.out.println("Informe o primeiro quadrante do navio");
			String navioLoc = pegaLinha();

			// Verifica se flaz conflito com outro navio
			if (verificaLoc(navioLoc, tabuleiro)) {
				System.out.println("Local invalido, faz conflito com outro navio");
				continue;
			}

			// Pede ao cliente uma localização final e armazena
			System.out.println("Informe o ultimo quadrante da localizacao do navio");
			String finalLoc = pegaLinha();

			// Se for um navio em Vertical
			if (navioLoc.charAt(0) == finalLoc.charAt(0)) {

				// Calcula a diferença (distancia) da posicao ini para a final
				if (navioLoc.charAt(1) - finalLoc.charAt(1) < 0) {
					// se der negativo, faz ABS, diminui 1 e compara se é o tamanho que deveria ter
					if (Math.abs(navioLoc.charAt(1) - finalLoc.charAt(1) - 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					// Forma String com todas os quadrantes referentes, menos o ultimo.
					// EX: de b0-b3; String = b0b1b2
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += navioLoc.charAt(0);
						navioLoc += (char) (navioLoc.charAt(1) + j + 1);
					}
				} else {
					// Se for positivo, faz ABS, aumenta 1 e compara se é o tamanho que deveria ter
					if (Math.abs(navioLoc.charAt(1) - finalLoc.charAt(1) + 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					// Forma String com todas os quadrantes referentes, menos o ultimo.
					// EX: de b0-b3; String = b0b1b2
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += navioLoc.charAt(0);
						navioLoc += (char) (finalLoc.charAt(1) + j + 1);
					}
				}

				// completa com o ultimo quadrante e envia para o codigo que verifica colisao
				navioLoc += finalLoc;
				if (estaColidindo(navioLoc, tabuleiro)) {
					System.out.println("Local invalido, dois ou mais navios estao colidindo");
					continue;
				}

				/*
				 * Se tudo der certo, utiliza atualiza as posições da matriz com S e vai para o
				 * próximo navio, se houver.
				 */
				navios[navioAtual] = navioLoc;
				for (int i = 0; i < navioLoc.length(); i += 2) {
					String loc = navioLoc.substring(i, i + 2);
					tabuleiro[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				navioAtual++;
				continue;

			}

			// Se for um navio na Horizontal (segue a mesma linha de comentarios acima)
			if (navioLoc.charAt(1) == finalLoc.charAt(1)) {
				if (navioLoc.charAt(0) - finalLoc.charAt(0) < 0) {
					if (Math.abs(navioLoc.charAt(0) - finalLoc.charAt(0) - 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					// Forma String com todas os quadrantes referentes, menos o ultimo.
					// EX: de b0-b3; String = b0b1b2
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += (char) (navioLoc.charAt(0) + j + 1);
						navioLoc += navioLoc.charAt(1);
					}
				} else {
					if (Math.abs(navioLoc.charAt(0) - finalLoc.charAt(0) + 1) != tamanho) {
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					// Forma String com todas os quadrantes referentes, menos o ultimo.
					// EX: de b0-b3; String = b0b1b2
					for (int j = 0; j < tamanho - 2; j++) {
						navioLoc += (char) (finalLoc.charAt(0) + j + 1);
						navioLoc += navioLoc.charAt(1);
					}
				}
				// completa com o ultimo quadrante e envia para o codigo que verifica colisao
				navioLoc += finalLoc;
				if (estaColidindo(navioLoc, tabuleiro)) {
					System.out.println("Local invalido, dois ou mais navios estao colidindo");
					continue;
				}

				/*
				 * Se tudo der certo, utiliza atualiza as posições da matriz com S e vai para o
				 * próximo navio, se houver.
				 */
				navios[navioAtual] = navioLoc;
				for (int i = 0; i < navioLoc.length(); i += 2) {
					String loc = navioLoc.substring(i, i + 2);
					tabuleiro[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				navioAtual++;
				continue;
			}

			// Se não for nem Vertical e nem Horizontal, anuncia erro
			System.out.println("Local invalido, navios devem ser posicionados em linha reta");
		}

		// Retorna os navios alocados em formato de String para serem enviados via
		// Socket
		return navios[0] + navios[1] + navios[2] + navios[3] + navios[4];
	}

	/**
	 * Compara posição informada com cálculo de área do navio
	 * 
	 * @param loc
	 * @param tabuleiro
	 * @return
	 */
	public boolean verificaLoc(String loc, char[][] tabuleiro) {
		return tabuleiro[loc.charAt(0) - 97][loc.charAt(1) - 48] == 'S';
	}

	/**
	 * Recebe uma String da localidade completa de um novo navio e verifica com o
	 * tabuleiro do cliente se já não existe um navio alí ex: b0b1b2... para um
	 * navio b0-b2.
	 * 
	 * @param navioLoc
	 * @param tabuleiro
	 * @return
	 */
	public boolean estaColidindo(String navioLoc, char[][] tabuleiro) {
		System.out.println(navioLoc);
		for (int i = 0; i < navioLoc.length(); i += 2) {
			String loc = navioLoc.substring(i, i + 2);
			if (verificaLoc(loc, tabuleiro)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Método que printa os navios do cliente na tela
	 * 
	 * @param tabuleiro
	 */
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

	/**
	 * Um comando é formado por "substring1:substring2". Este método recebe um
	 * comando, quebra, verifica se recebeu uma condição de término de jogo,
	 * atualiza variável e printa substring2 a tela.
	 * 
	 * @param comando
	 */
	public void processaComando(String comando) {
		String interno = comando.substring(0, comando.indexOf(":"));
		String externo = comando.substring(comando.indexOf(":") + 1, comando.indexOf(0));
		if (interno.equals("win") || interno.equals("lose") || interno.equals("reset")) {
			jogoAtivo = false;
		}
		System.out.println(externo);
	}
}