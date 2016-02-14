package com.jeffreychan.yutnori;

import java.util.TreeSet;

public class Piece {

	int location = 0;

	public void setLocation(int location) {
		this.location = location;
	}

	public int getLocation() {
		return this.location;
	}

	public int handleMovement(int moves){
		int location = 0;
		TreeSet<Integer> specialTiles = new TreeSet<>();
		specialTiles.add(6);
		specialTiles.add(11);
		specialTiles.add(21);
		specialTiles.add(21);
		specialTiles.add(23);
		specialTiles.add(24);
		specialTiles.add(25);

		if (specialTiles.contains(new Integer(location))){
			if (location == 6){
				if (moves >= 1) {
					location = 20 + moves;
				}
				else {
					location--;
				}
			}
			else if (location == 11){
				if (moves >= 1){
					location = 25 + moves;
				}
				else {
					location--;
				}
			}
			else if (location == 21){
				if (moves >= 1 && moves <= 4){
					location = 21 + moves;
				}
				else if (moves == -1){
					location = 6;
				}
				else if (moves == 5){
					location = 16;
				}
			}
			else if (location == 22){
				if (moves >= 1 && moves <= 3){
					location = 22 + moves;
				}
				else if (moves == -1){
					location--;
				}
				else if (moves == 4){
					location = 16;
				}
				else if (moves == 5){
					location = 17;
				}
			}
			else if (location == 23){
				location = 28 + moves;
			}
			else if (location == 24){
				if (moves == 1){
					location = 25;
				}
				else if (moves == -1){
					location--;
				}
				else if (moves >= 2) {
					location = 14 + moves;
				}
			}
			else if (location == 25){
				if (moves >= 1){
					location = 15 + moves;
				}
				else if (moves == -1){
					location--;
				}
			}
		}

		if (location > 32) {
			location = 32;
		}

		return location;
	}
}
