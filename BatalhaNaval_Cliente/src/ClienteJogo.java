import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ClienteJogo {
	ClienteIO io;
	BufferedReader inUsuario; //Buffered Reader para leitura do input do usuário
	public String username;
	boolean jogoAtivo;
	ClienteJogo(String address, int port)
	{
		io = new ClienteIO(address, port);
		inUsuario = new BufferedReader(new InputStreamReader(System.in));

	}

	void start()
	{
		username = CreateName();
		System.out.println("Usuario eh: "+username);
		io.sendMensagem(username+":join");
		if(io.getMensagem().equals("Servidor cheio"))
		{
			System.out.println("Servidor cheio, tente novamente mais tarde");
			System.exit(0);
		}
		else
		{
			startGame();
		}
		io.sendMensagem(username+":quit");
	}

	private String CreateName()
	{
		while(true)
		{
			System.out.print("Por favor digite um nome de usuario: ");
			try
			{
				return inUsuario.readLine();
			}
			catch(IOException e)
			{
				System.err.println("Error: Entrada de texto invalida");
			}
		}
	}

	private void startGame()
	{
			jogoAtivo = true;
			io.sendMensagem(username+":"+getShips());
			ProcessCommand(io.getMensagem());
			while(jogoAtivo)
			{
				io.sendMensagem(username+":"+getLine());
				System.out.println("Pacote enviado... Aguardando Replay...");
				ProcessCommand(io.getMensagem());
			}
	}

	/**
	 * Função que aguarda o recebimento de uma posição: "LETRA+NÚMERO" de um usuário
	 * cliente, valida e retorna o comando referente.
	 * 
	 * @return
	 */
	private String getLine()
	{
		String command = "";
		while(true)
		{
			try
			{
				System.out.println("Por favor digite uma combinação de letra e número. EX: 'a0'");
				command = inUsuario.readLine();
			}
			catch(IOException e)
			{
				System.err.println("Error: Entrada de texto invalida");
			}
			if(command.matches("[a-jA-J]{1}[0-9]{1}"))
			{
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
	private String getShips()
	{
		char[][] board = new char[10][10];
		for(int i=0; i < 10; i++)
		{
			Arrays.fill(board[i], '-');
		}
		System.out.println("Por favor escolha a localizacao dos navios que voce deseja alocar");
		String[] ships = new String[5];
		int currentShip = 0;
		int length = 0;
		while(currentShip != 5)
		{		
			switch(currentShip)
			{
				case 0:
					System.out.println("Alocando porta-avioes(5)!"); length = 5; break;
				case 1:
					System.out.println("Alocando navio-tanque(4)!"); length = 4; break;
				case 2:
					System.out.println("Alocando contratorpedeiros(3)!"); length = 3; break;
				case 3:
					System.out.println("Alocando submarinos(3)!"); length = 3; break;
				case 4:
					System.out.println("Alocando patrulhador(2)!"); length = 2; break;
			}
			DisplayShips(board);
			System.out.println("Informe o primeiro quadrante do navio");
			String shipLoc = getLine();
			if(checkLoc(shipLoc, board))
			{
				System.out.println("Local invalido, faz conflito com outro navio");
				continue;
			}
			System.out.println("Informe o ultimo quadrante da localizacao do navio");
			String endLoc = getLine();
			if(shipLoc.charAt(0) == endLoc.charAt(0))
			{
				if(shipLoc.charAt(1) - endLoc.charAt(1) < 0)
				{
					if(Math.abs(shipLoc.charAt(1) - endLoc.charAt(1)-1) != length)
					{
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for(int j = 0; j < length-2; j++)
					{
						shipLoc += shipLoc.charAt(0);
						shipLoc += (char)(shipLoc.charAt(1)+j+1);
					}
				}
				else
				{
					if(Math.abs(shipLoc.charAt(1) - endLoc.charAt(1)+1) != length)
					{
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for(int j = 0; j < length-2; j++)
					{
						shipLoc += shipLoc.charAt(0);
						shipLoc += (char)(endLoc.charAt(1)+j+1);
					}
				}
				shipLoc += endLoc;
				if(isColliding(shipLoc, board))
				{
					System.out.println("Local invalido, dois ou mais navios estao colidindo");
					continue;
				}
				ships[currentShip] = shipLoc;
				for(int i = 0; i < shipLoc.length(); i+=2)
				{
					String loc = shipLoc.substring(i, i+2);
					board[loc.charAt(0)-97][loc.charAt(1)-48] = 'S';
				}
				currentShip++;
				continue;
				
			}
			if(shipLoc.charAt(1) == endLoc.charAt(1))
			{
				if(shipLoc.charAt(0) - endLoc.charAt(0) < 0)
				{
					if(Math.abs(shipLoc.charAt(0) - endLoc.charAt(0)-1) != length)
					{
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for(int j = 0; j < length-2; j++)
					{
						shipLoc += (char)(shipLoc.charAt(0)+j+1); 
						shipLoc += shipLoc.charAt(1);
					}
				}
				else
				{
					if(Math.abs(shipLoc.charAt(0) - endLoc.charAt(0)+1) != length)
					{
						System.out.println("Local invalido, tamanho especificado invalido");
						continue;
					}
					for(int j = 0; j < length-2; j++)
					{
						shipLoc += (char)(endLoc.charAt(0)+j+1);
						shipLoc += shipLoc.charAt(1);
					}
				}
				shipLoc += endLoc;
				if(isColliding(shipLoc, board))
				{
					System.out.println("Local invalido, dois ou mais navios estao colidindo");
					continue;
				}
				ships[currentShip] = shipLoc;
				for(int i = 0; i < shipLoc.length(); i += 2)
				{
					String loc = shipLoc.substring(i, i+2);
					board[loc.charAt(0)-97][loc.charAt(1)-48] = 'S';
				}
				currentShip++;
				continue;		
			}
			System.out.println("Local invalido, navios devem ser posicionados em linha reta");
		}
		return ships[0]+ships[1]+ships[2]+ships[3]+ships[4];
	}

	private boolean checkLoc(String Loc, char[][] board)
	{
		return board[Loc.charAt(0)-97][Loc.charAt(1)-48] == 'S';
	}

	private boolean isColliding(String shipLoc, char[][] board)
	{
		for(int i = 0; i < shipLoc.length(); i += 2)
		{
			String loc = shipLoc.substring(i, i+2);
			if(checkLoc(loc, board))
			{
				return true;
			}
		}
		return false;
	}

	private void DisplayShips(char[][] board)
	{
		String out = " abcdefghij\n";
		for(int i = 0; i < 10; i++)
		{
			out += i;
			for(int j= 0; j < 10; j++)
			{
				out += board[j][i];
			}
			out += '\n';
		}
		System.out.println(out);
	}

	private void ProcessCommand(String Command)
	{
		String internal = Command.substring(0, Command.indexOf(":"));
		String external = Command.substring(Command.indexOf(":")+1, Command.indexOf(0));
		if(internal.equals("win")||internal.equals("lose")||internal.equals("reset"))
		{
			jogoAtivo = false;
		}
		System.out.println(external);
	}
}