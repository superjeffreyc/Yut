package com.jeffreychan.yutnori;

public class Player {
	private int score = 0;
	public Piece[] pieces = new Piece[4];
	
	public Player() {
		for (int i = 0; i < pieces.length; i++) {
			pieces[i] = new Piece();
		}
	}

	public int getScore() {
		return score;
	}

}