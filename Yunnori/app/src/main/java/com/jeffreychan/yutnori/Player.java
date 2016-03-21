package com.jeffreychan.yutnori;

public class Player {
	private int numPieces = 4;
	public Piece[] pieces = new Piece[4];
	
	public Player() {
		for (int i = 0; i < pieces.length; i++) {
			pieces[i] = new Piece();
		}
	}

	public int getNumPieces() {
		return numPieces;
	}
	
	public void incrementPieces(int num) {
		numPieces += num;
	}
	
	public void decrementPieces(int num) {
		numPieces -= num;
	}
	
		
}