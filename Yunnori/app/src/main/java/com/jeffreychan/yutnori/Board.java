package com.jeffreychan.yutnori;

import java.util.ArrayList;
import java.util.Random;

public class Board {

	int[] rollArray = new int[5];
	int rollIndex = 0;
	int playerTurn = 1;

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
	 * @return The last index that contains a roll
	 */
	public int addRoll(int roll){
		rollArray[rollIndex] = roll;

		if (roll == 4 || roll == 5 && rollIndex < 4){
			rollIndex++;
			return rollIndex-1;
		}
		else {
			playerTurn = ((playerTurn + 1) % 2) + 1;
			resetRollArray();
			return rollIndex;
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

}


	
