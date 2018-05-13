import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ClientGame {
	ClientNetWorkIO IO;
	BufferedReader inFromUser; // Buffered Reader para leitura do input do usuário
	public String username;
	boolean activeGame;

	ClientGame(String address, int port) {
		IO = new ClientNetWorkIO(address, port);
		inFromUser = new BufferedReader(new InputStreamReader(System.in));

	}

	void start() {
		username = CreateName();
		System.out.println("Username is: " + username);
		IO.sendMessage(username + ":join");
		if (IO.getMessage().equals("Server full")) {
			System.out.println("Server full, try again later");
			System.exit(0);
		} else {
			startGame();
		}
		IO.sendMessage(username + ":quit");
	}

	private String CreateName() {
		while (true) {
			System.out.print("Please enter a user name: ");
			try {
				return inFromUser.readLine();
			} catch (IOException e) {
				System.err.println("Error: Invalid text entry");
			}
		}
	}

	private void startGame() {
		activeGame = true;
		IO.sendMessage(username + ":" + getShips());
		ProcessCommand(IO.getMessage());
		while (activeGame) {
			IO.sendMessage(username + ":" + getLine());
			System.out.println("sent package. waiting for replay...");
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
				System.out.println("Please entered a letter and number combination");
				command = inFromUser.readLine();
			} catch (IOException e) {
				System.err.println("Error:Invalid text entry");
			}
			if (command.matches("[a-jA-J]{1}[0-9]{1}")) {
				return command.toLowerCase();
			}
			System.out.println("Try again!");
		}
	}

	/**
	 * Método que cria o tabuleiro do jogador com os navios e converte a informação
	 * para String que será enviada via Socket
	 * 
	 * @return
	 */
	private String getShips() {
		char[][] board = new char[10][10];
		for (int i = 0; i < 10; i++) {
			Arrays.fill(board[i], '-');
		}
		System.out.println("Please enter the location of ships you wish to place");
		String[] ships = new String[5];
		int currentShip = 0;
		int length = 0;
		while (currentShip != 5) {
			switch (currentShip) {
			case 0:
				System.out.println("Placeing the carrier(5)!");
				length = 5;
				break;
			case 1:
				System.out.println("Placeing the battleship(4)!");
				length = 4;
				break;
			case 2:
				System.out.println("Placeing the destoryer(3)!");
				length = 3;
				break;
			case 3:
				System.out.println("Placeing the submarine(3)!");
				length = 3;
				break;
			case 4:
				System.out.println("Placeing the patrol boat(2)!");
				length = 2;
				break;
			}
			DisplayShips(board);
			System.out.println("Enter the first corner of the ship");
			String shipLoc = getLine();
			if (checkLoc(shipLoc, board)) {
				System.out.println("Location invalid, it conflicts with another ship");
				continue;
			}
			System.out.println("Enter the last square of the ship's location");
			String endLoc = getLine();
			if (shipLoc.charAt(0) == endLoc.charAt(0)) {
				if (shipLoc.charAt(1) - endLoc.charAt(1) < 0) {
					if (Math.abs(shipLoc.charAt(1) - endLoc.charAt(1) - 1) != length) {
						System.out.println("Location invalid, incorrect length specified");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						shipLoc += shipLoc.charAt(0);
						shipLoc += (char) (shipLoc.charAt(1) + j + 1);
					}
				} else {
					if (Math.abs(shipLoc.charAt(1) - endLoc.charAt(1) + 1) != length) {
						System.out.println("Location invalid, incorrect length specified");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						shipLoc += shipLoc.charAt(0);
						shipLoc += (char) (endLoc.charAt(1) + j + 1);
					}
				}
				shipLoc += endLoc;
				if (isColliding(shipLoc, board)) {
					System.out.println("Location invalid, two or more ships are colliding");
					continue;
				}
				ships[currentShip] = shipLoc;
				for (int i = 0; i < shipLoc.length(); i += 2) {
					String loc = shipLoc.substring(i, i + 2);
					board[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				currentShip++;
				continue;

			}
			if (shipLoc.charAt(1) == endLoc.charAt(1)) {
				if (shipLoc.charAt(0) - endLoc.charAt(0) < 0) {
					if (Math.abs(shipLoc.charAt(0) - endLoc.charAt(0) - 1) != length) {
						System.out.println("Location invalid, incorrect length specified");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						shipLoc += (char) (shipLoc.charAt(0) + j + 1);
						shipLoc += shipLoc.charAt(1);
					}
				} else {
					if (Math.abs(shipLoc.charAt(0) - endLoc.charAt(0) + 1) != length) {
						System.out.println("Location invalid, incorrect length specified");
						continue;
					}
					for (int j = 0; j < length - 2; j++) {
						shipLoc += (char) (endLoc.charAt(0) + j + 1);
						shipLoc += shipLoc.charAt(1);
					}
				}
				shipLoc += endLoc;
				if (isColliding(shipLoc, board)) {
					System.out.println("Location invalid, two or more ships are colliding");
					continue;
				}
				ships[currentShip] = shipLoc;
				for (int i = 0; i < shipLoc.length(); i += 2) {
					String loc = shipLoc.substring(i, i + 2);
					board[loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
				}
				currentShip++;
				continue;
			}
			System.out.println("Location invalid, ships must be placed in streight lines");
		}
		return ships[0] + ships[1] + ships[2] + ships[3] + ships[4];
	}

	private boolean checkLoc(String Loc, char[][] board) {
		return board[Loc.charAt(0) - 97][Loc.charAt(1) - 48] == 'S';
	}

	private boolean isColliding(String shipLoc, char[][] board) {
		for (int i = 0; i < shipLoc.length(); i += 2) {
			String loc = shipLoc.substring(i, i + 2);
			if (checkLoc(loc, board)) {
				return true;
			}
		}
		return false;
	}

	private void DisplayShips(char[][] board) {
		String out = " abcdefghij\n";
		for (int i = 0; i < 10; i++) {
			out += i;
			for (int j = 0; j < 10; j++) {
				out += board[j][i];
			}
			out += '\n';
		}
		System.out.println(out);
	}

	private void ProcessCommand(String Command) {
		String internal = Command.substring(0, Command.indexOf(":"));
		String external = Command.substring(Command.indexOf(":") + 1, Command.indexOf(0));
		if (internal.equals("win") || internal.equals("lose") || internal.equals("reset")) {
			activeGame = false;
		}
		System.out.println(external);
	}
}