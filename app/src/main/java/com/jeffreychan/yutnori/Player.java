package com.jeffreychan.yutnori;

public class Player {
	private int score = 0;			// First to 4 wins
	private int numPieces = 0;		// Number of pieces on the board

	public Piece[] pieces = new Piece[4];

	/*
	 * Creates new pieces for the player 
	 */
	public Player() {
		for (int i = 0; i < pieces.length; i++) {
			pieces[i] = new Piece();
		}
	}

	/*
	 * Finds the next available piece (going from left to right) in the pieces array
	 */
	public int findAvailablePiece(){
		for (int i = 0; i < 4; i++){
			if (pieces[i].getLocation() == -1){
				return i;
			}
		}
		return -1;  // Error
	}

	public int getScore(){ return score; }

	public void addScore(int i){ score += i; }

	public int getNumPieces() { return numPieces; }

	public void addNumPieces(int i) { numPieces += i; }

	public void subtractNumPieces(int i) { numPieces -= i; }

	public boolean hasNoPiecesOnBoard() { return (numPieces == score); }

	public boolean hasAllPiecesOnBoard() { return (numPieces == 4); }

	public boolean hasWon() { return (score == 4); }

	public void reset() {
		score = 0;
		numPieces = 0;

		for (int i = 0; i < 4; i++) {
			pieces[i].reset();
		}
	}
}
