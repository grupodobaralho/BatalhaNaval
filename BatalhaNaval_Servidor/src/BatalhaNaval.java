import java.util.Arrays;

public class BatalhaNaval {
	private char[][][] tabuleirosJogadores;
	private final static char vazio = '-', miss = 'O', hit = 'X', ocupado = 'S';
	private int jogadorAtivo;

	private class Coordenada {
		public int X;
		public int Y;

		Coordenada(int x, int y) {
			X = x;
			Y = y;
		}
	}

	public BatalhaNaval() {
		tabuleirosJogadores = new char[2][10][10];
		for (int i = 0; i < 10; i++) {
			Arrays.fill(tabuleirosJogadores[0][i], vazio);
			Arrays.fill(tabuleirosJogadores[1][i], vazio);
		}
		jogadorAtivo = 0;
	}

	public void atribuiNavios(int indexJogador, String navios) {
		for (int i = 0; i < navios.length(); i += 2) {
			String loc = navios.substring(i, i + 2);
			tabuleirosJogadores[indexJogador][loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
		}
	}

	public int fazMovimento(int indexJogador, int x, int y) {
		if (indexJogador != jogadorAtivo) {
			return -2;
		}
		Coordenada move = new Coordenada(x, y);
		int resultado = atacar(indexJogador, move);
		switch (resultado) {
		case -1:
			return -1;
		case 0:
			jogadorAtivo = (indexJogador + 1) % 2;
			return 0;
		case 1:
			if (verificaVencedor(jogadorAtivo)) {
				return 2;
			}
			jogadorAtivo = (indexJogador + 1) % 2;
			return 1;
		default:
			return -3;
		}
	}

	public int atacar(int indexJogador, Coordenada c) {
		char quadrante = tabuleirosJogadores[(indexJogador + 1) % 2][c.X][c.Y];
		switch (quadrante) {
		case vazio:
			tabuleirosJogadores[(indexJogador + 1) % 2][c.X][c.Y] = miss;
			return 0;
		case ocupado:
			tabuleirosJogadores[(indexJogador + 1) % 2][c.X][c.Y] = hit;
			return 1;
		default:
			return -1;
		}
	}

	public boolean verificaVencedor(int indexJogador) {
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

	public char[][] ConvertToEnemyView(char[][] tabuleiro) {
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
		char[][][] resultados = new char[2][10][10];
		resultados[0] = tabuleirosJogadores[indexJogador];
		resultados[1] = ConvertToEnemyView(tabuleirosJogadores[(indexJogador + 1) % 2]);
		return resultados;
	}
}