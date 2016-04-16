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

	public void removeRoll(int i) {
		int j = 0;
		for(j = 0; j < rollArray.length; ++j) {
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

		System.out.println(Arrays.toString(rollArray));
	}
	/*
	DO NOT TOUCH ~NO TRESPASSING~
	calculates the distance moved by chosen piece
	public int moveDistance(int startPos, int endPos) {
		int distance = 0;
		if(startPos <= 19 && endPos <= 19) distance = endPos - startPos;
		else if(startPos == 5) distance = endPos - startPos - 14; // 14 displacement (5 -> 20)
		else if(startPos == 10) {
			if(endPos == 22) distance = 3;
			else if(endPos < 27) distance = endPos - 24;
			else distance = endPos - 23;
		}
		else if(startPos == 22) {
			if(endPos < 29) distance = endPos - 26;
			else distance = 3;
		}

		return distance;
	}
	*/
}


	
