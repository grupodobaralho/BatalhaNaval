import java.net.*;

public class ServidorJogo {
	ServidorIO io; // Classe que faz o tratamento da conexão - Entrada e Saida (IO)
	private InetAddress[] iPs; // Vetor de IPs Inet que guarda os ips dos clientes
	private String[] names; // Guarda o nome dos clientes
	private int[] portas; // Guarda a porta de acesso aos clientes
	private BatalhaNaval jogo; // Classe que faz o tratamento do jogo no servidor
	boolean[] naviosIDs; // boolean que informa se o jogador já alocou os navios
	boolean jogoAtivo; // Variavel que indica se o jogo está ativo

	public ServidorJogo(int port) {
		io = new ServidorIO(port);
		iPs = new InetAddress[2];
		names = new String[2];
		portas = new int[2];
		naviosIDs = new boolean[] { false, false };
		jogoAtivo = false;
	}

	/**
	 * Método principal que controla o recebimento de pacotes e manda processar
	 * 
	 */
	void start() {
		while (true) {
			System.out.println("Aguardando recebimento de pacotes...");
			DatagramPacket pacote = io.getPacote();
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
		int indexJogador = getIndex(pacote.getAddress(), nome);
		String resultado = "";
		System.out.println("mensagem é1: " + mensagem);
		// Em caso de jogador não registrado no servidor, verifica e cria-se jogador
		if (indexJogador == -1) {
			System.out.println("mensagem é2: " + mensagem);
			if (mensagem.equals("join")) {
				indexJogador = criarIndexPlayer(pacote.getAddress(), nome, pacote.getPort());
				if (indexJogador == -1) {
					resultado = "bad:Servidor cheio";
				} else {
					resultado = "good:Bem vindo ao servidor, " + nome + "!";
					System.out.println("mensagem é2.5: " + mensagem);
				}
				// Se os dois IPs estiverem registrados, inicia-se o jogo
				if (!(iPs[0] == null) && !(iPs[1] == null)) {
					jogoAtivo = true;
					jogo = new BatalhaNaval();
				}
			}

		} else { // Caso um jogador saia
			if (mensagem.equals("quit") && indexJogador != -1) {
				System.out.println("mensagem é3: " + mensagem);
				removeIndexPlayer(indexJogador);
				jogoAtivo = false;
				naviosIDs = new boolean[] { false, false };
				io.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length, pacote.getAddress(),
						pacote.getPort()));
				if (iPs[(indexJogador + 1) % 2] == null) {
					resultado = "reset:Resetando servidor, jogador saiu";
					io.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length,
							iPs[(indexJogador + 1) % 2], portas[(indexJogador + 1) % 2]));
				}
				return;
			} else {
				if (!jogoAtivo) {
					resultado = "bad:Aguardando jogadores adicionais...";
				} else {
					resultado = processMove(indexJogador, nome, mensagem);
				}
				if (resultado.substring(0, resultado.indexOf(":")).equals("win")) {
					String out = "lose:Voce perdeu!!";
					io.enviaPacote(new DatagramPacket(out.getBytes(), out.getBytes().length,
							iPs[(indexJogador + 1) % 2], portas[(indexJogador + 1) % 2]));
					iPs = new InetAddress[2];
					names = new String[2];
					portas = new int[2];
					naviosIDs = new boolean[] { false, false };
					jogoAtivo = false;
				}
			}
		}
		io.enviaPacote(
				new DatagramPacket(resultado.getBytes(), resultado.getBytes().length, pacote.getAddress(), pacote.getPort()));
	}

	/**
	 * Método que cria um jogador e armazena as variaveis
	 * 
	 * @param IP
	 * @param nome
	 * @param porta
	 * @return
	 */
	private int criarIndexPlayer(InetAddress IP, String name, int port) {
		for (int i = 0; i < iPs.length; i++) {
			if (iPs[i] == null) {
				iPs[i] = IP;
				names[i] = name;
				portas[i] = port;
				System.out.println("Jogador: " + name + " entrou pelo IP: " + IP);
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
	private void removeIndexPlayer(int Index) {
		if (Index != -1) {
			iPs[Index] = null;
			names[Index] = null;
			portas[Index] = 0;
		}
	}

	/**
	 * Método que retorna index de um jogador especifico a partir do seu IP e nome
	 * 
	 * @param IP
	 * @param nome
	 * @return
	 */
	private int getIndex(InetAddress IP, String name) {
		for (int i = 0; i < iPs.length; i++) {
			if (iPs[i] != null && iPs[i].equals(IP) && name.equals(names[i]))
				return i;
		}
		return -1;
	}

	/**
	 * Metodo que recebe um movimento e processa
	 * 
	 * @param jogadorIndex
	 * @param nome
	 * @param movimento
	 * @return
	 */
	private String processMove(int PlayerIndex, String name, String move) {
		// Se o usuário ainda nao alocou os navios, aloca!
		if (naviosIDs[PlayerIndex] == false) {
			jogo.assignShips(PlayerIndex, move);
			naviosIDs[PlayerIndex] = true;
			return "good:Navios alocados com sucesso!";
		} else {
			
			// Se o outro jogador ainda nao alocou o navio, mantem standby
			if (!naviosIDs[(PlayerIndex + 1) % 2]) {
				return "bad:Aguardando outros jogadores alocarem seus navios";
			}
			
			// Converte as coordenadas do usuario para a posicao correta no plano
			System.out.println(move);
			int x = move.charAt(0) - 97; //97 eh codigo para a letra a
			int y = move.charAt(1) - 48; //
			
			// Chama funcao que busca o resultado de um movimento enviando o jogador
			// e a posicao correspondente no plano
			int result = jogo.MakeMove(PlayerIndex, x, y);
			String out;
			switch (result) {
			case -2:
				out = "bad:Ainda nao eh o seu turno!";
				break;
			case -1:
				out = "bad:Voce ja atacou este local!";
				break;
			case 0:
				out = "good:MISS!!";
				break;
			case 1:
				out = "good:HIT!!";
				break;
			case 2:
				out = "win:You Win!!";
				break;
			default:
				out = "error:Erro interno ao fazer o movimento! caiu no default";
				break;
			}
			return out + '\n' + fazTabuleiro(PlayerIndex);
		}
	}

	/**
	 * Método que produz tabuleiros atualizados a serem enviados de volta,
	 * atualizando os jogadores com a situção atual do jogo.
	 * 
	 * @param jogadorIndex
	 * @return
	 */
	public String fazTabuleiro(int PlayerIndex) {
		char[][][] boards = jogo.getPlayerView(PlayerIndex);
		String result = "Seu tabuleiro: \n";
		result += " abcdefghij\n";
		for (int i = 0; i < 10; i++) {
			result += i;
			for (int j = 0; j < 10; j++) {
				result += boards[0][j][i];
			}
			result += '\n';
		}
		result += "Tabuleiro inimigo: \nabcdefghij\n";
		for (int i = 0; i < 10; i++) {
			result += i;
			for (int j = 0; j < 10; j++) {
				result += boards[1][j][i];
			}
			result += '\n';
		}
		return result;
	}
}