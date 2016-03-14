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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class BoardActivity extends Activity implements OnClickListener{

	Board board;
	Player[] players = new Player[2];
	ImageView[] playerOneImages, playerTwoImages, moveButtons;
	ImageView sticks, move1, move2, move3, move4, move5, moveminus1;
	AnimationDrawable fallingSticks;
	Button roll;
	int playerTurn = 1;
	boolean isRunning = true;
	int pieceSelected = 0;
	ArrayList<Integer> rolls;
	int rollAmount;
	TextView rollText, playerTurnText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_board);

		players[0] = new Player("Player 1");
		players[1] = new Player("Player 2");
		board = new Board();

		playerOneImages = new ImageView[4];
		playerTwoImages = new ImageView[4];

		move1 = (ImageView) findViewById(R.id.move1);
		move2 = (ImageView) findViewById(R.id.move2);
		move3 = (ImageView) findViewById(R.id.move3);
		move4 = (ImageView) findViewById(R.id.move4);
		move5 = (ImageView) findViewById(R.id.move5);
		moveminus1 = (ImageView) findViewById(R.id.moveminus1);
		rollText = (TextView) findViewById(R.id.rollText);
		rollText.setTextColor(Color.BLACK);
		playerTurnText = (TextView) findViewById(R.id.playerTurn);
		playerTurnText.setTextColor(Color.RED);

		move1.setBackgroundResource(R.drawable.move1);
		move2.setBackgroundResource(R.drawable.move2);
		move3.setBackgroundResource(R.drawable.move3);
		move4.setBackgroundResource(R.drawable.move4);
		move5.setBackgroundResource(R.drawable.move5);
		moveminus1.setBackgroundResource(R.drawable.moveminus1);


		sticks = (ImageView) findViewById(R.id.sticks);
		sticks.setBackgroundResource(R.drawable.fallingstickanimation);
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
		*/
		if (v.getId() == R.id.rollButton){


			roll.setVisibility(View.INVISIBLE);
			move1.setVisibility(View.INVISIBLE);
			move2.setVisibility(View.INVISIBLE);
			move3.setVisibility(View.INVISIBLE);
			move4.setVisibility(View.INVISIBLE);
			move5.setVisibility(View.INVISIBLE);
			moveminus1.setVisibility(View.INVISIBLE);
			rollText.setText("");
			playerTurnText.setText("");
			sticks.setVisibility(View.VISIBLE);
			fallingSticks.setVisible(true, false);
			fallingSticks.stop();
			fallingSticks.start();


			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					rollAmount = throwSticks();

					switch (rollAmount) {
						case -1:
							moveminus1.setVisibility(View.VISIBLE);
							rollText.setText("-1");
							break;
						case 1:
							move1.setVisibility(View.VISIBLE);
							rollText.setText("1");
							break;
						case 2:
							move2.setVisibility(View.VISIBLE);
							rollText.setText("2");
							break;
						case 3:
							move3.setVisibility(View.VISIBLE);
							rollText.setText("3");
							break;
						case 4:
							move4.setVisibility(View.VISIBLE);
							rollText.setText("4");
							break;
						case 5:
							move5.setVisibility(View.VISIBLE);
							rollText.setText("5");
							break;
						default:

					}

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
					moveminus1.setVisibility(View.INVISIBLE);

					roll.setVisibility(View.VISIBLE);

					if (playerTurn == 1) {
						playerTurn = 2;
						playerTurnText.setText("Player 2 Turn");
					}
					else {
						playerTurn = 1;
						playerTurnText.setText("Player 1 Turn");
					}

				}
			}, 1900);


		}

	}

	public int throwSticks(){
		int roll = 1;

		int num = (int) (Math.random() * 16) + 1;

		if (num == 1) roll = -1;
		else if (num > 1 && num <= 4) roll = 1;
		else if (num > 4 && num <= 10) roll = 2;
		else if (num > 10 && num <= 14) roll = 3;
		else if (num > 14 && num <= 15) roll = 4;
		else if (num > 15 && num <= 16) roll = 5;

		return roll;
	}
}
