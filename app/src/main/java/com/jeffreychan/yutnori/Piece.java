package com.jeffreychan.yutnori;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class Piece {

	private int location = -1;
	private int value = 1;


	static Set<Integer> specialTiles = new TreeSet<>(Arrays.asList(0, 1, 5, 10, 15, 20, 21, 22, 23, 24, 25, 26, 27));

	/**
	 * Processes the current rolls and returns move locations with respective move distances
	 *
	 * @param moves array of rolls
	 * @return 2d array containing move locations in first column and move distances in second column
	 */
	public Integer[][] calculateMoveset(int[] moves){

		ArrayList<Integer[]> moveSet = new ArrayList<>();
		int tempLocation = location;

		for (int i = 0; i < moves.length; i++) {
			if (moves[i] != 0) {
				if (specialTiles.contains(location)) {
					if (location == 0) {
						if (moves[i] >= 1) {
							location = 32;
						} else {
							location = 19;

							/* Rolling a -1 on the bottom right tile gives you two possible choices.
							 * This covers the second case.
							 * The first case is covered at the end of the loop
							 */
							Integer[] secondMove = new Integer[2];
							secondMove[0] = 28;
							secondMove[1] = moves[i];
							moveSet.add(secondMove);
						}
					} else if (location == 1) {
						location = 1 + moves[i];
					} else if (location == 5) {
						if (moves[i] >= 1) {
							location = 19 + moves[i];
						} else {
							location--;
						}
					} else if (location == 10) {
						if (moves[i] == 1 || moves[i] == 2) {
							location = 24 + moves[i];
						} else if (moves[i] == 3) {
							location = 22;
						} else if (moves[i] == 4 || moves[i] == 5){
							location = 23 + moves[i];
						} else {
							location--;
						}
					} else if (location == 15) {
						if (moves[i] > 0 && moves[i] < 5) {
							location += moves[i];
						} else if (moves[i] == 5){
							location = 0;
						} else {
							location--;

							/* Rolling a -1 on the bottom left tile gives you two possible choices.
							 * This covers the second case.
							 * The first case is covered at the end of the loop
							 */
							Integer[] secondMove = new Integer[2];
							secondMove[0] = 24;
							secondMove[1] = moves[i];
							moveSet.add(secondMove);
						}
					} else if (location == 20) {
						if (moves[i] >= 1 && moves[i] <= 4) {
							location = 20 + moves[i];
						} else if (moves[i] == -1) {
							location = 5;
						} else if (moves[i] == 5) {
							location = 15;
						}
					} else if (location == 21) {
						if (moves[i] >= 1 && moves[i] <= 3) {
							location = 21 + moves[i];
						} else if (moves[i] == -1) {
							location--;
						} else if (moves[i] == 4) {
							location = 15;
						} else if (moves[i] == 5) {
							location = 16;
						}
					} else if (location == 22) {
						if (moves[i] == 1 || moves[i] == 2 || moves[i] == 3){
							location = 26 + moves[i];
						} else if (moves[i] > 3){
							location = 32;
						} else {
							location = 21;

							/* Rolling a -1 on center tile gives you two possible choices.
							 * This covers the second case.
							 * The first case is covered at the end of the loop
							 */
							Integer[] secondMove = new Integer[2];
							secondMove[0] = 26;
							secondMove[1] = moves[i];
							moveSet.add(secondMove);
						}
					} else if (location == 23) {
						if (moves[i] == 1) {
							location++;
						} else if (moves[i] == -1) {
							location--;
						} else if (moves[i] >= 2 && moves[i] <= 4) {
							location = 13 + moves[i];
						} else if (moves[i] == 5) {
							location = 18;
						}
					} else if (location == 24) {
						if (moves[i] >= 1) {
							location = 14 + moves[i];
						} else if (moves[i] == -1) {
							location--;
						}
					} else if (location == 25) {
						if (moves[i] == 1) {
							location = 26;
						} else if (moves[i] == 2) {
							location = 22;
						} else if (moves[i] == 3 || moves[i] == 4) {
							location = 24 + moves[i];
						} else if (moves[i] == 5){
							location = 0;
						} else {
							location = 10;
						}
					} else if (location == 26) {
						if (moves[i] == 1) {
							location = 22;
						} else if (moves[i] == 2 || moves[i] == 3) {
							location = 25 + moves[i];
						} else if (moves[i] == 4){
							location = 0;
						} else if (moves[i] == 5) {
							location = 32;
						} else {
							location--;
						}
					} else if (location == 27) {
						if (moves[i] >= 1) {
							location += moves[i];
						} else {
							location = 22;
						}
					}
				} else if (location >= 15 && location <= 19){
					if (location + moves[i] == 20){
						location = 0;
					} else if (location + moves[i] < 20){
						location += moves[i];
					} else {
						location = 32;
					}
				} else if (location == -1) {
					if (moves[i] >= 1) {
						location = moves[i];
					}
				} else {
					location += moves[i];
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
			possibleMoves[1] = moves[i];
			moveSet.add(possibleMoves);
			location = tempLocation;
		}

		return moveSet.toArray(new Integer[moveSet.size()][2]);
	}

	public int getLocation(){
		return location;
	}

	public void setLocation(int i){
		location = i;
	}

	public int getValue() { return value; }

	public void setValue(int v) { value = v; }

	public void addValue(int v) { value += v; }

}
