package com.jeffreychan.yutnori;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.Arrays;
import java.util.TreeSet;

public class BoardActivity extends Activity implements OnClickListener{

	Board board;
	Player[] players;
	Piece currentPiece;

	Button rollButton;
	ImageView sticks, offBoardPiece, p1_currentChar, p2_currentChar, finish;
	AnimationDrawable fallingSticks, rollFlash, offBoardPieceAnimation;

	AnimationDrawable[] tilesAnimation;
	ImageView[] rollSlot, tiles, playerOneChars, playerTwoChars, p1_images, p2_images;

	LinearLayout topBar, bottomBar;
	RelativeLayout rl;

	boolean isRollDone, canRoll = true, isEndTurn, moveDone, capture, isGameOver;
	int rollAmount, turn = 0, counter = 0, width, height, playerOneCurrentPiece = 0, playerTwoCurrentPiece = 0, MAX_TILES = 29;

	boolean[] isMarked;
	int[][] moveSet;

	TreeSet<Integer> specialTiles = new TreeSet<>(Arrays.asList(0, 5, 10, 15, 22));
	ArrayList<Integer> tile_ids = new ArrayList<>();
	ArrayList<Integer> player_ids = new ArrayList<>();

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

		offBoardPiece = (ImageView) findViewById(R.id.playerIcon);
		offBoardPiece.setBackgroundResource(R.drawable.sealmoveanimation);
		offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();
		offBoardPiece.setOnClickListener(this);

		topBar = (LinearLayout) findViewById(R.id.topBar);
		bottomBar = (LinearLayout) findViewById(R.id.bottomBar);

		playerOneChars = new ImageView[4];
		playerTwoChars = new ImageView[4];

		isMarked = new boolean[MAX_TILES];
		tilesAnimation = new AnimationDrawable[MAX_TILES];

		/* BOARD SETUP
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 */
		double padding = 20;
		double space = 40;
		double boardSize = height*0.6;
		if(height*0.6 > width) boardSize = width;
		double tileSize = (boardSize - padding*2 - space*5) / 6;

		tiles = new ImageView[MAX_TILES];
		for(int i = 0; i < tiles.length; i++) {
			tiles[i] = new ImageView(this);
			if(i < 20 && i % 5 == 0 || i == 22) {
				tiles[i].setBackgroundResource(R.drawable.orange_marker);
			} else {
				tiles[i].setBackgroundResource(R.drawable.blue_marker);
			}
			tiles[i].setOnClickListener(this);
			tiles[i].setId(View.generateViewId());
			tile_ids.add(tiles[i].getId());
			tiles[i].setLayoutParams(new RelativeLayout.LayoutParams((int) tileSize, (int) tileSize));
			if(i < 6) {
				tiles[i].setX((float)(width / 2 + boardSize / 2 - tileSize - padding));
				tiles[i].setY((float)(height * 0.4 + boardSize / 2 - i * (tileSize + space) - tileSize - padding));
			}
			else if(i < 10) {
				tiles[i].setX((float) (tiles[5].getX() - (i - 5) * (tileSize + space)));
				tiles[i].setY((tiles[5].getY()));
			}
			else if(i < 16) {
				tiles[i].setX((float)(tiles[9].getX() - tileSize - space));
				tiles[i].setY(tiles[5 - (i - 10)].getY());
			}
			else if(i < 20) {
				tiles[i].setX((tiles[9 - (i - 16)].getX()));
				tiles[i].setY(tiles[15].getY());
			}
			else if(i < 25) {
				tiles[i].setX((float)(width / 2 - tileSize / 2 + (22 - i) * ((tiles[0].getX() - (width / 2 - tileSize / 2))) / 3));
				tiles[i].setY((float) (height * 0.4 - tileSize / 2 - (22 - i) * ((tiles[0].getY() - (height * 0.4 - tileSize / 2))) / 3));
			}
			else {
				int j = i;
				if(i > 26) j ++;
				tiles[i].setX((float)(width / 2 - tileSize / 2 + (j - 27) * ((tiles[0].getX() - (width / 2 - tileSize / 2))) / 3));
				tiles[i].setY((float) (height * 0.4 - tileSize / 2 + (j - 27) * ((tiles[0].getY() - (height * 0.4 - tileSize / 2))) / 3));
			}
			rl.addView(tiles[i]);
		}

		/* END BOARD SETUP
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 */

		rollSlot[0] = (ImageView) findViewById(R.id.rollSlot1);
		rollSlot[1] = (ImageView) findViewById(R.id.rollSlot2);
		rollSlot[2] = (ImageView) findViewById(R.id.rollSlot3);
		rollSlot[3] = (ImageView) findViewById(R.id.rollSlot4);
		rollSlot[4] = (ImageView) findViewById(R.id.rollSlot5);

		sticks = (ImageView) findViewById(R.id.sticks);

		rollButton = (Button) findViewById(R.id.rollButton);
		rollButton.setBackgroundResource(R.drawable.rollflashanimation);
		rollButton.setOnClickListener(this);
		rollFlash = (AnimationDrawable) rollButton.getBackground();
		rollFlash.start();

		finish = (ImageView) findViewById(R.id.finish);
		finish.setOnClickListener(this);

		p1_images = new ImageView[4];
		p2_images = new ImageView[4];
		p1_images[0] = (ImageView) findViewById(R.id.seal1);
		p1_images[1] = (ImageView) findViewById(R.id.seal2);
		p1_images[2] = (ImageView) findViewById(R.id.seal3);
		p1_images[3] = (ImageView) findViewById(R.id.seal4);
		p2_images[0] = (ImageView) findViewById(R.id.penguin1);
		p2_images[1] = (ImageView) findViewById(R.id.penguin2);
		p2_images[2] = (ImageView) findViewById(R.id.penguin3);
		p2_images[3] = (ImageView) findViewById(R.id.penguin4);

		// Set up player characters
		for (int i = 0; i < 4; i++){
			playerOneChars[i] = new ImageView(this);
			playerOneChars[i].setOnClickListener(this);
			playerOneChars[i].setId(View.generateViewId());
			playerOneChars[i].setLayoutParams(new RelativeLayout.LayoutParams((int) tileSize, (int) tileSize));
			playerOneChars[i].setBackgroundResource(R.drawable.seal1);
			playerOneChars[i].setX(-width);
			player_ids.add(playerOneChars[i].getId());
			rl.addView(playerOneChars[i]);

			playerTwoChars[i] = new ImageView(this);
			playerTwoChars[i].setOnClickListener(this);
			playerTwoChars[i].setId(View.generateViewId());
			playerTwoChars[i].setLayoutParams(new RelativeLayout.LayoutParams((int) tileSize, (int) tileSize));
			playerTwoChars[i].setBackgroundResource(R.drawable.penguin1);
			playerTwoChars[i].setX(-width);
			player_ids.add(playerTwoChars[i].getId());
			rl.addView(playerTwoChars[i]);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	return true; }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { return super.onOptionsItemSelected(item);	}

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

		if (isGameOver) return;

		if (v.getId() == R.id.rollButton) {
			handleRoll();
		}
		else if (v.getId() == R.id.finish) {
			players[turn].addScore(currentPiece.getValue());

			for (int i = 0; i < players[turn].getScore(); i++){
				if (turn == 0){
					p1_images[i].setVisibility(View.INVISIBLE);
				} else {
					p2_images[i].setVisibility(View.INVISIBLE);
				}
			}

			if (turn == 0) p1_currentChar.setX(-width);
			else p2_currentChar.setX(-width);

			movePiece(32); // finish location
			hidePossibleTiles();
			moveDone = true;

			if (players[turn].getScore() == 4) endGame();
		}
		else if (v.getId() == R.id.playerIcon && isRollDone) {
			int p = players[turn].findAvailablePiece();
			showPossibleTiles(p);
		}
		else if (tile_ids.contains(v.getId())){    // Activates on tile click
			for (int i = 0; i < MAX_TILES; i++){
				if (v.getId() == tiles[i].getId() && isMarked[i]){
					movePiece(i);
					moveDone = true;
				}
			}
		}
		else if (player_ids.contains(v.getId())){  // Activates on animal click; animal covers tile
			for (int i = 0; i < 4; i++){
				if (v.getId() == playerOneChars[i].getId() && turn == 0 && isRollDone && !isMarked[players[turn].pieces[i].getLocation()]) {
					showPossibleTiles(i);
				}
				else if (v.getId() == playerTwoChars[i].getId() & turn == 1 && isRollDone && !isMarked[players[1].pieces[i].getLocation()]) {
					showPossibleTiles(i);
				}
				// Land on player1 piece
				else if (v.getId() == playerOneChars[i].getId() && isMarked[players[0].pieces[i].getLocation()]){

					if (turn == 0) { // SAME TEAM
						movePiece(players[0].pieces[i].getLocation());
						for (int j = 0; j < 4; j++) {
							if (players[0].pieces[j].getLocation() == currentPiece.getLocation() && currentPiece != players[0].pieces[j]) {
								currentPiece.addValue(players[0].pieces[j].getValue());
								playerOneChars[j].setX(-width);
								players[0].pieces[j].setLocation(-1);
								players[0].pieces[j].setValue(1);
								playerOneChars[j].setBackgroundResource(R.drawable.seal1);
							}
						}

						switch (currentPiece.getValue()) {
							case 2:
								p1_currentChar.setBackgroundResource(R.drawable.seal2);
								break;
							case 3:
								p1_currentChar.setBackgroundResource(R.drawable.seal3);
								break;
							case 4:
								p1_currentChar.setBackgroundResource(R.drawable.seal4);
								break;
							default:
						}

					} else { // OPPOSITE TEAM; send target to graveyard
						movePiece(players[0].pieces[i].getLocation());
						for (int j = 0; j < 4; j++) {
							if (players[0].pieces[j].getLocation() == currentPiece.getLocation()) {
								playerOneChars[j].setX(-width);
								players[0].pieces[j].setLocation(-1);
								playerOneCurrentPiece -= players[0].pieces[j].getValue();
								players[0].pieces[j].setValue(1);
								playerOneChars[j].setBackgroundResource(R.drawable.seal1);
							}
						}
						rollButton.setVisibility(View.VISIBLE);
						capture = true;
					}
					moveDone = true;
				}
				// Land on player2 piece
				else if (v.getId() == playerTwoChars[i].getId() && isMarked[players[1].pieces[i].getLocation()]){

					if (turn == 1) { // SAME TEAM
						movePiece(players[1].pieces[i].getLocation());
						for (int j = 0; j < 4; j++) {
							if (players[1].pieces[j].getLocation() == currentPiece.getLocation() && currentPiece != players[1].pieces[j]) {
								currentPiece.addValue(players[1].pieces[j].getValue());
								playerTwoChars[j].setX(-width);
								players[1].pieces[j].setLocation(-1);
								players[1].pieces[j].setValue(1);
								playerTwoChars[j].setBackgroundResource(R.drawable.penguin1);
							}
						}

						switch (currentPiece.getValue()) {
							case 2:
								p2_currentChar.setBackgroundResource(R.drawable.penguin2);
								break;
							case 3:
								p2_currentChar.setBackgroundResource(R.drawable.penguin3);
								break;
							case 4:
								p2_currentChar.setBackgroundResource(R.drawable.penguin4);
								break;
							default:
						}

					} else { // OPPOSITE TEAM; send target to graveyard
						movePiece(players[1].pieces[i].getLocation());
						for (int j = 0; j < 4; j++){
							if (players[1].pieces[j].getLocation() == currentPiece.getLocation()){
								playerTwoChars[j].setX(-width);
								players[1].pieces[j].setLocation(-1);
								playerTwoCurrentPiece -= players[1].pieces[j].getValue();
								players[1].pieces[j].setValue(1);
								playerTwoChars[j].setBackgroundResource(R.drawable.penguin1);
							}
						}
						rollButton.setVisibility(View.VISIBLE);
						capture = true;
					}

					moveDone = true;
				}
			}
		}

		// Clean-up after move is done
		if (moveDone) {
			moveDone = false;
			int value = 0;
			for (int[] i : moveSet) {
				if (i[0] == currentPiece.getLocation()) {
					value = i[1];
					break;
				}
			}
			removeRoll(value);

			if ((turn == 0 && playerOneCurrentPiece == 4) || (turn == 0 && playerTwoCurrentPiece == 4)){
				offBoardPiece.setVisibility(View.INVISIBLE);
				offBoardPieceAnimation.stop();
			}

			if (!capture) {
				int count = 0;
				for (int i : board.rollArray) {
					if (i != 0) {
						count++;
						break;
					}
				}
				if (count == 0 && isRollDone) endTurn();
			} else {
				offBoardPiece.setVisibility(View.INVISIBLE);
				offBoardPieceAnimation.stop();
				isRollDone = false;
				canRoll = true;
			}

			capture = false;
			hidePossibleTiles();
		}
	}

	private void showPossibleTiles(int pi){

		hidePossibleTiles();

		currentPiece = players[turn].pieces[pi];

		if (turn == 0) p1_currentChar = playerOneChars[pi];
		else p2_currentChar = playerTwoChars[pi];

		moveSet = players[turn].pieces[pi].calculateMoveset(board.rollArray);
		for (int[] move : moveSet) {
			int location = move[0];
			if (location == 32){
				finish.setVisibility(View.VISIBLE);
			}
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
	}

	private void hidePossibleTiles() {

		finish.setVisibility(View.INVISIBLE);

		for (int i = 0; i < MAX_TILES; i++) {
			isMarked[i] = false;

			if (!specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.blue_marker);
			} else if (specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.orange_marker);
			}

		}
	}

	private void movePiece(int i){

		if (turn == 0 && currentPiece.getLocation() == -1) playerOneCurrentPiece++;
		if (turn == 1 && currentPiece.getLocation() == -1) playerTwoCurrentPiece++;

		currentPiece.setLocation(i);

		if (turn == 0 && i != 32) {
			p1_currentChar.setX(tiles[i].getX());
			p1_currentChar.setY(tiles[i].getY());
		} else if (turn == 1 && i != 32) {
			p2_currentChar.setX(tiles[i].getX());
			p2_currentChar.setY(tiles[i].getY());
		}
	}

	private void handleRoll(){
		rollAmount = board.throwSticks();
		rollButton.setVisibility(View.INVISIBLE);

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

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				showRoll(rollAmount);

				if ((rollAmount == 4 || rollAmount == 5) && counter < 4) counter++;
				else if (rollAmount == -1 && counter == 0 && turn == 0 && playerOneCurrentPiece == 0) isEndTurn = true;
				else if (rollAmount == -1 && counter == 0 && turn == 1 && playerTwoCurrentPiece == 0) isEndTurn = true;
				else {
					canRoll = false;
					isRollDone = true;
				}

			}
		}, 990);

		Handler handler2 = new Handler();
		handler2.postDelayed(new Runnable() {
			public void run() {

				hideSticks();

				if (isEndTurn) endTurn();
				else if (canRoll) rollButton.setVisibility(View.VISIBLE);
				else {
					if ((turn == 0 && playerOneCurrentPiece < 4) || (turn == 1 && playerTwoCurrentPiece < 4)) {
						offBoardPiece.setVisibility(View.VISIBLE);
						offBoardPieceAnimation.start();
					}
				}
			}
		}, 2000);
	}

	private void showRoll(int rollAmount){
		board.addRoll(rollAmount);
		updateRollSlots(counter, rollAmount);
		fallingSticks.setVisible(false, false);
	}

	private void hideSticks(){
		sticks.setVisibility(View.INVISIBLE);
		fallingSticks.setVisible(false, false);
	}

	private void endTurn(){
		board.endTurn();
		reset();
	}

	private void reset(){
		turn = board.getPlayerTurn();
		offBoardPiece.setVisibility(View.INVISIBLE);
		offBoardPieceAnimation.stop();

		if (turn == 1) {
			offBoardPiece.setBackgroundResource(R.drawable.penguinjumpanimation);
			offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();

			bottomBar.setBackgroundResource(R.color.DarkerBlue);
			bottomBar.setAlpha(1.0f);
			topBar.setBackgroundResource(R.color.LighterBlue);
			topBar.setAlpha(0.5f);
		} else {
			offBoardPiece.setBackgroundResource(R.drawable.sealmoveanimation);
			offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();

			topBar.setBackgroundResource(R.color.DarkerBlue);
			topBar.setAlpha(1.0f);
			bottomBar.setBackgroundResource(R.color.LighterBlue);
			bottomBar.setAlpha(0.5f);
		}

		for (int i = 0; i < 5; i++) {
			rollSlot[i].setBackgroundResource(R.drawable.white_marker);
		}

		board.resetRollArray();
		hidePossibleTiles();

		counter = 0;
		isRollDone = false;
		canRoll = true;
		isEndTurn = false;
		rollButton.setVisibility(View.VISIBLE);
	}

	private void removeRoll(int i) {
		board.removeRoll(i);
		int count = 0;
		for (int k = 0; k < board.rollArray.length; k++){
			if (board.rollArray[k] != 0) count++;
		}
		counter = count;
		if (counter == 5) counter = 4;

		for(int j = 0; j < 5; ++j) {
			updateRollSlots(j, board.rollArray[j]);
		}
	}

	private void updateRollSlots(int index, int roll){
		switch (roll) {
			case -1:
				rollSlot[index].setBackgroundResource(R.drawable.circleminus1);
				break;
			case 1:
				rollSlot[index].setBackgroundResource(R.drawable.circle1);
				break;
			case 2:
				rollSlot[index].setBackgroundResource(R.drawable.circle2);
				break;
			case 3:
				rollSlot[index].setBackgroundResource(R.drawable.circle3);
				break;
			case 4:
				rollSlot[index].setBackgroundResource(R.drawable.circle4);
				break;
			case 5:
				rollSlot[index].setBackgroundResource(R.drawable.circle5);
				break;
			default:
				rollSlot[index].setBackgroundResource(R.drawable.white_marker);
		}
	}

	private void endGame(){

		isGameOver = true;

		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		TextView tv = new TextView(this);
		tv.setPadding(0, 40, 0, 40);

		if (players[0].getScore() == 4)	tv.setText("Player 1 wins!\nPlay again?");
		else tv.setText("Player 2 wins!\nPlay again?");

		tv.setTextSize(20f);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		adb.setView(tv);
		adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				recreate();
			}
		});
		adb.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		});
		adb.show();
	}
}
