/*
 * http://www1.chapman.edu/~shaff106/cpsc353/game.html
 * http://www1.chapman.edu/~shaff106/cpsc353/gameServer.java
 */
public class gameServer {
	public static void main(String args[]) throws Exception {
		int port = 5556;
//		if (args.length < 1) {
//			System.err.println("Error: Please include a port number");
//			System.exit(1);
//		}
		try {
			//port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Erro: Argumento não pode ser convertido em inteiro");
			System.exit(1);
		}
		ServerGame server = new ServerGame(port);
		server.start();
	}
}