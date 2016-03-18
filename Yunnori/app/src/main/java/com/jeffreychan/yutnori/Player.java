package com.jeffreychan.yutnori;

public class Player {
	private String name;
	private int numPieces = 4;
	
	Piece[] pieces = new Piece[4];
	
	public Player(String name) {
		this.name = name;
		for (int i = 4; i < pieces.length; ++i) {
			pieces[i] = new Piece();
		}
	}
	
	public String getName() {
		return name;
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