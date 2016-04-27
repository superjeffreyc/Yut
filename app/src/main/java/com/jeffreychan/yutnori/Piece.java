package com.jeffreychan.yutnori;

import java.util.ArrayList;

public class Piece {

	private int location = -1;  // Current location on the board. -1 indicates off the board.
	private int value = 1;      // Current number of pieces stacked

	/**
	 * Iterates through each move and calls the Board class to calculate possible locations to move to
	 * based on that move.
	 *
	 * Returns all possible move locations with respective move distances as a 2d Integer array
	 *
	 * @param moves array of rolls
	 * @return 2d array containing move locations in first column and move distances in second column
	 */
	public Integer[][] calculateMoveset(int[] moves){

		ArrayList<Integer[]> moveList = new ArrayList<>();

		for (int i : moves){
			moveList.addAll(Board.calculateLocation(i, location));
		}

		return moveList.toArray(new Integer[moveList.size()][2]);
	}

	/**
	 * Gets the location of the current piece.
	 *
	 * Location -1 indicates off the board.
	 * Location 32 indicates finished.
	 *
	 * @return The location of this piece
	 */
	public int getLocation() { return location; }

	/**
	 * Set the location of this piece
	 * @param i The new location
	 */
	public void setLocation(int i){
		location = i;
	}

	/**
	 * Gets the number of pieces stacked on this piece
	 * Value of 1 indicates this is the only piece at this location
	 *
	 * @return The number of stacked pieces
	 */
	public int getValue() { return value; }

	/**
	 * Resets the value of this piece back to 1. Called when a piece is sent off the board.
	 */
	public void resetValue() { value = 1; }

	/**
	 * Adds to the value of the current piece. Called when another piece is stacking on this piece.
	 *
	 * @param v The amount of pieces being stacked with this piece
	 */
	public void addValue(int v) { value += v; }

}
