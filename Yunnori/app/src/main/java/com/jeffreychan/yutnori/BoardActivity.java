package com.jeffreychan.yutnori;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class BoardActivity extends Activity implements OnClickListener{

	Board board;
	Player[] players = new Player[2];
	ImageView[] playerOneImages, playerTwoImages, moveButtons, rollSlot, tiles;
	ImageView sticks, move1, move2, move3, move4, move5, moveMinus1;
	AnimationDrawable fallingSticks;
	Button roll;
	int rollAmount, turn = 0, counter = 0, width, height;
	TextView rollText;
	LinearLayout topBar, bottomBar, boardLayout;
	int[] playerOneCompleted, playerTwoCompleted;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_board);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;

		players[0] = new Player("Player 1");
		players[1] = new Player("Player 2");
		rollSlot = new ImageView[5];
		board = new Board();
		boardLayout = (LinearLayout) findViewById(R.id.board);

		topBar = (LinearLayout) findViewById(R.id.topBar);
		bottomBar = (LinearLayout) findViewById(R.id.bottomBar);

		playerOneImages = new ImageView[4];
		playerTwoImages = new ImageView[4];

		tiles = new ImageView[29];
		tiles[0] = (ImageView) findViewById(R.id.location0);
		tiles[1] = (ImageView) findViewById(R.id.location1);
		tiles[2] = (ImageView) findViewById(R.id.location2);
		tiles[3] = (ImageView) findViewById(R.id.location3);
		tiles[4] = (ImageView) findViewById(R.id.location4);
		tiles[5] = (ImageView) findViewById(R.id.location5);
		tiles[6] = (ImageView) findViewById(R.id.location6);
		tiles[7] = (ImageView) findViewById(R.id.location7);
		tiles[8] = (ImageView) findViewById(R.id.location8);
		tiles[9] = (ImageView) findViewById(R.id.location9);
		tiles[10] = (ImageView) findViewById(R.id.location10);
		tiles[11] = (ImageView) findViewById(R.id.location11);
		tiles[12] = (ImageView) findViewById(R.id.location12);
		tiles[13] = (ImageView) findViewById(R.id.location13);
		tiles[14] = (ImageView) findViewById(R.id.location14);
		tiles[15] = (ImageView) findViewById(R.id.location15);
		tiles[16] = (ImageView) findViewById(R.id.location16);
		tiles[17] = (ImageView) findViewById(R.id.location17);
		tiles[18] = (ImageView) findViewById(R.id.location18);
		tiles[19] = (ImageView) findViewById(R.id.location19);

//		for (ImageView iv : tiles){	iv.setOnClickListener(this); }

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
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		topBar.setMinimumHeight(height/5);
	//	boardLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, width));

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
