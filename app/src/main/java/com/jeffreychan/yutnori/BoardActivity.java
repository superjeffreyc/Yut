package com.jeffreychan.yutnori;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	boolean isComputerPlaying;
	int COMPUTER_THINK_DURATION = 1000;

	/*
	 * Called when this activity is launched for the first time
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);

		isComputerPlaying = getIntent().getExtras().getBoolean("Computer");
		updateTurnText();
	}

	/*
	 * Returns the layout for this activity
	 */
	@Override
	protected int getLayoutId(){
		return R.layout.activity_board;
	}

	/***
	 * If user is allowed to click, call the handleClick method.
	 *
	 * @param v The view being clicked on
	 */
	@Override
	public void onClick(View v) {
		if (isGameOver) return;
		if (rollButton.getVisibility() == View.VISIBLE && v.getId() != rollButton.getId()) return;
		if (isMoveInProgress || isRollInProgress || (turn == 1 && isComputerPlaying)) return;

		handleClick(v);
	}

	/**
	 * This method contains the main logic of the game.
	 *
	 * @param v The view being clicked on
	 */
	protected void handleClick(View v){
		if (v.getId() == R.id.rollButton) { // Called when roll button is clicked
			handleRoll(0);
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
	 * Land on your own piece.
	 *
	 * Sends all your other pieces at that location off the board but increases the value of the current piece.
	 */
	protected void stack(){
		// Check for achievement for stacking all 4
		for (int j = 0; j < 4; j++) {
			if (players[turn].pieces[j].getLocation() == currentPiece.getLocation() && currentPiece != players[turn].pieces[j]) {
				if (client != null && client.isConnected() && isComputerPlaying && turn == 0 && currentPiece.getValue() == 4) Games.Achievements.unlock(client, getResources().getString(R.string.achievement_the_stack));
			}
		}

		super.stack();
	}

	/**
	 * Land on an opponent's piece.
	 *
	 * Send all their pieces off the board and roll again.
	 */
	protected void capture(){
		super.capture();
		if (!isComputerPlaying) rollButton.setVisibility(View.VISIBLE);
	}

	/**
	 * End the current player's turn
	 */
	protected void endTurn(){
		super.endTurn();
		if (isComputerPlaying && turn == 1) handleComputerRoll();
	}

	/**
	 * Prepares the board for the next player's turn
	 */
	protected void prepareForNextTurn(){
		super.prepareForNextTurn();
		if (!isComputerPlaying) rollButton.setVisibility(View.VISIBLE);
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
				handleRoll(0);
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
				int[] move = Computer.selectMove(players, board.getRollArray());
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

	public void handleCanRoll() {
		if (turn == 0 || !isComputerPlaying) rollButton.setVisibility(View.VISIBLE);
		else handleComputerRoll();
	}

	public void handleTipsUpdate() {
		if (isComputerPlaying && turn == 1) {
			tips.setText(R.string.computer);
			handleComputerMove();
		}
	}

	protected void updateTipsText(String s) {
		switch (s) {
			case "Hide":
				if (players[turn].hasNoPiecesOnBoard()) tips.setText(R.string.click_me);
				else {
					if (turn == 0) tips.setText(playerTips[0]);
					else if (turn == 1 && !isComputerPlaying) tips.setText(playerTips[1]);
				}
				break;
			case "Show":
				if (finish.getVisibility() == View.VISIBLE) tips.setText(R.string.click_finish);
				else {
					if (turn == 0 || (!isComputerPlaying && turn == 1)) tips.setText(R.string.click_yellow);
					else tips.setText(R.string.computer);
				}
				break;
		}
	}


	public void handleEndMove() {
		if (capture && isComputerPlaying && turn == 1) handleComputerRoll();
		else if ((!capture && board.rollEmpty()) || (board.hasOnlyNegativeRoll() && players[turn].hasNoPiecesOnBoard())) endTurn();
		else if (!board.rollEmpty() && isComputerPlaying && turn == 1) handleComputerMove();
	}

	protected void awardCoins() {
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

	}

	protected void showGameOverDialog() {
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
}
