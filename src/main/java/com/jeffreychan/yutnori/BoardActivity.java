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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeSet;

public class BoardActivity extends Activity implements OnClickListener{

	Board board;
	Player[] players;
	ImageView[] playerOneImages, playerTwoImages, moveButtons, rollSlot, tiles;
	ImageView sticks, move1, move2, move3, move4, move5, moveMinus1, playerIcon;
	AnimationDrawable fallingSticks, rollFlash, playerIconAnimation;
	Button roll;
	int rollAmount, turn = 0, counter = 0, width, height, playerOneCurrentPiece = 0, playerTwoCurrentPiece = 0, MAX_TILES = 20;
	TextView rollText;
	LinearLayout topBar, bottomBar;
	int[] playerOneCompleted, playerTwoCompleted;
	boolean[] isPlayerOnePieceSelected, isPlayerTwoPieceSelected, isPlayerOnePieceDone, isPlayerTwoPieceDone, isMarked;
	boolean isReady, canRoll = true, isEndTurn;
	TreeSet<Integer> specialTiles = new TreeSet<>();
	RelativeLayout rl;
	ArrayList<Integer> ids = new ArrayList<>();

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

		rl = (RelativeLayout) findViewById(R.id.rl);

		players = new Player[2];
		players[0] = new Player();
		players[1] = new Player();

		rollSlot = new ImageView[5];
		board = new Board();
		isPlayerOnePieceSelected = new boolean[4];
		isPlayerTwoPieceSelected = new boolean[4];
		isPlayerOnePieceDone = new boolean[4];
		isPlayerTwoPieceDone = new boolean[4];
		for (int i = 0; i < 4; i++){
			isPlayerOnePieceSelected[i] = true;
			isPlayerTwoPieceSelected[i] = true;
		}

		specialTiles.add(0);
		specialTiles.add(5);
		specialTiles.add(10);
		specialTiles.add(15);

		playerIcon = (ImageView) findViewById(R.id.playerIcon);
		playerIcon.setBackgroundResource(R.drawable.sealmoveanimation);
		playerIconAnimation = (AnimationDrawable) playerIcon.getBackground();
		playerIcon.setOnClickListener(this);

		topBar = (LinearLayout) findViewById(R.id.topBar);
		bottomBar = (LinearLayout) findViewById(R.id.bottomBar);

		playerOneImages = new ImageView[4];
		playerTwoImages = new ImageView[4];

		isMarked = new boolean[29];
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
		for (int i = 0; i < MAX_TILES; i++){
			tiles[i].setOnClickListener(this);
			ids.add(tiles[i].getId());
		}

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
		roll.setBackgroundResource(R.drawable.rollflashanimation);
		roll.setOnClickListener(this);
		rollFlash = (AnimationDrawable) roll.getBackground();
		rollFlash.start();

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


//		for (int i = 20; i < 29; i++) {
//			tiles[i] = new ImageView(this);
//
//			if (i == 22) tiles[i].setBackgroundResource(R.drawable.orange_marker);
//			else tiles[i].setBackgroundResource(R.drawable.blue_marker);
//
//			tiles[i].setLayoutParams(new LayoutParams(50, 50));
//			tiles[i].setOnClickListener(this);
//			rl.addView(tiles[i]);
//		}



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
//
//		int w = tiles[10].getWidth();
//		for (int i = 20; i < 29; i++){
//			tiles[i].getLayoutParams().width = w;
//			tiles[i].getLayoutParams().height = w;
//		}
//
//		tiles[25].setX(tiles[10].getX() + (float) 1.4 * w);
//		tiles[25].setY(tiles[10].getY() + (float) 3.2 * w);
//
//		tiles[26].setX(tiles[25].getX() + (float) 1.2 * w);
//		tiles[26].setY(tiles[25].getY() + (float) 1.2 * w);
//
//		tiles[22].setX(tiles[26].getX() + (float) 1.2 * w);
//		tiles[22].setY(tiles[26].getY() + (float) 1.2 * w);
//
//		tiles[27].setX(tiles[22].getX() + (float) 1.2 * w);
//		tiles[27].setY(tiles[22].getY() + (float) 1.2 * w);
//
//		tiles[28].setX(tiles[27].getX() + (float) 1.2 * w);
//		tiles[28].setY(tiles[27].getY() + (float) 1.2 * w);
//
//		tiles[20].setX(tiles[28].getX());
//		tiles[20].setY(tiles[25].getY());
//
//		tiles[21].setX(tiles[27].getX());
//		tiles[21].setY(tiles[26].getY());
//
//		tiles[23].setX(tiles[26].getX());
//		tiles[23].setY(tiles[27].getY());
//
//		tiles[24].setX(tiles[25].getX());
//		tiles[24].setY(tiles[28].getY());

	}

	private void showPossibleTiles(int pl, int pi){
		int[] moveSet = players[pl].pieces[pi].tempCalculateMoveset(board.rollArray);
		for (int move : moveSet) {
			if (move != -1) {
				tiles[move].setBackgroundResource(R.drawable.red_marker);
				isMarked[move] = true;
			}
		}
	}

	private void hidePossibleTiles() {
		for (int i = 0; i < MAX_TILES; i++) {
			isMarked[i] = false;

			if (!specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.blue_marker);
			} else if (specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.orange_marker);
			}

		}
	}

	private void highlightPlayerImages(int pl, int piece){

		if (pl == 0){
			playerOneCompleted[piece] = (playerOneCompleted[piece] + 1) % 2;
			if (playerOneCompleted[piece] == 1) {
				playerIconAnimation.stop();
				playerIcon.setBackgroundResource(R.drawable.seal_highlighted);
				showPossibleTiles(0, piece);
			} else {
				playerIcon.setBackgroundResource(R.drawable.sealmoveanimation);
				playerIconAnimation = (AnimationDrawable) playerIcon.getBackground();
				playerIconAnimation.start();
				hidePossibleTiles();
			}
		} else {
			playerTwoCompleted[piece] = (playerTwoCompleted[piece] + 1) % 2;
			if (playerTwoCompleted[piece] == 1) {
				playerIcon.setBackgroundResource(R.drawable.penguin_highlighted);
				showPossibleTiles(1, piece);
			} else {
				playerIcon.setBackgroundResource(R.drawable.penguinjumpanimation);
				playerIconAnimation = (AnimationDrawable) playerIcon.getBackground();
				playerIconAnimation.start();
				hidePossibleTiles();
			}
		}
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.playerIcon && turn == 0 && isReady) {
			for (int i = 1; i < 4; i++){
				isPlayerOnePieceSelected[i] = !isPlayerOnePieceSelected[i];
			}
			highlightPlayerImages(0, 0);
		}
		else if (v.getId() == R.id.playerIcon && turn == 1 && isReady) {
			for (int i = 1; i < 4; i++){
				isPlayerTwoPieceSelected[i] = !isPlayerTwoPieceSelected[i];
			}
			highlightPlayerImages(1, 0);
		}
		else if (ids.contains(v.getId())){
			for (int i = 0; i < MAX_TILES; i++){
				if (v.getId() == tiles[i].getId() && isMarked[i]){
					// TODO: Continue game logic
					endTurn();
				}
			}
		}
		else if (v.getId() == R.id.rollButton) {
			handleRoll();
		}

	}

	private void handleRoll(){
		roll.setVisibility(View.INVISIBLE);
		sticks.setVisibility(View.VISIBLE);
		fallingSticks.setVisible(true, false);
		fallingSticks.stop();
		fallingSticks.start();

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				rollAmount = board.throwSticks();
				showRoll(rollAmount);

				if ((rollAmount == 4 || rollAmount == 5) && counter < 4) counter++;
				else if (rollAmount == -1 && counter == 0) isEndTurn = true;
				else {
					canRoll = false;
					isReady = true;
				}

			}
		}, 900);

		Handler handler2 = new Handler();
		handler2.postDelayed(new Runnable() {
			public void run() {

				hideSticks();

				if (isEndTurn) endTurn();
				else if (canRoll) roll.setVisibility(View.VISIBLE);
				else {
					if ((turn == 0 && playerOneCurrentPiece < 4) || (turn == 1 && playerTwoCurrentPiece < 4)){
						playerIcon.setVisibility(View.VISIBLE);
						playerIconAnimation.start();
					}
				}
			}

		}, 1900);
	}

	private void showRoll(int rollAmount){
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
	}

	private void hideSticks(){
		rollText.setText("");
		sticks.setVisibility(View.INVISIBLE);
		fallingSticks.setVisible(false, false);
		move1.setVisibility(View.INVISIBLE);
		move2.setVisibility(View.INVISIBLE);
		move3.setVisibility(View.INVISIBLE);
		move4.setVisibility(View.INVISIBLE);
		move5.setVisibility(View.INVISIBLE);
		moveMinus1.setVisibility(View.INVISIBLE);
	}

	private void endTurn(){
		board.endTurn();
		reset();
	}

	private void reset(){
		turn = board.getPlayerTurn();
		playerIcon.setVisibility(View.INVISIBLE);
		playerIconAnimation.stop();

		if (turn == 1) {
			playerIcon.setBackgroundResource(R.drawable.penguinjumpanimation);
			playerIconAnimation = (AnimationDrawable) playerIcon.getBackground();
			bottomBar.setBackgroundResource(R.color.DarkerBlue);
			topBar.setBackgroundResource(R.color.LighterBlue);
		} else {
			playerIcon.setBackgroundResource(R.drawable.sealmoveanimation);
			playerIconAnimation = (AnimationDrawable) playerIcon.getBackground();
			topBar.setBackgroundResource(R.color.DarkerBlue);
			bottomBar.setBackgroundResource(R.color.LighterBlue);
		}

		for (int i = 0; i < 5; i++) {
			rollSlot[i].setBackgroundResource(R.drawable.white_marker);
		}

		board.resetRollArray();
		counter = 0;
		isReady = false;
		canRoll = true;
		hidePossibleTiles();
		roll.setVisibility(View.VISIBLE);

		for (int i = 0; i < 4; i++){
			playerOneImages[i].setBackgroundResource(R.drawable.seal1);
			playerTwoImages[i].setBackgroundResource(R.drawable.penguin1);
			playerOneCompleted[i] = 0;
			playerTwoCompleted[i] = 0;
		}

		isEndTurn = false;

	}

}
