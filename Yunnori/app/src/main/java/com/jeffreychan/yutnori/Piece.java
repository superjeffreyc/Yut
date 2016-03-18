package com.jeffreychan.yutnori;

import java.util.TreeSet;

public class Piece {

	int location = 0;
	
	static TreeSet<Integer> specialTiles = new TreeSet<>();

	public Piece(){
		specialTiles.add(0);
		specialTiles.add(1);
		specialTiles.add(6);
		specialTiles.add(11);
		specialTiles.add(21);
		specialTiles.add(21);
		specialTiles.add(23);
		specialTiles.add(24);
		specialTiles.add(25);
	}
	
	public void setLocation(int location) {
		this.location = location;
	}

	public int getLocation() {
		return this.location;
	}

	public int[] calculateMoveset(int[] moves){

		int[] possibleMoves = new int[moves.length];

		for (int i = 0; i < moves.length; i++) {
			if (specialTiles.contains(new Integer(location))) {
				if (location == 0) {
					if (moves[i] >= 1) {
						location = 1 + moves[i];
					}
				} else if (location == 1) {
					if (moves[i] >= 1) {
						location = 1 + moves[i];
					} else if (moves[i] == -1) {
						location = 32;
					}
				} else if (location == 6) {
					if (moves[i] >= 1) {
						location = 20 + moves[i];
					} else {
						location--;
					}
				} else if (location == 11) {
					if (moves[i] >= 1) {
						location = 25 + moves[i];
					} else {
						location--;
					}
				} else if (location == 21) {
					if (moves[i] >= 1 && moves[i] <= 4) {
						location = 21 + moves[i];
					} else if (moves[i] == -1) {
						location = 6;
					} else if (moves[i] == 5) {
						location = 16;
					}
				} else if (location == 22) {
					if (moves[i] >= 1 && moves[i] <= 3) {
						location = 22 + moves[i];
					} else if (moves[i] == -1) {
						location--;
					} else if (moves[i] == 4) {
						location = 16;
					} else if (moves[i] == 5) {
						location = 17;
					}
				} else if (location == 23) {
					location = 28 + moves[i];
				} else if (location == 24) {
					if (moves[i] == 1) {
						location = 25;
					} else if (moves[i] == -1) {
						location--;
					} else if (moves[i] >= 2) {
						location = 14 + moves[i];
					}
				} else if (location == 25) {
					if (moves[i] >= 1) {
						location = 15 + moves[i];
					} else if (moves[i] == -1) {
						location--;
					}
				}
			} else {
				location += moves[i];
			}

			if (location > 32) {
				location = 32;
			}

			possibleMoves[i] = location;
		}

		return possibleMoves;
	}
}
