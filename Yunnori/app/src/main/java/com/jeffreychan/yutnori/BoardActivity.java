package com.jeffreychan.yutnori;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BoardActivity extends Activity implements OnClickListener{

	Board board;
	Player[] players = new Player[2];
	ImageView[] playerOneImages, playerTwoImages, moveButtons, rollSlot;
	ImageView sticks, move1, move2, move3, move4, move5, moveMinus1;
	AnimationDrawable fallingSticks;
	Button roll;
	int rollAmount, turn = 0, counter = 0;
	TextView rollText;
	LinearLayout topBar, bottomBar;
	int[] playerOneCompleted, playerTwoCompleted;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_board);

		players[0] = new Player("Player 1");
		players[1] = new Player("Player 2");
		rollSlot = new ImageView[5];
		board = new Board();

		topBar = (LinearLayout) findViewById(R.id.topBar);
		bottomBar = (LinearLayout) findViewById(R.id.bottomBar);

		playerOneImages = new ImageView[4];
		playerTwoImages = new ImageView[4];

		move1 = (ImageView) findViewById(R.id.move1);
		move2 = (ImageView) findViewById(R.id.move2);
		move3 = (ImageView) findViewById(R.id.move3);
		move4 = (ImageView) findViewById(R.id.move4);
		move5 = (ImageView) findViewById(R.id.move5);
		moveMinus1 = (ImageView) findViewById(R.id.moveminus1);
		rollText = (TextView) findViewById(R.id.rollText);
		rollText.setTextColor(Color.BLACK);

		rollSlot[0] = (ImageView) findViewById(R.id.rollSlot1);
		rollSlot[1] = (ImageView) findViewById(R.id.rollSlot2);
		rollSlot[2] = (ImageView) findViewById(R.id.rollSlot3);
		rollSlot[3] = (ImageView) findViewById(R.id.rollSlot4);
		rollSlot[4] = (ImageView) findViewById(R.id.rollSlot5);

		move1.setBackgroundResource(R.drawable.move1);
		move2.setBackgroundResource(R.drawable.move2);
		move3.setBackgroundResource(R.drawable.move3);
		move4.setBackgroundResource(R.drawable.move4);
		move5.setBackgroundResource(R.drawable.move5);
		moveMinus1.setBackgroundResource(R.drawable.moveminus1);

		sticks = (ImageView) findViewById(R.id.sticks);
		sticks.setBackgroundResource(R.drawable.fallingstickanimation);
		fallingSticks = (AnimationDrawable) sticks.getBackground();
		sticks.setVisibility(View.INVISIBLE);
		fallingSticks.setVisible(false, false);

		roll = (Button) findViewById(R.id.rollButton);
		roll.setOnClickListener(this);

		playerOneImages[0] = (ImageView) findViewById(R.id.seal1);
		playerOneImages[1] = (ImageView) findViewById(R.id.seal2);
		playerOneImages[2] = (ImageView) findViewById(R.id.seal3);
		playerOneImages[3] = (ImageView) findViewById(R.id.seal4);

		playerTwoImages[0] = (ImageView) findViewById(R.id.penguin1);
		playerTwoImages[1] = (ImageView) findViewById(R.id.penguin2);
		playerTwoImages[2] = (ImageView) findViewById(R.id.penguin3);
		playerTwoImages[3] = (ImageView) findViewById(R.id.penguin4);

		for (int i = 0; i < 4; i++){
			playerOneImages[i].setOnClickListener(this);
			playerTwoImages[i].setOnClickListener(this);
		}

		moveButtons = new ImageView[5];
		for (int i = 0; i < 4; i++){
			moveButtons[i] = new ImageView(this);
			moveButtons[i].setId(i + 10);
			moveButtons[i].setOnClickListener(this);

		}

		playerOneCompleted = new int[4];
		playerTwoCompleted = new int[4];

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

		if (v.getId() == R.id.seal1) {
			playerOneCompleted[0] = (playerOneCompleted[0] + 1) % 2;
			if (playerOneCompleted[0] == 1)	playerOneImages[0].setBackgroundResource(R.drawable.sealfaded);
			else playerOneImages[0].setBackgroundResource(R.drawable.seal1);
		}
		else if (v.getId() == R.id.seal2) {
			playerOneCompleted[1] = (playerOneCompleted[1] + 1) % 2;
			if (playerOneCompleted[1] == 1)	playerOneImages[1].setBackgroundResource(R.drawable.sealfaded);
			else playerOneImages[1].setBackgroundResource(R.drawable.seal1);
		}
		else if (v.getId() == R.id.seal3) {
			playerOneCompleted[2] = (playerOneCompleted[2] + 1) % 2;
			if (playerOneCompleted[2] == 1)	playerOneImages[2].setBackgroundResource(R.drawable.sealfaded);
			else playerOneImages[2].setBackgroundResource(R.drawable.seal1);
		}
		else if (v.getId() == R.id.seal4) {
			playerOneCompleted[3] = (playerOneCompleted[3] + 1) % 2;
			if (playerOneCompleted[3] == 1)	playerOneImages[3].setBackgroundResource(R.drawable.sealfaded);
			else playerOneImages[3].setBackgroundResource(R.drawable.seal1);
		}
		else if (v.getId() == R.id.penguin1) {
			playerTwoCompleted[0] = (playerTwoCompleted[0] + 1) % 2;
			if (playerTwoCompleted[0] == 1)	playerTwoImages[0].setBackgroundResource(R.drawable.penguinfaded);
			else playerTwoImages[0].setBackgroundResource(R.drawable.penguin1);
		}
		else if (v.getId() == R.id.penguin2) {
			playerTwoCompleted[1] = (playerTwoCompleted[1] + 1) % 2;
			if (playerTwoCompleted[1] == 1)	playerTwoImages[1].setBackgroundResource(R.drawable.penguinfaded);
			else playerTwoImages[1].setBackgroundResource(R.drawable.penguin1);
		}
		else if (v.getId() == R.id.penguin3) {
			playerTwoCompleted[2] = (playerTwoCompleted[2] + 1) % 2;
			if (playerTwoCompleted[2] == 1)	playerTwoImages[2].setBackgroundResource(R.drawable.penguinfaded);
			else playerTwoImages[2].setBackgroundResource(R.drawable.penguin1);
		}
		else if (v.getId() == R.id.penguin4) {
			playerTwoCompleted[3] = (playerTwoCompleted[3] + 1) % 2;
			if (playerTwoCompleted[3] == 1)	playerTwoImages[3].setBackgroundResource(R.drawable.penguinfaded);
			else playerTwoImages[3].setBackgroundResource(R.drawable.penguin1);
		}
		else if (v.getId() == R.id.rollButton){
			roll.setVisibility(View.INVISIBLE);
			move1.setVisibility(View.INVISIBLE);
			move2.setVisibility(View.INVISIBLE);
			move3.setVisibility(View.INVISIBLE);
			move4.setVisibility(View.INVISIBLE);
			move5.setVisibility(View.INVISIBLE);
			moveMinus1.setVisibility(View.INVISIBLE);
			rollText.setText("");
			sticks.setVisibility(View.VISIBLE);
			fallingSticks.setVisible(true, false);
			fallingSticks.stop();
			fallingSticks.start();

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					rollAmount = board.throwSticks();
					board.addRoll(rollAmount);

					switch (rollAmount) {
						case -1:
							moveMinus1.setVisibility(View.VISIBLE);
							rollText.setText("-1");
							rollSlot[counter].setBackgroundResource(R.drawable.circleminus1);
							break;
						case 1:
							move1.setVisibility(View.VISIBLE);
							rollText.setText("1");
							rollSlot[counter].setBackgroundResource(R.drawable.circle1);
							break;
						case 2:
							move2.setVisibility(View.VISIBLE);
							rollText.setText("2");
							rollSlot[counter].setBackgroundResource(R.drawable.circle2);
							break;
						case 3:
							move3.setVisibility(View.VISIBLE);
							rollText.setText("3");
							rollSlot[counter].setBackgroundResource(R.drawable.circle3);
							break;
						case 4:
							move4.setVisibility(View.VISIBLE);
							rollText.setText("4");
							rollSlot[counter].setBackgroundResource(R.drawable.circle4);
							break;
						case 5:
							move5.setVisibility(View.VISIBLE);
							rollText.setText("5");
							rollSlot[counter].setBackgroundResource(R.drawable.circle5);
							break;
						default:

					}

					counter++;

				}
			}, 900);

			Handler handler2 = new Handler();
			handler2.postDelayed(new Runnable() {
				public void run() {

					rollText.setText("");
					sticks.setVisibility(View.INVISIBLE);
					fallingSticks.setVisible(false, false);
					move1.setVisibility(View.INVISIBLE);
					move2.setVisibility(View.INVISIBLE);
					move3.setVisibility(View.INVISIBLE);
					move4.setVisibility(View.INVISIBLE);
					move5.setVisibility(View.INVISIBLE);
					moveMinus1.setVisibility(View.INVISIBLE);

					roll.setVisibility(View.VISIBLE);

					if (board.getPlayerTurn() == 1 && turn == 0) {
						turn = board.getPlayerTurn();
						bottomBar.setBackgroundResource(R.color.Orange);
						topBar.setBackgroundResource(R.color.LighterBlue);
						reset();
					} else if (board.getPlayerTurn() == 0 && turn == 1){
						turn = board.getPlayerTurn();
						topBar.setBackgroundResource(R.color.Orange);
						bottomBar.setBackgroundResource(R.color.LighterBlue);
						reset();
					}

					}

			}, 1900);
		}
	}


	public void reset(){
		for (int i = 0; i < 5; i++) {
			rollSlot[i].setBackgroundResource(R.drawable.white_marker);
		}
		board.resetRollArray();
		counter = 0;
	}

}
