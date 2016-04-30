package com.jeffreychan.yutnori;

public class Player {
	private int score = 0;
	private int numPieces = 0;

	public Piece[] pieces = new Piece[4];

	public Player() {
		for (int i = 0; i < pieces.length; i++) {
			pieces[i] = new Piece();
		}
	}

	public int findAvailablePiece(){
		for (int i = 0; i < 4; i++){
			if (pieces[i].getLocation() == -1){
				return i;
			}
		}
		return -1;
	}

	public int getScore() { return score; }

	public void addScore(int i){ score += i; }

	public int getNumPieces() { return numPieces; }

	public void addNumPieces(int i) { numPieces += i; }

	public void subtractNumPieces(int i) { numPieces -= i; }

	public boolean hasNoPiecesOnBoard() { return (numPieces == score); }

	public boolean hasAllPiecesOnBoard() { return (numPieces == 4); }

	public boolean hasWon() { return (score == 4); }
}