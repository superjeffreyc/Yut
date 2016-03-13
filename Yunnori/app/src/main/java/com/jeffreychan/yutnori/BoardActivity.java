package com.jeffreychan.yutnori;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class BoardActivity extends Activity implements OnClickListener{

	Board board;
	Player[] players = new Player[2];
	ImageView[] playerOneImages, playerTwoImages, moveButtons;
	ImageView sticks;
	AnimationDrawable fallingSticks;
	Button roll;
	int playerTurn = 1;
	boolean isRunning = true;
	int pieceSelected = 0;
	ArrayList<Integer> rolls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);
		players[0] = new Player("Player 1");
		players[1] = new Player("Player 2");
		board = new Board();

		playerOneImages = new ImageView[4];
		playerTwoImages = new ImageView[4];
		sticks = (ImageView) findViewById(R.id.sticks);
		fallingSticks = (AnimationDrawable) sticks.getBackground();

		roll = (Button) findViewById(R.id.rollButton);
		roll.setOnClickListener(this);


		for (int i = 0; i < 4; i++){
			playerOneImages[i] = new ImageView(this);
			playerTwoImages[i] = new ImageView(this);

			playerOneImages[i].setId(i);
			playerOneImages[i].setOnClickListener(this);
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
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//
//		//noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings) {
//			return true;
//		}

		return super.onOptionsItemSelected(item);
	}

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
				finish();
			}
		});
		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		adb.show();
	}

	@Override
	public void onClick(View v) {
/*		if (playerTurn == 1) {
			if (v.getId() == playerOneImages[0].getId()) {
				//popup
				// alert dialog
			} else if (v.getId() == playerOneImages[1].getId()) {

			} else if (v.getId() == playerOneImages[2].getId()) {

			} else if (v.getId() == playerOneImages[3].getId()) {

			}
		}
		else if (playerTurn == 2) {
			if (v.getId() == playerTwoImages[0].getId()) {

			} else if (v.getId() == playerTwoImages[1].getId()) {

			} else if (v.getId() == playerTwoImages[2].getId()) {

			} else if (v.getId() == playerTwoImages[3].getId()) {

			}
		}
*/
		if (v.getId() == moveButtons[0].getId() && pieceSelected == 1){
			//players[0].getPiece(0).handleMovement()
		}
		else if (v.getId() == moveButtons[1].getId() && pieceSelected == 1){

		}
		else if (v.getId() == moveButtons[2].getId() && pieceSelected == 1){

		}
		else if (v.getId() == moveButtons[3].getId() && pieceSelected == 1){

		}
		else if (v.getId() == moveButtons[4].getId() && pieceSelected == 1) {

		}
		else if (v.getId() == R.id.rollButton){
			fallingSticks.start();
		}

	}
}
