import java.util.Arrays;

public class JShipGame {
	char[][][] tabuleirosJogadores;
	final static char vazio = '-', falha = 'O', acerto = 'X', ocupado = 'S';
	int jogadorAtivo;

	private class coordenada {
		public int X;
		public int Y;

		coordenada(int x, int y) {
			X = x;
			Y = y;
		}
	}

	JShipGame() {
		tabuleirosJogadores = new char[2][10][10];
		for (int i = 0; i < 10; i++) {
			Arrays.fill(tabuleirosJogadores[0][i], vazio);
			Arrays.fill(tabuleirosJogadores[1][i], vazio);
		}
		jogadorAtivo = 0;
	}

	void atribuirNavios(int indexJogador, String navios) {
		for (int i = 0; i < navios.length(); i += 2) {
			String loc = navios.substring(i, i + 2);
			tabuleirosJogadores[indexJogador][loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
		}
	}

	int MakeMove(int indexJogador, int x, int y) {
		if (indexJogador != jogadorAtivo) {
			return -2;
		}
		coordenada move = new coordenada(x, y);
		int result = atacar(indexJogador, move);
		switch (result) {
		case -1:
			return -1;
		case 0:
			jogadorAtivo = (indexJogador + 1) % 2;
			return 0;
		case 1:
			if (checarVencedor(jogadorAtivo)) {
				return 2;
			}
			jogadorAtivo = (indexJogador + 1) % 2;
			return 1;
		default:
			return -3;
		}
	}

	private int atacar(int indexJogador, coordenada c) {
		char spot = tabuleirosJogadores[(indexJogador + 1) % 2][c.X][c.Y];
		switch (spot) {
		case vazio:
			tabuleirosJogadores[(indexJogador + 1) % 2][c.X][c.Y] = falha;
			return 0;
		case ocupado:
			tabuleirosJogadores[(indexJogador + 1) % 2][c.X][c.Y] = acerto;
			return 1;
		default:
			return -1;
		}
	}

	private boolean checarVencedor(int indexJogador) {
		boolean venceu = true;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (tabuleirosJogadores[(indexJogador + 1) % 2][i][j] == ocupado) {
					venceu = false;
					break;
				}
			}
		}
		return venceu;
	}

	private char[][] ConvertToEnemyView(char[][] tabuleiro) {
		char[][] interno = new char[10][10];
		for (int i = 0; i < 10; i++) {
			interno[i] = (char[]) tabuleiro[i].clone();
		}
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (tabuleiro[j][i] == ocupado) {
					interno[j][i] = vazio;
				}
			}
		}
		return interno;
	}

	public char[][][] getPlayerView(int indexJogador) {
		char[][][] results = new char[2][10][10];
		results[0] = tabuleirosJogadores[indexJogador];
		results[1] = ConvertToEnemyView(tabuleirosJogadores[(indexJogador + 1) % 2]);
		return results;
	}
}