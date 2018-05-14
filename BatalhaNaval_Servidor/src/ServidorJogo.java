import java.net.*;

/**
 * Classe que administra o Jogo do lado do servidor. Esta classe utiliza o
 * ServidorIO para mandar e receber dados via Socket, o BatalhaNaval para
 * persistir os dados do jogo, e armazena as informa��es dos jogadores.
 * 
 * @author Israel Deorce
 *
 */
public class ServidorJogo {
	private ServidorIO io; // Classe que faz o tratamento da conex�o - Entrada e Saida (IO)

	// Atributos do Jogador
	private InetAddress[] iPs; // Vetor de IPs Inet que guarda os ips dos clientes
	private String[] nomes; // Guarda o nome dos clientes
	private int[] portas; // Guarda a porta de acesso aos clientes
	private boolean[] naviosIDs; // boolean que informa se o jogador j� alocou os navios

	private BatalhaNaval jogo; // Classe que faz o tratamento do jogo no servidor
	private boolean jogoAtivo; // Variavel que indica se o jogo est� ativo (dois jogadores registados)

	/**
	 * Construtor que inicializa os atributos da classe
	 */
	public ServidorJogo(int porta) {
		io = new ServidorIO(porta);
		iPs = new InetAddress[2];
		nomes = new String[2];
		portas = new int[2];
		naviosIDs = new boolean[] { false, false };
		jogoAtivo = false;
	}

	/**
	 * M�todo principal que controla o recebimento de pacotes e manda processar
	 * 
	 */
	public void start() {
		while (true) {
			System.out.println("Aguardando recebimento de pacote...");
			DatagramPacket pacote = io.getPacote();
			if (pacote != null) {
				processaInput(pacote);
			}
		}
	}

	/**
	 * M�todo que recebe um pacote de Datagrama e processa as informa��es
	 * 
	 * @param pacote
	 */
	public void processaInput(DatagramPacket pacote) {
		// Pega dados e decompoe em nome (inf. cliente) e mensagem
		String mensagemPacote = new String(pacote.getData());
		String nome = mensagemPacote.substring(0, mensagemPacote.indexOf(':'));
		String mensagem = mensagemPacote.substring(mensagemPacote.indexOf(':') + 1, mensagemPacote.indexOf(0));

		System.out.println("Servidor processou input nome: " + nome + " e mensagem: " + mensagem);

		// transforma nome em index
		int indexJogador = getIndex(pacote.getAddress(), nome);

		// inicializa string que seria enviada ao cliente como dados
		String resultado = "";

		// Em caso de jogador n�o registrado no servidor, verifica e registra jogador
		if (indexJogador == -1) {
			if (mensagem.equals("join")) {
				indexJogador = criarIndexJogador(pacote.getAddress(), nome, pacote.getPort());
				if (indexJogador == -1) {
					resultado = "bad:Servidor cheio";
				} else {
					resultado = "good:Bem vindo ao servidor, " + nome + "!";
				}
				// Se os dois IPs estiverem registrados, inicia-se o jogo
				if (!(iPs[0] == null) && !(iPs[1] == null)) {
					jogoAtivo = true;
					jogo = new BatalhaNaval();
				}
			}

		} else { // Caso um jogador saia
			if (mensagem.equals("quit") && indexJogador != -1) {
				removeIndexJogador(indexJogador);
				jogoAtivo = false;
				naviosIDs = new boolean[] { false, false };
				io.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length,
						pacote.getAddress(), pacote.getPort()));
				if (iPs[(indexJogador + 1) % 2] == null) {
					resultado = "reset:Resetando servidor, jogador saiu";
					io.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length,
							iPs[(indexJogador + 1) % 2], portas[(indexJogador + 1) % 2]));
				}
				return;
			} else { // Caso o jogo ainda n�o esteja ativo, informa ao usuario, ou ent�o processa a
						// mensagem do usu�rio e envia resultado
				if (!jogoAtivo) {
					resultado = "bad:Aguardando jogadores adicionais...";
				} else {
					// A mensagem pode ser para alocar navios ou para dar um palpite
					resultado = processaMensagem(indexJogador, nome, mensagem);
				}
				// se for vitoria ou derrota atualiza resultado e finaliza o jogo
				if (resultado.substring(0, resultado.indexOf(":")).equals("win")) {
					String out = "lose:Voce perdeu!!";
					io.enviaPacote(new DatagramPacket(out.getBytes(), out.getBytes().length,
							iPs[(indexJogador + 1) % 2], portas[(indexJogador + 1) % 2]));
					iPs = new InetAddress[2];
					nomes = new String[2];
					portas = new int[2];
					naviosIDs = new boolean[] { false, false };
					jogoAtivo = false;
				}
			}
		}

		// Ao final do processo, constroi e envia um novo pacote de Datagrama passando o
		// endere�o e a porta do cliente disponivel no pacote
		io.enviaPacote(new DatagramPacket(resultado.getBytes(), resultado.getBytes().length, pacote.getAddress(),
				pacote.getPort()));
	}

	/**
	 * M�todo que cria um jogador e armazena as variaveis
	 * 
	 * @param ip
	 * @param nome
	 * @param porta
	 * @return
	 */
	public int criarIndexJogador(InetAddress ip, String nome, int porta) {
		for (int i = 0; i < iPs.length; i++) {
			if (iPs[i] == null) {
				iPs[i] = ip;
				nomes[i] = nome;
				portas[i] = porta;
				System.out.println("Jogador: " + nome + " entrou pelo IP: " + ip);
				return i;
			}
		}
		return -1;
	}

	/**
	 * M�todo que remove jogador de index referente
	 * 
	 * @param index
	 */
	public void removeIndexJogador(int index) {
		if (index != -1) {
			iPs[index] = null;
			nomes[index] = null;
			portas[index] = 0;
		}
	}

	/**
	 * M�todo que retorna index de um jogador especifico a partir do seu IP e nome,
	 * ou -1 em caso de jogador n�o encontrado
	 * 
	 * @param ip
	 * @param nome
	 * @return
	 */
	public int getIndex(InetAddress ip, String nome) {
		for (int i = 0; i < iPs.length; i++) {
			if (iPs[i] != null && iPs[i].equals(ip) && nome.equals(nomes[i]))
				return i;
		}
		return -1;
	}

	/**
	 * Metodo que recebe uma mensagem e processa
	 * 
	 * @param jogadorIndex
	 * @param nome
	 * @param mensagem
	 * @return
	 */
	public String processaMensagem(int indexJogador, String nome, String mensagem) {
		// Se o usu�rio ainda nao alocou os navios, aloca!
		if (naviosIDs[indexJogador] == false) {
			jogo.atribuiNavios(indexJogador, mensagem);
			naviosIDs[indexJogador] = true;
			return "good:Navios alocados com sucesso!";
		} else {

			// Se o outro jogador ainda nao alocou o navio, mantem standby
			if (!naviosIDs[(indexJogador + 1) % 2]) {
				return "bad:Aguardando outros jogadores alocarem seus navios";
			}

			// Converte as coordenadas do usuario para a posicao correta no plano
			System.out.println(mensagem);
			int x = mensagem.charAt(0) - 97; // 97 eh codigo para a letra a
			int y = mensagem.charAt(1) - 48; // 48 � para 0

			// Chama funcao que busca o resultado de um movimento enviando o jogador
			// e a posicao correspondente no plano
			int result = jogo.fazMovimento(indexJogador, x, y);
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
			return out + '\n' + fazTabuleiro(indexJogador);
		}
	}

	/**
	 * M�todo que produz tabuleiros atualizados a serem enviados de volta,
	 * atualizando os jogadores com a situ��o atual do jogo.
	 * 
	 * @param jogadorIndex
	 * @return
	 */
	public String fazTabuleiro(int indexJogador) {
		char[][][] tabuleiros = jogo.getVisaoJogador(indexJogador);
		String resultado = "Seu tabuleiro: \n";
		resultado += " abcdefghij\n";
		for (int i = 0; i < 10; i++) {
			resultado += i;
			for (int j = 0; j < 10; j++) {
				resultado += tabuleiros[0][j][i];
			}
			resultado += '\n';
		}
		resultado += "Tabuleiro inimigo: \nabcdefghij\n";
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