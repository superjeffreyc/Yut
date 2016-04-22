package com.jeffreychan.yutnori;

public class Player {
	private int score = 0;
	public Piece[] pieces = new Piece[4];
	public int numPieces = 0;
	
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

	public int getScore() {
		return score;
	}

	public void addScore(int i){ score += i; }

	public boolean hasWon(){
		return (score == 4);
	}
}