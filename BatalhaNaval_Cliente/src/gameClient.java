public class gameClient 
{
	public static void main(String args[]) throws Exception
	{
		String ip = "127.0.0.1";
		int port = 5556;
		
//		int port = 0;
//		if(args.length < 2)
//		{
//			System.err.println("Error: Please a server name or address and a port number");
//			System.exit(1);
//		}
//		try
//		{
//			port = Integer.parseInt(args[1]);
//		}
//		catch(NumberFormatException e)
//		{
//			System.err.println("Error: Argument could not be parsed into an integer, please include port number as second argument");
//			System.exit(1);
//		}
//		ClientGame game = new ClientGame(args[0], port);
		ClientGame game = new ClientGame(ip, port);
		game.start();
	}

}