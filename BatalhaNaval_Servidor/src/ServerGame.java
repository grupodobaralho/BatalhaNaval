import java.net.*;

public class ServerGame {
	ServerNetWorkIO IO; // Classe que faz o tratamento da rede - Entrada e Saida (IO)

	private InetAddress[] enderecosIP; // Vetor de IPs Inet que guarda os ips dos clientes
	private String[] nomes; // Guarda o nome dos clientes
	private int portas[]; // Guarda a porta de acesso aos clientes
	private JShipGame jogo; // Classe que faz o tratamento do jogo no servidor
	boolean idsNavios[]; //
	boolean jogoAtivo; // Variavel que indica se o jogo está ativo

	public ServerGame(int porta) {
		IO = new ServerNetWorkIO(porta);
		enderecosIP = new InetAddress[2];
		nomes = new String[2];
		portas = new int[2];
		idsNavios = new boolean[] { false, false };
		jogoAtivo = false;
	}

	/**
	 * Método principal que controla o recebimento de pacotes e manda processar
	 * 
	 */
	void start() {
		while (true) {
			System.out.println("Aguardando pacote...");
			DatagramPacket pacote = IO.getPacote();
			if (pacote != null) {
				processaInput(pacote);
			}
		}
	}

	/**
	 * 
	 * @param pacote
	 */
	public void processaInput(DatagramPacket pacote) {
		String mensagemPacote = new String(pacote.getData());
		String nome = mensagemPacote.substring(0, mensagemPacote.indexOf(':'));
		String mensagem = mensagemPacote.substring(mensagemPacote.indexOf(':') + 1, mensagemPacote.indexOf(0));
		int jogadorIndex = getIndex(pacote.getAddress(), nome);
		String resultado = "";
		
		//Em caso de jogador não registrado no servidor, verifica e cria-se jogador
		if (jogadorIndex == -1) {
			if (mensagem.equals("join")) {
				jogadorIndex = criaJogadorIndex(pacote.getAddress(), nome, pacote.getPort());
				if (jogadorIndex == -1) {
					resultado = "ruim:Servidor cheio";
				} else {
					resultado = "bom:Bem vindo ao servidor, " + nome + "!";
				}
				
				//Se os dois IPs estiverem registrados, inicia-se o jogo
				if (!(enderecosIP[0] == null) && !(enderecosIP[1] == null)) {
					jogoAtivo = true;
					jogo = new JShipGame();
				}
			}

		} else { //Caso um jogador saia
			if (mensagem.equals("sair") && jogadorIndex != -1) {
				removeJogadorIndex(jogadorIndex);
				jogoAtivo = false;
				idsNavios = new boolean[] { false, false };
				IO.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length,
						pacote.getAddress(), pacote.getPort()));
				if (enderecosIP[(jogadorIndex + 1) % 2] == null) {
					resultado = "reseta:Reseta servidor, jogador saiu";
					IO.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length,
							enderecosIP[(jogadorIndex + 1) % 2], portas[(jogadorIndex + 1) % 2]));
				}
				return;
			} else {
				if (!jogoAtivo) {
					resultado = "ruim:Aguardando jogadores adicionais!";
				} else {
					resultado = processaMovimento(jogadorIndex, nome, mensagem);
				}
				if (resultado.substring(0, resultado.indexOf(":")).equals("vitoria")) {
					String out = "derrota:Você perdeu!!";
					IO.enviaPacote(new DatagramPacket(out.getBytes(), out.getBytes().length,
							enderecosIP[(jogadorIndex + 1) % 2], portas[(jogadorIndex + 1) % 2]));
					enderecosIP = new InetAddress[2];
					nomes = new String[2];
					portas = new int[2];
					idsNavios = new boolean[] { false, false };
					jogoAtivo = false;
				}
			}
		}
		IO.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length, pacote.getAddress(),
				pacote.getPort()));
	}

	/**
	 * Método que cria um jogador e armazena as variaveis
	 * 
	 * @param IP
	 * @param nome
	 * @param porta
	 * @return
	 */
	private int criaJogadorIndex(InetAddress IP, String nome, int porta) {
		for (int i = 0; i < enderecosIP.length; i++) {
			if (enderecosIP[i] == null) {
				enderecosIP[i] = IP;
				nomes[i] = nome;
				portas[i] = porta;
				System.out.println("Jogador: " + nome + " IP Origem: " + IP);
				return i;
			}
		}
		return -1;
	}

	/**
	 * Método que remove jogador de index referente
	 * 
	 * @param index
	 */
	private void removeJogadorIndex(int index) {
		if (index != -1) {
			enderecosIP[index] = null;
			nomes[index] = null;
			portas[index] = 0;
		}
	}

	/**
	 * Método que retorna index de um jogador especifico a partir do seu IP e nome
	 * 
	 * @param IP
	 * @param nome
	 * @return
	 */
	private int getIndex(InetAddress IP, String nome) {
		for (int i = 0; i < enderecosIP.length; i++) {
			if (enderecosIP[i] != null && enderecosIP[i].equals(IP) && nome.equals(nomes[i]))
				return i;
		}
		return -1;
	}

	/**
	 * Metodo que recebe
	 * 
	 * @param jogadorIndex
	 * @param nome
	 * @param movimento
	 * @return
	 */
	private String processaMovimento(int jogadorIndex, String nome, String movimento) {
		if (idsNavios[jogadorIndex] == false) {
			jogo.atribuirNavios(jogadorIndex, movimento);
			idsNavios[jogadorIndex] = true;
			return "bom:Ships succesfully placed!";
		} else {
			if (!idsNavios[(jogadorIndex + 1) % 2]) {
				return "ruim:Esperando outros jogadores colocarem navios";
			}
			int x = movimento.charAt(0) - 97;
			int y = movimento.charAt(1) - 48;
			int resultado = jogo.fazMovimeto(jogadorIndex, x, y);
			String str;
			switch (resultado) {
			case -2:
				str = "ruim:Ainda não é o seu turno!!";
				break;
			case -1:
				str = "ruim:Você já atacou este local!";
				break;
			case 0:
				str = "bom:MISS!!";
				break;
			case 1:
				str = "bom:HIT!!";
				break;
			case 2:
				str = "vitoria:Você Venceu!!";
				break;
			default:
				str = "erro:Erro interno enquanto fazia uma movimento";
				break;
			}
			return str + '\n' + produzTabuleiro(jogadorIndex);
		}
	}

	/**
	 * Método que produz produz tabuleiros atualizados a serem enviados de volta,
	 * atualizando os jogadores com a situção atual do jogo.
	 * 
	 * @param jogadorIndex
	 * @return
	 */
	public String produzTabuleiro(int jogadorIndex) {
		char[][][] tabuleiros = jogo.getPlayerView(jogadorIndex);
		String resultado = "Seu tabuleiro: \n";
		// resultado += "\ta\tb\tc\td\te\tf\tg\th\ti\tj\n";// meu
		resultado += " abcdefghij\n";
		for (int i = 0; i < 10; i++) {
			resultado += i;
			for (int j = 0; j < 10; j++) {
				resultado += tabuleiros[0][j][i];
			}
			resultado += '\n';
		}
		// resultado += "tabuleiro inimigo: \n\ta\tb\tc\td\te\tf\tg\th\ti\tj\n";
		resultado += "tabuleiro inimigo: \nabcdefghij\n";
		for (int i = 0; i < 10; i++) {
			resultado += i;
			for (int j = 0; j < 10; j++) {
				resultado += tabuleiros[1][j][i];
			}
			resultado += '\n';
		}
		return resultado;
	}
}