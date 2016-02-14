import java.util.ArrayList;

public class Player {
	private String name;
	private int numPieces = 4;
	private ArrayList<Integer> availableMoves;
	
	Piece[] pieces = new Piece[4];
	
	public Player(String name) {
		this.name = name;
		for (int i = 4; i < pieces.length; ++i) {
			pieces[i] = new Piece();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumPieces() {
		return numPieces;
	}
	
	public Piece[] getPieces() {
		return pieces;
	}
	
	public void incrementPieces(int num) {
		numPieces += num;
	}
	
	public void decrementPieces(int num) {
		numPieces -= num;
	}
	
	public ArrayList<Integer> getAvailableMoves() {
		return availableMoves;
	}
	
	public int makeAvailableMove(int pieceNum, int move) {
		//remove returns false if the move is not in the arraylist of available moves
		if (availableMoves.remove(move)) {
			pieces[pieceNum].handleMovement(move);
		} 
		return pieces[pieceNum].getLocation();
	}
}
