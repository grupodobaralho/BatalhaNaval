import java.net.*;

public class ServerGame {
	ServerNetWorkIO IO; // Classe que faz o tratamento da conexão - Entrada e Saida (IO)
	private InetAddress[] IPAddress; // Vetor de IPs Inet que guarda os ips dos clientes
	private String[] names; // Guarda o nome dos clientes
	private int ports[]; // Guarda a porta de acesso aos clientes
	private JShipGame game; // Classe que faz o tratamento do jogo no servidor
	boolean shipIDs[];
	boolean activeGame; // Variavel que indica se o jogo está ativo

	public ServerGame(int port) {
		IO = new ServerNetWorkIO(port);
		IPAddress = new InetAddress[2];
		names = new String[2];
		ports = new int[2];
		shipIDs = new boolean[] { false, false };
		activeGame = false;
	}

	/**
	 * Método principal que controla o recebimento de pacotes e manda processar
	 * 
	 */
	void start() {
		while (true) {
			System.out.println("Aguardando recebimento de pacotes...");
			DatagramPacket packet = IO.getPacket();
			if (packet != null) {
				ProccessInput(packet);
			}
		}
	}

	/**
	 * 
	 * @param pacote
	 */
	public void ProccessInput(DatagramPacket packet) {
		String packetMessage = new String(packet.getData());
		String name = packetMessage.substring(0, packetMessage.indexOf(':'));
		String message = packetMessage.substring(packetMessage.indexOf(':') + 1, packetMessage.indexOf(0));
		int PlayerIndex = GetIndex(packet.getAddress(), name);
		String result = "";

		// Em caso de jogador não registrado no servidor, verifica e cria-se jogador
		if (PlayerIndex == -1) {
			if (message.equals("join")) {
				PlayerIndex = CreatePlayerindex(packet.getAddress(), name, packet.getPort());
				if (PlayerIndex == -1) {
					result = "bad:Servidor cheio";
				} else {
					result = "good:Bem vindo ao servidor, " + name + "!";
				}
				// Se os dois IPs estiverem registrados, inicia-se o jogo
				if (!(IPAddress[0] == null) && !(IPAddress[1] == null)) {
					activeGame = true;
					game = new JShipGame();
				}
			}

		} else {  //Caso um jogador saia
			if (message.equals("quit") && PlayerIndex != -1) {
				RemovePlayerindex(PlayerIndex);
				activeGame = false;
				shipIDs = new boolean[] { false, false };
				IO.sendPacket(new DatagramPacket(result.getBytes(), result.getBytes().length, packet.getAddress(),
						packet.getPort()));
				if (IPAddress[(PlayerIndex + 1) % 2] == null) {
					result = "reset:Resetando servidor, jogador saiu";
					IO.sendPacket(new DatagramPacket(result.getBytes(), result.getBytes().length,
							IPAddress[(PlayerIndex + 1) % 2], ports[(PlayerIndex + 1) % 2]));
				}
				return;
			} else {
				if (!activeGame) {
					result = "bad:Aguardando jogadores adicionais...";
				} else {
					result = processMove(PlayerIndex, name, message);
				}
				if (result.substring(0, result.indexOf(":")).equals("win")) {
					String out = "lose:Voce perdeu!!";
					IO.sendPacket(new DatagramPacket(out.getBytes(), out.getBytes().length,
							IPAddress[(PlayerIndex + 1) % 2], ports[(PlayerIndex + 1) % 2]));
					IPAddress = new InetAddress[2];
					names = new String[2];
					ports = new int[2];
					shipIDs = new boolean[] { false, false };
					activeGame = false;
				}
			}
		}
		IO.sendPacket(
				new DatagramPacket(result.getBytes(), result.getBytes().length, packet.getAddress(), packet.getPort()));
	}

	/**
	 * Método que cria um jogador e armazena as variaveis
	 * 
	 * @param IP
	 * @param nome
	 * @param porta
	 * @return
	 */
	private int CreatePlayerindex(InetAddress IP, String name, int port) {
		for (int i = 0; i < IPAddress.length; i++) {
			if (IPAddress[i] == null) {
				IPAddress[i] = IP;
				names[i] = name;
				ports[i] = port;
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
	private void RemovePlayerindex(int Index) {
		if (Index != -1) {
			IPAddress[Index] = null;
			names[Index] = null;
			ports[Index] = 0;
		}
	}

	/**
	 * Método que retorna index de um jogador especifico a partir do seu IP e nome
	 * 
	 * @param IP
	 * @param nome
	 * @return
	 */
	private int GetIndex(InetAddress IP, String name) {
		for (int i = 0; i < IPAddress.length; i++) {
			if (IPAddress[i] != null && IPAddress[i].equals(IP) && name.equals(names[i]))
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
		if (shipIDs[PlayerIndex] == false) {
			game.assignShips(PlayerIndex, move);
			shipIDs[PlayerIndex] = true;
			return "good:Navios alocados com sucesso!";
		} else {
			if (!shipIDs[(PlayerIndex + 1) % 2]) {
				return "bad:Aguardando outros jogadores alocarem seus navios";
			}
			int x = move.charAt(0) - 97;
			int y = move.charAt(1) - 48;
			int result = game.MakeMove(PlayerIndex, x, y);
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
			return out + '\n' + ProduceBoard(PlayerIndex);
		}
	}

	/**
	 * Método que produz produz tabuleiros atualizados a serem enviados de volta,
	 * atualizando os jogadores com a situção atual do jogo.
	 * 
	 * @param jogadorIndex
	 * @return
	 */
	public String ProduceBoard(int PlayerIndex) {
		char[][][] boards = game.getPlayerView(PlayerIndex);
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