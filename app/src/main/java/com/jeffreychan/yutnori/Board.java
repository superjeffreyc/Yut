package com.jeffreychan.yutnori;

import java.util.Arrays;

public class Board {

	public int[] rollArray = new int[5];
	int rollIndex = 0;
	int playerTurn = 0;

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

	public void resetRollArray(){
		rollIndex = 0;
		for (int i = 0; i < 5; i++){
			rollArray[i] = 0;
		}
	}

	public int getPlayerTurn(){
		return playerTurn;
	}

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

	public boolean rollEmpty(){
		int count = 0;
		for (int i = 0; i < 5; i++){
			if (rollArray[i] != 0) count++;
		}

		return (count == 0);
	}
}


	
