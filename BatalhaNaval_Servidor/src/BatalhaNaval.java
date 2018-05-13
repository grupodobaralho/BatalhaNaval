import java.util.Arrays;

public class BatalhaNaval {
	char[][][] PlayerBoards;
	final static char empty = '-', miss = 'O', hit = 'X', occupied = 'S';
	int ActivePlayer;

	private class coordinate {
		public int X;
		public int Y;

		coordinate(int x, int y) {
			X = x;
			Y = y;
		}
	}

	BatalhaNaval() {
		PlayerBoards = new char[2][10][10];
		for (int i = 0; i < 10; i++) {
			Arrays.fill(PlayerBoards[0][i], empty);
			Arrays.fill(PlayerBoards[1][i], empty);
		}
		ActivePlayer = 0;
	}

	void assignShips(int PlayerIndex, String ships) {
		for (int i = 0; i < ships.length(); i += 2) {
			String loc = ships.substring(i, i + 2);
			PlayerBoards[PlayerIndex][loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
		}
	}

	int MakeMove(int PlayerIndex, int x, int y) {
		if (PlayerIndex != ActivePlayer) {
			return -2;
		}
		coordinate move = new coordinate(x, y);
		int result = Attack(PlayerIndex, move);
		switch (result) {
		case -1:
			return -1;
		case 0:
			ActivePlayer = (PlayerIndex + 1) % 2;
			return 0;
		case 1:
			if (checkWinner(ActivePlayer)) {
				return 2;
			}
			ActivePlayer = (PlayerIndex + 1) % 2;
			return 1;
		default:
			return -3;
		}
	}

	private int Attack(int PlayerIndex, coordinate c) {
		char spot = PlayerBoards[(PlayerIndex + 1) % 2][c.X][c.Y];
		switch (spot) {
		case empty:
			PlayerBoards[(PlayerIndex + 1) % 2][c.X][c.Y] = miss;
			return 0;
		case occupied:
			PlayerBoards[(PlayerIndex + 1) % 2][c.X][c.Y] = hit;
			return 1;
		default:
			return -1;
		}
	}

	private boolean checkWinner(int PlayerIndex) {
		boolean won = true;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (PlayerBoards[(PlayerIndex + 1) % 2][i][j] == occupied) {
					won = false;
					break;
				}
			}
		}
		return won;
	}

	private char[][] ConvertToEnemyView(char[][] board) {
		char[][] internal = new char[10][10];
		for (int i = 0; i < 10; i++) {
			internal[i] = (char[]) board[i].clone();
		}
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (board[j][i] == occupied) {
					internal[j][i] = empty;
				}
			}
		}
		return internal;
	}

	public char[][][] getPlayerView(int PlayerIndex) {
		char[][][] results = new char[2][10][10];
		results[0] = PlayerBoards[PlayerIndex];
		results[1] = ConvertToEnemyView(PlayerBoards[(PlayerIndex + 1) % 2]);
		return results;
	}
}