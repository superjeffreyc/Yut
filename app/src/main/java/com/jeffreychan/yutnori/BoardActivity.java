package com.jeffreychan.yutnori;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

public class BoardActivity extends GameActivity implements OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

	Context context = this;
	boolean isComputerPlaying;  // One player mode
	int COMPUTER_THINK_DURATION = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);

		isComputerPlaying = getIntent().getExtras().getBoolean("Computer");

	}

	/*
	 * Returns the layout for this activity
	 */
	@Override
	protected int getLayoutId(){
		return R.layout.activity_board;
	}

	/*
	 * Shows an AlertDialog warning the user that the current game will not be saved upon exit.
	 */
	@Override
	public void onBackPressed() {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		TextView tv = new TextView(this);
		tv.setPadding(0, 40, 0, 40);
		tv.setText("Return to main menu?\nThe game will not be saved.");
		tv.setTextSize(20f);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		adb.setView(tv);
		adb.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				quit();
			}
		});
		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		adb.show();
	}

	/**
	 * Click actions are not directly handled here. They are handled in the handleClick method.
	 *
	 * Once a player has won, prevent any further click events
	 * Otherwise, call the handleClick method if it is not the computer's turn
	 * Also, check that an animation is not in progress
	 *
	 * @param v The view being clicked on
	 */
	@Override
	public void onClick(View v) {
		if (isGameOver) return;
		if ((turn == 0 || !isComputerPlaying) && !isMoveInProgress) handleClick(v);
	}

	/**
	 * This method contains the main logic of the game.
	 *
	 * Handles the onClick actions. This allows the computer AI to call these actions while
	 * preventing the user from interfering with the computer's moves.
	 *
	 * @param v The view being clicked on
	 */
	protected void handleClick(View v){
		if (v.getId() == R.id.rollButton) { // Called when roll button is clicked
			handleRoll();
		}
		else if (v.getId() == finish.getId()) {
			movePiece(32, Move.NORMAL); // 32 = finish location
		}
		else if (v.getId() == offBoardPiece.getId()) {  // Image that represents both players' off board pieces
			showPossibleTiles(players[turn].findAvailablePiece());
		}
		else if (tile_ids.contains(v.getId())){    // Activates on tile click
			handleTileClick(v);
		}
		else if (player_ids.contains(v.getId())){  // Activates on animal click; animal covers tile
			handlePlayerClick(v);
		}
		else {
			hidePossibleTiles();    // Cancel move by clicking anything else
		}

	}

	/**
	 * Handles the logic for player character clicks. There are three possible situations
	 * when a player character is clicked:
	 *
	 * 1) User wants to see possible move locations
	 * 2) User wants to stack multiple team piece(s)
	 * 3) User wants to capture opponent's piece(s)
	 *
	 * @param v The player character being clicked on
	 */
	protected void handlePlayerClick(View v){
		if (isRollDone) {
			for (int i = 0; i < 4; i++) {

				int myLocation = players[turn].pieces[i].getLocation();
				int oppLocation = players[oppTurn].pieces[i].getLocation();

				// Show possible move locations
				if (v.getId() == playerOnBoardImages[turn][i].getId() && myLocation != -1 && myLocation != 32 && !isMarked[players[turn].pieces[i].getLocation()]) {
					showPossibleTiles(i);
				}
				// Same team
				else if (v.getId() == playerOnBoardImages[turn][i].getId() && myLocation != -1 && myLocation != 32 && isMarked[players[turn].pieces[i].getLocation()]) {
					movePiece(players[turn].pieces[i].getLocation(), Move.STACK);
				}
				// Opponent's pieces
				else if (v.getId() == playerOnBoardImages[oppTurn][i].getId() && oppLocation != -1 && oppLocation != 32 && isMarked[players[oppTurn].pieces[i].getLocation()]) {
					movePiece(players[oppTurn].pieces[i].getLocation(), Move.CAPTURE);
				}
			}
		}
	}

	/**
	 * Handles the determination of the amount rolled when the roll button is clicked
	 * Decides what should happen next based on roll.
	 * Ex: Rolling 4 or 5 allows the user to roll again. Rolling -1 with no pieces on the board ends the turn.
	 *
	 * Once the rolling phase is completed, prompt the user to make a move with an appropriate message
	 */
	protected void handleRoll(){
		rollAmount = board.throwSticks();
		rollButton.setVisibility(View.INVISIBLE);
		turnText.setVisibility(View.INVISIBLE);

		board.addRoll(rollAmount);
		final int currentIndex = rollSlotIndex;

		if ((rollAmount == 4 || rollAmount == 5) && rollSlotIndex < 4) {
			rollSlotIndex++;
			canRoll = true;
		}
		else if (rollAmount == -1 && rollSlotIndex == 0 && players[turn].hasNoPiecesOnBoard()) {
			isEndTurn = true;
			canRoll = false;
		}
		else {
			canRoll = false;
		}

		switch (rollAmount) {
			case -1:
				sticks.setBackgroundResource(R.drawable.fallingstickanimationminus1);
				break;
			case 1:
				sticks.setBackgroundResource(R.drawable.fallingstickanimation1);
				break;
			case 2:
				sticks.setBackgroundResource(R.drawable.fallingstickanimation2);
				break;
			case 3:
				sticks.setBackgroundResource(R.drawable.fallingstickanimation3);
				break;
			case 4:
				sticks.setBackgroundResource(R.drawable.fallingstickanimation4);
				break;
			case 5:
				sticks.setBackgroundResource(R.drawable.fallingstickanimation5);
				break;
			default:
		}

		fallingSticks = (AnimationDrawable) sticks.getBackground();
		sticks.setVisibility(View.VISIBLE);
		sticks.bringToFront();
		fallingSticks.setVisible(true, false);
		fallingSticks.stop();
		fallingSticks.start();

		// Wait until roll finishes before displaying roll value
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				updateRollArray(currentIndex, rollAmount);

				if ((rollAmount == 4 || rollAmount == 5) && rollSlotIndex < 4) {
					updateTurnText();
				}
			}
		}, 990);

		// Hide the sticks 1 second after the roll is shown
		Handler handler2 = new Handler();
		handler2.postDelayed(new Runnable() {
			public void run() {

				hideSticks();

				if (isEndTurn) endTurn();
				else if (canRoll) {
					if (turn == 0 || !isComputerPlaying) rollButton.setVisibility(View.VISIBLE);
					else handleComputerRoll();
				}
				else {
					isRollDone = true;

					int posCount = 0;
					for (int i : board.rollArray) {
						if (i != 0 && i != -1) {
							posCount++;
							break;
						}
					}

					tips.setVisibility(View.VISIBLE);

					if (players[turn].getNumPieces() < 4 && posCount > 0) {
						offBoardPiece.setVisibility(View.VISIBLE);
						offBoardPieceAnimation.start();

						if (players[turn].hasNoPiecesOnBoard()) tips.setText(R.string.click_me);
					} else if (players[turn].hasAllPiecesOnBoard()){
						tips.setText(playerTips[turn]);
					}

					for (int j = 0; j < 4; j++){
						playerAnimation[turn][j].stop();
						playerAnimation[turn][j] = (AnimationDrawable) playerOnBoardImages[turn][j].getBackground();
						playerAnimation[turn][j].start();
					}

					if (isComputerPlaying && turn == 1) {
						tips.setText(R.string.computer);
						handleComputerMove();
					}
				}
			}
		}, 1990);
	}

	/**
	 * Land on your own piece.
	 *
	 * Sends all your other pieces at that location off the board but increases the value of the current piece.
	 */
	protected void stack(){
		for (int j = 0; j < 4; j++) {
			if (players[turn].pieces[j].getLocation() == currentPiece.getLocation() && currentPiece != players[turn].pieces[j]) {
				currentPiece.addValue(players[turn].pieces[j].getValue());

				// Check for achievement for stacking all 4
				if (client != null && client.isConnected() && isComputerPlaying && turn == 0 && currentPiece.getValue() == 4) Games.Achievements.unlock(client, getResources().getString(R.string.achievement_the_stack));

				playerOnBoardImages[turn][j].setX(-currentPieceImage.getWidth());
				players[turn].pieces[j].setLocation(-1);
				players[turn].pieces[j].resetValue();

				playerOnBoardImages[turn][j].setBackgroundResource(avatarIds[turn][1]);

			}
		}

		currentPieceImage.setBackgroundResource(avatarIds[turn][currentPiece.getValue()]);

		for (int j = 0; j < 4; j++){
			playerAnimation[turn][j].stop();
			playerAnimation[turn][j] = (AnimationDrawable) playerOnBoardImages[turn][j].getBackground();
			playerAnimation[turn][j].start();
		}
	}

	/**
	 * Land on an opponent's piece.
	 *
	 * Send all their pieces off the board and roll again.
	 */
	protected void capture(){
		for (int j = 0; j < 4; j++) {
			if (players[oppTurn].pieces[j].getLocation() == currentPiece.getLocation()) {
				playerOnBoardImages[oppTurn][j].setX(-currentPieceImage.getWidth());
				players[oppTurn].pieces[j].setLocation(-1);
				players[oppTurn].subtractNumPieces(players[oppTurn].pieces[j].getValue());
				players[oppTurn].pieces[j].resetValue();

				playerOnBoardImages[oppTurn][j].setBackgroundResource(avatarIds[oppTurn][1]);
			}
		}
		if (turn == 0 || !isComputerPlaying) rollButton.setVisibility(View.VISIBLE);
		capture = true;
	}

	/**
	 * Highlight possible move locations for the currently selected piece.
	 *
	 * @param i The index of the piece to be moved
	 */
	protected void showPossibleTiles(int i){

		hidePossibleTiles();

		currentPiece = players[turn].pieces[i];
		currentPieceImage = playerOnBoardImages[turn][i];

		moveSet = players[turn].pieces[i].calculateMoveset(board.rollArray);
		for (Integer[] move : moveSet) {
			int location = move[0];

			if (location == 32) finish.setVisibility(View.VISIBLE);
			else if (location != -1) {
				if (specialTiles.contains(location)){
					tiles[location].setBackgroundResource(R.drawable.orangemarkerflash);
				} else {
					tiles[location].setBackgroundResource(R.drawable.bluemarkerflash);
				}
				tilesAnimation[location] = (AnimationDrawable) tiles[location].getBackground();
				tilesAnimation[location].start();
				isMarked[location] = true;
			}
		}

		if (finish.getVisibility() == View.VISIBLE) tips.setText(R.string.click_finish);
		else {
			if (turn == 0 || (!isComputerPlaying && turn == 1)) tips.setText(R.string.click_yellow);
			else tips.setText(R.string.computer);
		}
	}

	/**
	 * If the user made a move (STACK, CAPTURE, or NORMAL),
	 * prepare the board for another move or end the turn
	 */
	protected void endMove(){

		if (isGameOver) return;

		if (players[turn].hasAllPiecesOnBoard() || capture){
			offBoardPiece.setVisibility(View.INVISIBLE);
			offBoardPieceAnimation.stop();
			offBoardPieceAnimation.selectDrawable(0);
		}

		if (capture) {
			updateTurnText();
			tips.setVisibility(View.INVISIBLE);

			isRollDone = false;
			canRoll = true;
		}

		hidePossibleTiles();
		updateOffBoardImages();

		if (capture && isComputerPlaying && turn == 1) handleComputerRoll();
		else if ((!capture && board.rollEmpty()) || (board.hasOnlyNegativeRoll() && players[turn].hasNoPiecesOnBoard())) endTurn();
		else if (!board.rollEmpty() && isComputerPlaying && turn == 1) handleComputerMove();

		capture = false;
	}

	/**
	 * End the current player's turn
	 */
	protected void endTurn(){
		board.endTurn();
		prepareForNextTurn();
		if (isComputerPlaying && turn == 1) handleComputerRoll();
	}

	/**
	 * Displays an AlertDialog with the winner and asks if the user wants to play again.
	 * Prevents buttons and text from appearing.
	 */
	protected void endGame(){

		isGameOver = true;

		updateTurnText();

		// Check for computer match and user won
		if (isComputerPlaying && players[0].hasWon()) {
			// Check if GoogleApiClient is connected
			if (client != null && client.isConnected()) {
				// Increment number of wins for white belt achievement
				Games.Achievements.increment(client, getResources().getString(R.string.achievement_white_belt), 1);
				// Achievement for beating computer once
				Games.Achievements.unlock(client, getResources().getString(R.string.achievement_first_victory));
				// Achievement for crossing finish with all 4 pieces against computer
				if (currentPiece.getValue() == 4) Games.Achievements.unlock(client, getResources().getString(R.string.achievement_full_stack_finish));
			}

			Shop.Instance.addCoins(3);
		}
		else {
			Shop.Instance.addCoins(1);
		}

		// Check for achievement for playing two player mode
		if (client != null && client.isConnected() && !isComputerPlaying) Games.Achievements.unlock(client, getResources().getString(R.string.achievement_two_player_battle));

		updateOffBoardImages();
		rollButton.setVisibility(View.INVISIBLE);
		tips.setVisibility(View.INVISIBLE);

		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		TextView tv = new TextView(this);
		tv.setPadding(0, 40, 0, 40);

		// Display message notifying winner and amount of coins earned
		String winner = "\nYou now have " + Shop.Instance.getCoins() + " coin(s)";
		if (players[0].hasWon() && isComputerPlaying) winner = "Player 1 wins!\n\nYou have earned 3 coins!" + winner;
		else if (players[0].hasWon() && !isComputerPlaying) winner = "Player 1 wins!\n\nYou have earned 1 coin!" + winner;
		else if (players[1].hasWon() && !isComputerPlaying) winner = "Player 2 wins!\n\nYou have earned 1 coin!" + winner;
		else winner = "Computer wins!\n\nYou have earned 1 coin!" + winner;
		tv.setText(winner);

		tv.setTextSize(20f);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		adb.setView(tv);
		adb.setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent(context, BoardActivity.class);
				intent.putExtra("Computer", isComputerPlaying);
				intent.putExtra("Song", mp.getCurrentPosition());
				if (client != null && client.isConnected()) intent.putExtra("SignedIn", "Connected");
				else intent.putExtra("SignedIn", "Disconnected");
				finish();
				startActivity(intent);
			}
		});
		adb.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				quit();
			}
		});
		adb.setNeutralButton("Rate", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				quit();
				Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.jeffreychan.yunnori"));
				startActivity(intent);
			}
		});
		adb.show();
	}

	/**
	 * Prepares the board for the next player's turn
	 */
	protected void prepareForNextTurn(){
		turn = board.getPlayerTurn();
		oppTurn = (turn + 1) % 2;

		offBoardPiece.setVisibility(View.INVISIBLE);
		offBoardPieceAnimation.stop();
		offBoardPieceAnimation.selectDrawable(0);

		tips.setVisibility(View.INVISIBLE);
		tips.setText(playerTips[turn]);
		if (turn == 1) {
			offBoardPiece.setBackgroundResource(avatarIds[turn][1]);

			bottomBar.setBackgroundResource(R.drawable.bar1);
			bottomBar.setAlpha(1.0f);
			topBar.setBackgroundResource(R.drawable.bar2);
			topBar.setAlpha(0.25f);
		} else {
			offBoardPiece.setBackgroundResource(avatarIds[turn][1]);

			topBar.setBackgroundResource(R.drawable.bar1);
			topBar.setAlpha(1.0f);
			bottomBar.setBackgroundResource(R.drawable.bar2);
			bottomBar.setAlpha(0.25f);
		}

		offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();

		for (int i = 0; i < 5; i++) {
			rollSlot[i].setBackgroundResource(R.drawable.white_marker);
		}

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				playerAnimation[i][j].stop();
				playerAnimation[i][j].selectDrawable(0);
			}
		}

		board.resetRollArray();
		hidePossibleTiles();

		rollAmount = 0;
		rollSlotIndex = 0;
		isRollDone = false;
		canRoll = true;
		isEndTurn = false;
		if (turn == 0 || !isComputerPlaying) rollButton.setVisibility(View.VISIBLE);

		updateTurnText();
	}

	/**
	 * Stop all tiles from flashing yellow and prompt user to select a piece
	 */
	protected void hidePossibleTiles() {

		finish.setVisibility(View.INVISIBLE);

		if (isRollDone) {
			if (players[turn].hasNoPiecesOnBoard()) tips.setText(R.string.click_me);
			else {
				if (turn == 0) tips.setText(playerTips[0]);
				else if (turn == 1 && !isComputerPlaying) tips.setText(playerTips[1]);
			}
		}

		if (isComputerPlaying && turn == 1) {
			tips.setText(R.string.computer);
		}

		for (int i = 0; i < MAX_TILES; i++) {
			isMarked[i] = false;

			if (!specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.blue_marker);
			} else if (specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.orange_marker);
			}
		}
	}

	/**
	 * Exits this activity
	 */
	protected void quit(){
		Intent intent = new Intent(this, TitleScreenActivity.class);
		intent.putExtra("Song", mp.getCurrentPosition());
		intent.putExtra("Board", true);
		if (client != null && client.isConnected()) intent.putExtra("SignedIn", "Connected");
		else intent.putExtra("SignedIn", "Disconnected");
		startActivity(intent);
		finish();
	}

	/**
	 * Handles the computer call to throw the sticks.
	 * Adds a delay of 1s to play at a reasonable speed.
	 */
	protected void handleComputerRoll(){
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				handleRoll();
			}
		}, COMPUTER_THINK_DURATION);
	}

	/**
	 * Handles how the computer will move the pieces
	 * Adds 2 delays of 1s each to show computer moves being made
	 */
	protected void handleComputerMove(){
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				int[] move = Computer.selectMove(players, board.rollArray);
				int piece = move[0];
				final int i = move[1];

				if (piece == -1 || players[1].pieces[piece].getLocation() == -1) handleClick(offBoardPiece);
				else handleClick(playerOnBoardImages[1][piece]);

				tips.setText(R.string.computer);

				Handler handler1 = new Handler();
				handler1.postDelayed(new Runnable() {
					@Override
					public void run(){
						if (i == 32) handleClick(finish);
						else handleClick(tiles[i]);
					}
				}, COMPUTER_THINK_DURATION);
			}
		}, COMPUTER_THINK_DURATION);
	}

	protected void updateTurnText() {
		if (isComputerPlaying) {
			if (isGameOver)
				turnText.setText(players[0].hasWon() ? R.string.you_win : R.string.computer_wins);
			else if (capture || rollAmount >= 4)
				turnText.setText(turn == 0 ? R.string.you_roll_again : R.string.computer_roll_again);
			else
				turnText.setText(turn == 0 ? R.string.your_turn : R.string.computer_turn);
		}
		else {
			if (isGameOver)
				turnText.setText(players[0].hasWon() ? R.string.player1_wins : R.string.player2_wins);
			else if (capture || rollAmount >= 4)
				turnText.setText(turn == 0 ? R.string.player1_roll_again : R.string.player2_roll_again);
			else
				turnText.setText(turn == 0 ? R.string.player1_turn : R.string.player2_turn);
		}

		turnText.setVisibility(View.VISIBLE);
	}
}
