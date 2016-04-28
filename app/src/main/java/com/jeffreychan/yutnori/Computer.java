package com.jeffreychan.yutnori;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class Computer {

	static Set<Integer> shortcuts = new TreeSet<>(Arrays.asList(0, 5, 10, 22)); // Used for Priority #3 in selectMove

	/**
	 * Calculates all possible moves for all pieces.
	 * The moves are stored in a 2d ArrayList, where the row indicates the piece associated with that move.
	 *
	 * @param players The two players in the game
	 * @param rollArray The current rolls available
	 * @return An int array of 2 elements: The best piece to move and the best location to move to with that piece. [piece, move]
	 */
	public static int[] selectMove(Player[] players, int[] rollArray){

		ArrayList<ArrayList<Integer>> moves = new ArrayList<>();

		for (int i = 0; i < 4; i++){
			ArrayList<Integer> m = new ArrayList<>();

			// Don't calculate moveset for finished pieces
			if (players[1].pieces[i].getLocation() != 32) {
				Integer[][] moveSet = players[1].pieces[i].calculateMoveset(rollArray);

				for (Integer[] arr : moveSet) {
					if (arr[0] != -1) {
						m.add(arr[0]);
					}
				}
			}

			moves.add(m);
		}

		// #1 Priority: Capture enemy piece----------------------------------------
		for (int i = 0; i < moves.size(); i++){
			for (int j : moves.get(i)){
				for (int k = 0; k < 4; k++) {
					if (players[0].pieces[k].getLocation() == j) {
						if (players[1].getNumPieces() < 4 && j <= 5){
							if (players[1].pieces[i].getLocation() == -1) {
								return new int[]{-1, j};
							}
						}
						if (players[1].pieces[i].getLocation() != -1 && players[1].pieces[i].getLocation() != 32) {
							return new int[]{i, j};
						}
					}
				}
			}
		}

		// #2 Priority: Try to stack----------------------------------------
		for (int i = 0; i < moves.size(); i++){
			for (int j : moves.get(i)){
				for (int k = 0; k < 4; k++) {
					if (players[1].pieces[k].getLocation() == j) {
						if (players[1].getNumPieces() < 4 && j <= 5){
							if (players[1].pieces[i].getLocation() == -1) {
								return new int[]{-1, j};
							}
						}
						if (players[1].pieces[i].getLocation() != -1 && players[1].pieces[i].getLocation() != 32) {
							return new int[]{i, j};
						}
					}
				}
			}
		}

		// #3 Priority: Try to take shortcuts----------------------------------------
		for (int i = 0; i < moves.size(); i++){
			for (int j : moves.get(i)){
				if (shortcuts.contains(j)){
					if (players[1].getNumPieces() < 4 && j == 5){
						if (players[1].pieces[i].getLocation() == -1) {
							return new int[]{-1, j};
						}
					}
					if (players[1].pieces[i].getLocation() != -1 && players[1].pieces[i].getLocation() != 32) {
						return new int[]{i, j};
					}
				}
			}
		}

		// #4 Priority: Finish pieces----------------------------------------
		for (int i = 0; i < moves.size(); i++){
			for (int j : moves.get(i)){
				if (j == 32){   // finish location
					return new int[]{i,j};
				}
			}
		}

		// #5 Priority: Take first available move----------------------------------------
		ArrayList<Integer> rolls = new ArrayList<>();
		for (int i : rollArray){
			if (i != 0) rolls.add(i);
		}

		// Check for single -1 roll (cannot use off board pieces)
		if (rolls.size() == 1 && rolls.get(0) == -1){
			for (int i = 0; i < 4; i++) {
				if (players[1].pieces[i].getLocation() != -1 && players[1].pieces[i].getLocation() != 32){
					return new int[]{i, moves.get(i).get(0)};
				}
			}
		}
		// Use off board pieces
		else if (players[1].getNumPieces() < 4){
			for (int i = 0; i < 4; i++) {
				if (players[1].pieces[i].getLocation() == -1)
					return new int[]{-1, moves.get(i).get(0)};
			}
		}
		// Use on board pieces
		else {
			for (int i = 0; i < 4; i++) {
				if (players[1].pieces[i].getLocation() != -1 && players[1].pieces[i].getLocation() != 32){
					return new int[] {i, moves.get(i).get(0)};
				}
			}
		}

		return new int[] {-1, -1};  // Error while selecting piece and location
	}
}
