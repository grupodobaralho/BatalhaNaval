import java.io.*;
import java.util.Arrays;

public class ClientGame {
	ClientNetWorkIO IO;
	BufferedReader inFromUser;
	public String usuario;
	boolean activeGame;

	ClientGame(String address, int port) {
		IO = new ClientNetWorkIO(address, port);
		inFromUser = new BufferedReader(new InputStreamReader(System.in));

	}

	void start() {
		usuario = CreateName();
		System.out.println("Usuário é: " + usuario);
		IO.sendMessage(usuario + ":join");
		if (IO.getMessage().equals("Servidor cheio")) {
			System.out.println("Servidor cheio, tente novamente mais tarde");
			System.exit(0);
		} else {
			startGame();
		}
		IO.sendMessage(usuario + ":sair");
	}

	private String CreateName() {
		while (true) {
			System.out.print("Por favor, digite um nome de usuário: ");
			try {
				return inFromUser.readLine();
			} catch (IOException e) {
				System.err.println("Erro: Entrada de texto inválida");
			}
		}
	}

	private void startGame() {
		activeGame = true;
		IO.sendMessage(usuario + ":" + getNavios());
		ProcessCommand(IO.getMessage());
		while (activeGame) {
			IO.sendMessage(usuario + ":" + getLine());
			System.out.println("Pacote enviado. Aguardando replay...");
			ProcessCommand(IO.getMessage());
		}
	}

	/**
	 * Função que aguarda o recebimento de uma posição: "LETRA+NÚMERO" de um usuário
	 * cliente, valida e retorna o comando referente.
	 * 
	 * @return
	 */
	private String getLine() {
		String command = "";
		while (true) {
			try {
				System.out.println("Por favor digite uma combinação de letra e número. EX: 'a0'");
				command = inFromUser.readLine();
			} catch (IOException e) {
				System.err.println("Erro: Entrada de texto inválida");
			}
			if (command.matches("[a-jA-J]{1}[0-9]{1}")) {
				return command.toLowerCase();
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
	private String getNavios() {
		char[][] tabuleiro = new char[10][10];
		for (int i = 0; i < 10; i++) {
			Arrays.fill(tabuleiro[i], '-');
		}
		System.out.println("Por favor, informe aonde deseja posicionar os navios...");
		String[] navios = new String[5];
		int currentShip = 0;
		int length = 0;
		while (currentShip != 5) {
			switch (currentShip) {
			case 0:
				System.out.println("Alocando porta-aviões(5)!");
				length = 5;
				break;
			case 1:
				System.out.println("Alocando navio-tanque(4)!");
				length = 4;
				break;
			case 2:
				System.out.println("Alocando contratorpedeiros(3)!");
				length = 3;
				break;
			case 3:
				System.out.println("Alocando submarinos(3)!");
				length = 3;
				break;
			case 4:
				System.out.println("Alocando patrulhador(2)!");
				length = 2;
				break;
			}
			apresentaNavios(tabuleiro);
			System.out.println("Informe o primeiro quadrante do navio");
			String navioLoc = getLine();
			if (checkLoc(navioLoc, tabuleiro)) {
				System.out.println("Local inválido, faz conflito com outro navio");
				continue;
			}
			System.out.println("Informe o último quadrante do navio");
			String endLoc = getLine();
			if (navioLoc.charAt(0) == endLoc.charAt(0)) {
				if (navioLoc.charAt(1) - endLoc.charAt(1) < 0) {
					if (Math.abs(navioLoc.charAt(1) - endLoc.charAt(1) - 1) != length) {
						System.out.println("Local inválido, tamanho especificado inválido");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						navioLoc += navioLoc.charAt(0);
						navioLoc += (char) (navioLoc.charAt(1) + j + 1);
					}
				} else {
					if (Math.abs(navioLoc.charAt(1) - endLoc.charAt(1) + 1) != length) {
						System.out.println("Local inválido, faz conflito com outro navio");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						navioLoc += navioLoc.charAt(0);
						navioLoc += (char) (endLoc.charAt(1) + j + 1);
					}
				}
				navioLoc += endLoc;
				if (isColidindo(navioLoc, tabuleiro)) {
					System.out.println("Local inválido, dois ou mais navios estão colidindo");
					continue;
				}
				navios[currentShip] = navioLoc;
				for (int i = 0; i < navioLoc.length(); i += 2) {
					String loc = navioLoc.substring(i, i + 2);
					tabuleiro[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				currentShip++;
				continue;

			}
			if (navioLoc.charAt(1) == endLoc.charAt(1)) {
				if (navioLoc.charAt(0) - endLoc.charAt(0) < 0) {
					if (Math.abs(navioLoc.charAt(0) - endLoc.charAt(0) - 1) != length) {
						System.out.println("Local inválido, tamanho especificado inválido");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						navioLoc += (char) (navioLoc.charAt(0) + j + 1);
						navioLoc += navioLoc.charAt(1);
					}
				} else {
					if (Math.abs(navioLoc.charAt(0) - endLoc.charAt(0) + 1) != length) {
						System.out.println("Local inválido, tamanho especificado inválido");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						navioLoc += (char) (endLoc.charAt(0) + j + 1);
						navioLoc += navioLoc.charAt(1);
					}
				}
				navioLoc += endLoc;
				if (isColidindo(navioLoc, tabuleiro)) {
					System.out.println("Local inválido, dois ou mais navios estão colidindo");
					continue;
				}
				navios[currentShip] = navioLoc;
				for (int i = 0; i < navioLoc.length(); i += 2) {
					String loc = navioLoc.substring(i, i + 2);
					tabuleiro[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				currentShip++;
				continue;
			}
			System.out.println("Local inválido, navios devem ser posicionados de forma reta");
		}
		return navios[0] + navios[1] + navios[2] + navios[3] + navios[4];
	}

	private boolean checkLoc(String Loc, char[][] tabuleiro) {
		return tabuleiro[Loc.charAt(0) - 97][Loc.charAt(1) - 48] == 'S';
	}

	private boolean isColidindo(String navioLoc, char[][] tabuleiro) {
		for (int i = 0; i < navioLoc.length(); i += 2) {
			String loc = navioLoc.substring(i, i + 2);
			if (checkLoc(loc, tabuleiro)) {
				return true;
			}
		}
		return false;
	}

	private void apresentaNavios(char[][] tabuleiro) {
		String str = "\ta\tb\tc\td\te\tf\tg\th\ti\tj\n";
		for (int i = 0; i < 10; i++) {
			str += i + "\t";
			for (int j = 0; j < 10; j++) {
				str += tabuleiro[j][i] + "\t";
			}
			str += '\n';
		}
		System.out.println(str);
	}

	private void ProcessCommand(String Command) {
		String interno = Command.substring(0, Command.indexOf(":"));
		String externo = Command.substring(Command.indexOf(":") + 1, Command.indexOf(0));
		if (interno.equals("vitoria") || interno.equals("derrota") || interno.equals("reseta")) {
			activeGame = false;
		}
		System.out.println(externo);
	}
}
