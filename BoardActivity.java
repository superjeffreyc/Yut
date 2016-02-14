package com.jeffreychan.yutnori;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.widget.ImageView;

public class BoardActivity extends Activity {

	Board board;
	Player[] players = new Player[2];
	ImageView image;
	int playerTurn = 1;
	boolean isRunning = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);

		players[0] = new Player("Player 1");
		players[1] = new Player("Player 2");

		startGameLoop();
	}

	public void startGameLoop(){
		int[] rolls;
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
		getMenuInflater().inflate(R.menu.menu_board, menu);
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
}
