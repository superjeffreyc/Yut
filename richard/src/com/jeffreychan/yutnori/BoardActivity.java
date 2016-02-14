package com.jeffreychan.yutnori;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.util.ArrayList;

public class BoardActivity extends Activity implements OnClickListener 
	implements PopupMenu.OnMenuItemClickListener{

	Board board;
	Player[] players = new Player[2];
	ImageView[] playerOneImages, playerTwoImages, moveButtons;
	int playerTurn = 1;
	boolean isRunning = true;
	int pieceSelected = 0;
	ArrayList<Integer> rolls;
	boolean selectingMove = false;
	PopupMenu movesPopup;
	ImageView rollButton;
	boolean isRolling;
	Piece currentPiece;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);

		players[0] = new Player("Player 1");
		players[1] = new Player("Player 2");
		board = new Board();

		playerOneImages = new ImageView[4];
		playerTwoImages = new ImageView[4];
		
		rollButton = (ImageView)findViewById(R.id.rollbutton);

		for (int i = 0; i < 4; i++){
			playerOneImages[i] = new ImageView(this);
			playerTwoImages[i] = new ImageView(this);

			playerOneImages[i].setId(i);
			playerOneImages[i].setOnClickListener(this);
			playerOneImages[i].setDrawable(R.drawable.penguinfaded);
			playerOneImages[i].setSize;
			playerTwoImages[i].setId(i + 4);
			playerTwoImages[i].setOnClickListener(this);

		}

		moveButtons = new ImageView[5];
		for (int i = 0; i < 4; i++){
			moveButtons[i] = new ImageView(this);
			moveButtons[i].setId(i + 10);
			moveButtons[i].setOnClickListener(this);

		}

	
	}

	public void startGameLoop(){
		while (isRunning) {
			if (playerTurn == 1) {
				//rolls = board.diceRoll();


				playerTurn = 2;
			} else {
				//rolls = board.diceRoll();


				playerTurn = 1;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (playerTurn == 1 && selectingMove) {
			if (v.getId() < 4) {
				pieceSelected = v.getId();
				Piece [] pieceArray = players[0].getPieces;
				currentPiece = pieceArray[pieceSelected];
				ArrayList<Integer> moves = players[0].getAvailableMoves();
				if (movesPopup != null) {
					movesPopup.dismiss();
				}
				movesPopup = new PopupMenu(this, findViewbyId(pieceSelected));
				for (int i = 0; i < moves.size(); i++) {
					movesPopup.getMenu().add(""+moves[i]);
				}
				movesPopup.setOnMenuItemClickListener(this);
				movesPopup.show();
			}
		} else if (playerTurn == 2 && selectingMove) {
			if (v.getId() >= 4) {
				pieceSelected = v.getId();
				Piece [] pieceArray = players[1].getPieces;
				currentPiece = pieceArray[pieceSelected-4];
				ArrayList<Integer> moves = players[1].getAvailableMoves();
				if (movesPopup != null) {
					movesPopup.dismiss();
				}
				movesPopup = new PopupMenu(this, findViewbyId(pieceSelected));
				for (int i = 0; i < moves.size(); i++) {
					movesPopup.getMenu().add(""+moves[i]);
				}
				movesPopup.setOnMenuItemClickListener(this);
				movesPopup.show();
			}
		} else if (isRolling && v.getId() == rollbutton) {
			board.diceRoll();
		}
	}
	
	@Override
	public void onMenuItemClick(MenuItem item) {
		int moveChosen = Integer.parseInt(item.getTitle());
		this.players[this.playerTurn - 1].makeAvailableMove(pieceSelected - 
				(this.playerTurn-1)*4, moveChosen);
		this.movesPopup.dismiss();
		this.animatePieceMovement(currentPiece, pieceSelected);
		if (this.players[this.playerTurn-1].getAvailableMoves().size()==0){
			if (this.playerTurn == 1) {
				this.playerTurn++;
			} else if (this.playerTurn == 2) {
				this.playerTurn = 1;
			}
		}
	}
	
	public void animatePieceMovement(Piece p, int pId) {
		current = p.getLocation();
		
	}
}
