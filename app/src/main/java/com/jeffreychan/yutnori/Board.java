package com.jeffreychan.yutnori;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/*
 * Each tile on the Board is given a number and referred to in this manner.
 * A piece's location is based on this numbering system with 2 exceptions:
 * 1) -1 represents an off board location
 * 2) 32 represents the finish location (cannot be changed)
 * The board numbers are as follows:
 *
 * 10  9  8  7  6  5
 * 11 25        20 4
 * 12    26   21   3
 *         22
 * 13    23   27   2
 * 14 24        28 1
 * 15 16 17 18 19  0
 *
 * All pieces begin on tile 0 but do not appear there initially.
 *
 * To finish a piece, the piece must move all the way around the board and back to tile 0
 * and then go at least one more move past tile 0.
 *
 * Tiles 5, 10, and 22 represent special tiles which re-direct a piece on a path towards the start tile
 * These tiles represent shortcuts and can only be used if the piece lands directly on this tile
 *
 */
public class Board {

	public int[] rollArray = new int[5];    // Max number of rolls that can be stored is 5
	int rollIndex = 0;                      // The index pointing to the first empty roll slot
	int playerTurn = 0;                     // Current turn (0 = Player 1, 1 = Player 2)

	// These tiles must be handled differently. You cannot simply add moves to these tiles to calculate move locations.
	static Set<Integer> specialTiles = new TreeSet<>(Arrays.asList(0, 1, 5, 10, 15, 20, 21, 22, 23, 24, 25, 26, 27));

	/*
	 * Initialize the roll array to all 0s
	 */
	public Board(){
		for (int i = 0; i < 5; i++){
			rollArray[i] = 0;
		}
	}

	/**
	 * There are 4 sticks to throw, each of which can be facing up or down.
	 * The roll is determined by the number of sticks facing up, with the following exceptions:
	 * All sticks face down is a roll of 5 and only one marked stick face up is a roll of -1.
	 * This gives the following probabilities for these rolls:
	 * (1) 4/16 (2) 6/16 (3) 4/16 (4) 1/16 (5) 1/16 (-1) 1/16
	 *
	 * @return the value of the sticks thrown
	 */
	public int throwSticks(){
		int roll = 1;

		int num = (int) (Math.random() * 16) + 1;

		if (num == 1) roll = -1;
		else if (num > 1 && num <= 4) roll = 1;
		else if (num > 4 && num <= 10) roll = 2;
		else if (num > 10 && num <= 14) roll = 3;
		else if (num > 14 && num <= 15) roll = 4;
		else if (num > 15 && num <= 16) roll = 5;

		return roll;
	}

	/**
	 * Adds a roll amount to the rollArray
	 *
	 * @param roll The roll amount being added to the array
	 */
	public void addRoll(int roll){
		rollArray[rollIndex] = roll;

		if ((roll == 4 || roll == 5) && rollIndex < 4){
			rollIndex++;
		}
	}

	/*
	 * Set all rolls to 0
	 */
	public void resetRollArray(){
		rollIndex = 0;
		for (int i = 0; i < 5; i++){
			rollArray[i] = 0;
		}
	}

	/**
	 * Gets the current turn
	 * 0 = Player 1's Turn
	 * 1 = Player 2's Turn or Computer's Turn
	 *
	 * @return Current turn
	 */
	public int getPlayerTurn(){
		return playerTurn;
	}

	/*
	 * Ends the current turn by switching playerTurn and clearing rollArray
	 */
	public void endTurn(){
		playerTurn = (playerTurn + 1) % 2;
		resetRollArray();
	}

	/**
	 * Removes the roll from the roll array. This method should be called after the player has made a move.
	 *
	 * @param i the roll to remove from the roll array
	 */
	public void removeRoll(int i) {
		for(int j = 0; j < rollArray.length; ++j) {
			if(rollArray[j] == i) {rollArray[j] = 0; break;}
		}
		for(int k = 0; k < rollArray.length; ++k) {
			if(rollArray[k] == 0) {
				int index = k + 1;
				while(index < rollArray.length && rollArray[index] == 0) index += 1;
				if(index == rollArray.length) index--;
				if(rollArray[index] == 0) break;
				rollArray[k] = rollArray[index];
				rollArray[index] = 0;
			}
		}
	}

	/**
	 * Checks if rollArray is empty.
	 * @return true if rollArray is empty, false otherwise
	 */
	public boolean rollEmpty(){
		int count = 0;
		for (int i = 0; i < 5; i++){
			if (rollArray[i] != 0) count++;
		}

		return (count == 0);
	}

	/**
	 * Processes the roll amount and starting location to calculate possible locations to move to.
	 *
	 * Returns an ArrayList of 2-element Integer arrays, with the arrays as follows:
	 * Index 0 contains the destination location
	 * Index 1 contains the roll amount
	 *
	 * Only a roll of -1 on tiles 0, 15, and 22 will return 2 Integer arrays in the ArrayList
	 * All other locations and rolls will only return 1 Integer array
	 *
	 * Rolling a -1 with no pieces on the board will skip the current turn
	 *
	 * @param move The roll amount
	 * @param location The starting location
	 * @return An ArrayList<Integer[]> containing pairs as follows: [Destination, roll amount]
	 */
	public static ArrayList<Integer[]> calculateLocation(int move, int location){
		ArrayList<Integer[]> moveList = new ArrayList<>();

		if (move != 0) {
			if (specialTiles.contains(location)) {
				if (location == 0) {
					if (move >= 1) {
						location = 32;
					} else {
						location = 19;

							/* Rolling a -1 on the bottom right tile gives you two possible choices.
							 * This covers the second case.
							 * The first case is covered at the end of the loop
							 */
						Integer[] secondMove = new Integer[2];
						secondMove[0] = 28;
						secondMove[1] = move;
						moveList.add(secondMove);
					}
				} else if (location == 1) {
					location = 1 + move;
				} else if (location == 5) {
					if (move >= 1) {
						location = 19 + move;
					} else {
						location--;
					}
				} else if (location == 10) {
					if (move == 1 || move == 2) {
						location = 24 + move;
					} else if (move == 3) {
						location = 22;
					} else if (move == 4 || move == 5){
						location = 23 + move;
					} else {
						location--;
					}
				} else if (location == 15) {
					if (move > 0 && move < 5) {
						location += move;
					} else if (move == 5){
						location = 0;
					} else {
						location--;

							/* Rolling a -1 on the bottom left tile gives you two possible choices.
							 * This covers the second case.
							 * The first case is covered at the end of the loop
							 */
						Integer[] secondMove = new Integer[2];
						secondMove[0] = 24;
						secondMove[1] = move;
						moveList.add(secondMove);
					}
				} else if (location == 20) {
					if (move >= 1 && move <= 4) {
						location = 20 + move;
					} else if (move == -1) {
						location = 5;
					} else if (move == 5) {
						location = 15;
					}
				} else if (location == 21) {
					if (move >= 1 && move <= 3) {
						location = 21 + move;
					} else if (move == -1) {
						location--;
					} else if (move == 4) {
						location = 15;
					} else if (move == 5) {
						location = 16;
					}
				} else if (location == 22) {
					if (move == 1 || move == 2 || move == 3){
						location = 26 + move;
					} else if (move > 3){
						location = 32;
					} else {
						location = 21;

							/* Rolling a -1 on center tile gives you two possible choices.
							 * This covers the second case.
							 * The first case is covered at the end of the loop
							 */
						Integer[] secondMove = new Integer[2];
						secondMove[0] = 26;
						secondMove[1] = move;
						moveList.add(secondMove);
					}
				} else if (location == 23) {
					if (move == 1) {
						location++;
					} else if (move == -1) {
						location--;
					} else if (move >= 2 && move <= 4) {
						location = 13 + move;
					} else if (move == 5) {
						location = 18;
					}
				} else if (location == 24) {
					if (move >= 1) {
						location = 14 + move;
					} else if (move == -1) {
						location--;
					}
				} else if (location == 25) {
					if (move == 1) {
						location = 26;
					} else if (move == 2) {
						location = 22;
					} else if (move == 3 || move == 4) {
						location = 24 + move;
					} else if (move == 5){
						location = 0;
					} else {
						location = 10;
					}
				} else if (location == 26) {
					if (move == 1) {
						location = 22;
					} else if (move == 2 || move == 3) {
						location = 25 + move;
					} else if (move == 4){
						location = 0;
					} else if (move == 5) {
						location = 32;
					} else {
						location--;
					}
				} else if (location == 27) {
					if (move >= 1) {
						location += move;
					} else {
						location = 22;
					}
				}
			} else if (location >= 15 && location <= 19){
				if (location + move == 20){
					location = 0;
				} else if (location + move < 20){
					location += move;
				} else {
					location = 32;
				}
			} else if (location == -1) {
				if (move >= 1) {
					location = move;
				}
			} else {
				location += move;
			}

		} else {
			location = -1;
		}

		if (location == 29) {
			location = 0;
		} else if (location > 29){
			location = 32;
		}

		Integer[] possibleMoves = new Integer[2];
		possibleMoves[0] = location;
		possibleMoves[1] = move;
		moveList.add(possibleMoves);

		return moveList;
	}
}


	
