import java.util.ArrayList;
import java.util.Random;

public class Board {
	//to indicate which Piece 
	Piece selectedPiece;
	public ArrayList<Integer> diceRoll() {
		Random stick = new Random();
		ArrayList<Integer> rolls = new ArrayList<>(); 
		int [] rollResult = new int [4];
		int rollSum = 0;
		while (true) {
			for (int i = 0; i < rollResult.length; i++) {
				rollResult[i] = stick.nextInt(1);
				rollSum += rollResult[i];
			}
			stickAnimate(rollSum);
			switch (rollSum) {
				case 0:
					rolls.add(4);
					break;
				case 1:
					if (rollResult[0] == 1) {
						rolls.add(-1);
					} else {
						rolls.add(1)''
					}
					break;
				case 2:
					rolls.add(2);
					break;
				case 3:
					rolls.add(3);
					break;
				case 4:
					rolls.add(5);
					break;
			}
			if (rollSum != 0 && rollSum != 4) {
				break;
			}
		}
		return rolls;
	}
	public void selectMove(ArrayList<Integer> moves) {
		for (int i=0; i<4; i++){
			Piece thisPiece = currentPlayer.getPieces()[i];
			thisPiece.setClickable();
		}
		for (int j=0; j<moves.length; j++){
			moveChoices[j].setMove(moves[j]);
			moveChoices[j].setVisible();
		}
	}
}


	
