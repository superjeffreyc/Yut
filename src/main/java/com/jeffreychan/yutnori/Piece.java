package com.jeffreychan.yutnori;

import java.util.Set;
import java.util.TreeSet;

public class Piece {

	int location = -1;
	
	static Set<Integer> specialTiles = new TreeSet<>();

	public Piece(){
		specialTiles.add(0);
		specialTiles.add(1);
		specialTiles.add(5);
		specialTiles.add(10);
		specialTiles.add(20);
		specialTiles.add(21);
		specialTiles.add(22);
		specialTiles.add(23);
		specialTiles.add(24);
	}

	public int[] calculateMoveset(int[] moves){

		int[] possibleMoves = new int[moves.length];
		int tempLocation = location;

		for (int i = 0; i < moves.length; i++) {
			if (moves[i] != 0) {

				if (specialTiles.contains(Integer.valueOf(location))) {
					if (location == 0) {
						if (moves[i] >= 1) {
							location = 32;
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
						if (moves[i] >= 1) {
							location = 24 + moves[i];
						} else {
							location--;
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

			possibleMoves[i] = location;
			location = tempLocation;
		}

		return possibleMoves;
	}

	public int[] tempCalculateMoveset(int[] moves){

		int[] possibleMoves = new int[moves.length];
		int tempLocation = location;

		for (int i = 0; i < moves.length; i++) {
			if (moves[i] != 0) {

				if (location == -1){
					location++;
				}

				location += moves[i];

				if (location >= 20) {
					location = 0;
				}
				possibleMoves[i] = location;
			} else {
				possibleMoves[i] = -1;
			}
			location = tempLocation;
		}
		return possibleMoves;
	}

	public int getLocation(){
		return location;
	}

	public void setLocation(int i){
		location = i;
	}
}
