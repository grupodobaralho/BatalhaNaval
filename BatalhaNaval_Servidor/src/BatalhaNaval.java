import java.util.Arrays;

/**
 * Classe respons�vel por armazenar os tabuleiros do jogo com as informa��es de
 * cada jogador. Define valores das posi��es, inicia tabuleiro, verifica ataque
 * do usu�rio, atualiza tabuleiro, informa e processa estado do tabuleiro para a
 * classe ServidorJogo
 * 
 * @author Israel Deorce
 *
 */
public class BatalhaNaval {
	// 1[] - jogador 1 e 2; [][] matriz que representa o tabuleiro
	private char[][][] tabuleirosJogadores;
	// atribui valores para os chars que ir�o compor a matriz
	private final static char vazio = '-', miss = 'O', hit = 'X', ocupado = 'S';
	private int jogadorAtivo;

	/**
	 * Uma coordenada representa um ponto no tabuleiro/matriz
	 * 
	 * @author Israel-PC
	 *
	 */
	private class Coordenada {
		public int X;
		public int Y;

		Coordenada(int x, int y) {
			X = x;
			Y = y;
		}
	}

	/**
	 * Construtor que inicia o tabuleiro com todas as posi��es com '-'
	 */
	public BatalhaNaval() {
		tabuleirosJogadores = new char[2][10][10];
		for (int i = 0; i < 10; i++) {
			Arrays.fill(tabuleirosJogadores[0][i], vazio);
			Arrays.fill(tabuleirosJogadores[1][i], vazio);
		}
		jogadorAtivo = 0;
	}

	/**
	 * M�todo que recebe o index do jogador e os seus navios e marca com S na matriz
	 * correspondente
	 * 
	 * @param indexJogador
	 * @param navios
	 */
	public void atribuiNavios(int indexJogador, String navios) {
		for (int i = 0; i < navios.length(); i += 2) {
			String loc = navios.substring(i, i + 2);
			tabuleirosJogadores[indexJogador][loc.charAt(0) - 97][loc.charAt(1) - 48] = 'S';
		}
	}

	/**
	 * M�todo que recebe um palpite de um jogador, chama o m�todo atacar que confere
	 * a matriz, e retorna um valor que corresponde ao resultado: -3: default -2:
	 * n�o � seu turno -1: j� atacou 0: miss 1: HIT 2: win
	 * 
	 * @param indexJogador
	 * @param x
	 * @param y
	 * @return
	 */
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

	/**
	 * M�todo auxiliar que recebe jogador e coordenada, confere na matriz, atualiza
	 * valor e retorna resultado -1: j� atacou 0: miss 1: hit
	 * 
	 * @param indexJogador
	 * @param c
	 * @return
	 */
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

	/**
	 * M�todo que percorre a matriz do adversario e tenta encontrar alguma posi��o
	 * ocupada. Se n�o encontrar, o jogador atual venceu.
	 * 
	 * @param indexJogador
	 * @return
	 */
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

	/**
	 * M�todo auxiliar que converte o tabuleiro inimigo para esconder a visualiza��o
	 * das posi��es que cont�m navios e ainda n�o foram encontradas pelo jogador
	 * 
	 * @param tabuleiro
	 * @return
	 */
	public char[][] converterVisaoInimigo(char[][] tabuleiro) {
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

	/**
	 * M�todo que cria dois novos tabuleiros (um para cada jogador), copia as
	 * informa��es do jogador atual na nova matriz [0][10[10] e chama o m�todo que
	 * converte a vis�o do inimigo, atribui � matriz [1][10[10] e retorna
	 * 
	 * @param indexJogador
	 * @return
	 */
	public char[][][] getVisaoJogador(int indexJogador) {
		char[][][] resultados = new char[2][10][10];
		resultados[0] = tabuleirosJogadores[indexJogador];
		resultados[1] = converterVisaoInimigo(tabuleirosJogadores[(indexJogador + 1) % 2]);
		return resultados;
	}
}