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
	ImageView sticks, offBoardPiece, currentPieceImage, finish, board_lines;
	AnimationDrawable fallingSticks, rollFlash, offBoardPieceAnimation;
	AnimationDrawable[] tilesAnimation;
	AnimationDrawable[][] playerAnimation;
	ImageView[] rollSlot, tiles;
	ImageView[][] playerOnBoardImages, playerOffBoardImages;
	LinearLayout topBar, bottomBar;
	RelativeLayout rl;

	boolean isRollDone, canRoll = true, isEndTurn, moveDone, capture, isGameOver;
	int rollAmount, turn = 0, oppTurn = 1, counter = 0, width, height, MAX_TILES = 29;
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
		rl.setOnClickListener(this);

		players = new Player[2];
		players[0] = new Player();
		players[1] = new Player();

		rollSlot = new ImageView[5];
		board = new Board();

		offBoardPiece = (ImageView) findViewById(R.id.playerIcon);
		offBoardPiece.setBackgroundResource(R.drawable.sealmoveanimationclick);
		offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();
		offBoardPiece.setOnClickListener(this);

		currentPieceImage = offBoardPiece;

		topBar = (LinearLayout) findViewById(R.id.topBar);
		bottomBar = (LinearLayout) findViewById(R.id.bottomBar);

		playerOnBoardImages = new ImageView[2][4];

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

		// Set up lines behind the tiles
		board_lines = new ImageView(this);
		board_lines.setBackgroundResource(R.drawable.board_lines);
		int dim = (int) (boardSize - 2 * padding - tileSize);   // Width of board lines on screen
		float line_width = (float) (11.0/500) * dim;  // Thickness of line is 11 pixels in a 500x500 image
		dim += line_width;  // Account for left shift of image
		board_lines.setLayoutParams(new RelativeLayout.LayoutParams(dim, dim));
		board_lines.setX((float) (tiles[10].getX() + tileSize/2 - line_width/2));
		board_lines.setY((float) (tiles[10].getY() + tileSize/2 - line_width/2));
		rl.addView(board_lines);

		for(ImageView iv : tiles) {
			iv.bringToFront();
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

		playerOffBoardImages = new ImageView[2][4];
		playerOffBoardImages[0][0] = (ImageView) findViewById(R.id.seal1);
		playerOffBoardImages[0][1] = (ImageView) findViewById(R.id.seal2);
		playerOffBoardImages[0][2] = (ImageView) findViewById(R.id.seal3);
		playerOffBoardImages[0][3] = (ImageView) findViewById(R.id.seal4);
		playerOffBoardImages[1][0] = (ImageView) findViewById(R.id.penguin1);
		playerOffBoardImages[1][1] = (ImageView) findViewById(R.id.penguin2);
		playerOffBoardImages[1][2] = (ImageView) findViewById(R.id.penguin3);
		playerOffBoardImages[1][3] = (ImageView) findViewById(R.id.penguin4);

		playerAnimation = new AnimationDrawable[2][4];
		// Set up player characters
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				playerOnBoardImages[i][j] = new ImageView(this);
				playerOnBoardImages[i][j].setOnClickListener(this);
				playerOnBoardImages[i][j].setId(View.generateViewId());
				playerOnBoardImages[i][j].setLayoutParams(new RelativeLayout.LayoutParams((int) tileSize, (int) tileSize));
				playerOnBoardImages[i][j].setX(-width);
				player_ids.add(playerOnBoardImages[i][j].getId());
				rl.addView(playerOnBoardImages[i][j]);

				if (i == 0) playerOnBoardImages[i][j].setBackgroundResource(R.drawable.sealmoveanimation);
				else playerOnBoardImages[i][j].setBackgroundResource(R.drawable.penguinjumpanimation);

				playerAnimation[i][j] = (AnimationDrawable) playerOnBoardImages[i][j].getBackground();
			}
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
			currentPieceImage.setX(-width);
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
				if (v.getId() == playerOnBoardImages[turn][i].getId() && isRollDone && !isMarked[players[turn].pieces[i].getLocation()]) {
					showPossibleTiles(i);
				}
				// SAME TEAM
				else if (v.getId() == playerOnBoardImages[turn][i].getId() && isMarked[players[turn].pieces[i].getLocation()]){
					movePiece(players[turn].pieces[i].getLocation());

					for (int j = 0; j < 4; j++) {
						if (players[turn].pieces[j].getLocation() == currentPiece.getLocation() && currentPiece != players[turn].pieces[j]) {
							currentPiece.addValue(players[turn].pieces[j].getValue());
							playerOnBoardImages[turn][j].setX(-width);
							players[turn].pieces[j].setLocation(-1);
							players[turn].pieces[j].setValue(1);

							if (turn == 0) playerOnBoardImages[turn][j].setBackgroundResource(R.drawable.sealmoveanimation);
							else playerOnBoardImages[turn][j].setBackgroundResource(R.drawable.penguinjumpanimation);

							playerAnimation[turn][j] = (AnimationDrawable) playerOnBoardImages[turn][j].getBackground();
						}
					}

					switch (currentPiece.getValue()) {
						case 2:
							if (turn == 0) currentPieceImage.setBackgroundResource(R.drawable.sealmoveanimation2);
							else currentPieceImage.setBackgroundResource(R.drawable.penguinjumpanimation2);
							break;
						case 3:
							if (turn == 0) currentPieceImage.setBackgroundResource(R.drawable.sealmoveanimation3);
							else currentPieceImage.setBackgroundResource(R.drawable.penguinjumpanimation3);
							break;
						case 4:
							if (turn == 0) currentPieceImage.setBackgroundResource(R.drawable.sealmoveanimation4);
							else currentPieceImage.setBackgroundResource(R.drawable.penguinjumpanimation4);
							break;
						default:
					}

					for (int j = 0; j < 4; j++){
						if (currentPieceImage == playerOnBoardImages[turn][j]){
							playerAnimation[turn][j] = (AnimationDrawable) playerOnBoardImages[turn][j].getBackground();
						}
					}

					moveDone = true;
				}
				// OPPOSITE TEAM
				else if (v.getId() == playerOnBoardImages[oppTurn][i].getId() && isMarked[players[oppTurn].pieces[i].getLocation()]) {
					movePiece(players[oppTurn].pieces[i].getLocation());
					for (int j = 0; j < 4; j++) {
						if (players[oppTurn].pieces[j].getLocation() == currentPiece.getLocation()) {
							playerOnBoardImages[oppTurn][j].setX(-width);
							players[oppTurn].pieces[j].setLocation(-1);
							players[oppTurn].numPieces -= players[oppTurn].pieces[j].getValue();
							players[oppTurn].pieces[j].setValue(1);

							if (turn == 0) playerOnBoardImages[oppTurn][j].setBackgroundResource(R.drawable.penguinjumpanimation);
							else playerOnBoardImages[oppTurn][j].setBackgroundResource(R.drawable.sealmoveanimation);

							playerAnimation[turn][j] = (AnimationDrawable) playerOnBoardImages[turn][j].getBackground();
						}
					}
					rollButton.setVisibility(View.VISIBLE);
					capture = true;
					moveDone = true;
				}
			}
		}
		else {
			hidePossibleTiles();
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

			if (players[turn].numPieces == 4){
				offBoardPiece.setVisibility(View.INVISIBLE);
				offBoardPieceAnimation.stop();
				offBoardPieceAnimation.selectDrawable(0);
			}

			if (!capture) {
				int count = 0;
				int posCount = 0;
				for (int i : board.rollArray) {
					if (i != 0) {
						count++;
						break;
					}
				}
				for (int i : board.rollArray) {
					if (i != 0 && i != -1) {
						posCount++;
						break;
					}
				}
				if (count == 0 && isRollDone) endTurn();
				if (posCount == 0 && isRollDone) {
					offBoardPiece.setVisibility(View.INVISIBLE);
					offBoardPieceAnimation.stop();
					offBoardPieceAnimation.selectDrawable(0);
				}
			} else {
				offBoardPiece.setVisibility(View.INVISIBLE);
				offBoardPieceAnimation.stop();
				offBoardPieceAnimation.selectDrawable(0);
				isRollDone = false;
				canRoll = true;
			}

			capture = false;
			hidePossibleTiles();

			// Hide pieces that are on the board or completed
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 4; j++) {
					if (j < players[i].numPieces) playerOffBoardImages[i][j].setVisibility(View.INVISIBLE);
					else playerOffBoardImages[i][j].setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private void showPossibleTiles(int pi){

		hidePossibleTiles();

		currentPiece = players[turn].pieces[pi];
		currentPieceImage = playerOnBoardImages[turn][pi];

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

		if (currentPiece.getLocation() == -1) players[turn].numPieces++;

		currentPiece.setLocation(i);

		// Move image to tile i
		if (i != 32) {
			currentPieceImage.setX(tiles[i].getX());
			currentPieceImage.setY(tiles[i].getY());
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

		// Wait until roll finishes before displaying roll value
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				showRoll(rollAmount);

				if ((rollAmount == 4 || rollAmount == 5) && counter < 4) counter++;
				else if (rollAmount == -1 && counter == 0 && players[turn].numPieces == players[0].getScore()) isEndTurn = true;
				else {
					canRoll = false;
					isRollDone = true;
				}

			}
		}, 990);

		// Hide the sticks 1 second after the roll is shown
		Handler handler2 = new Handler();
		handler2.postDelayed(new Runnable() {
			public void run() {

				hideSticks();

				if (isEndTurn) endTurn();
				else if (canRoll) rollButton.setVisibility(View.VISIBLE);
				else {
					int posCount = 0;
					for (int i : board.rollArray) {
						if (i != 0 && i != -1) {
							posCount++;
							break;
						}
					}

					if (players[turn].numPieces < 4 && posCount > 0) {
						offBoardPiece.setVisibility(View.VISIBLE);
						offBoardPieceAnimation.start();
					}

					for (int j = 0; j < 4; j++){
						playerAnimation[turn][j].start();
					}
				}
			}
		}, 1990);
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
		oppTurn = (turn + 1) % 2;

		offBoardPiece.setVisibility(View.INVISIBLE);
		offBoardPieceAnimation.stop();
		offBoardPieceAnimation.selectDrawable(0);

		if (turn == 1) {
			offBoardPiece.setBackgroundResource(R.drawable.penguinjumpanimationclick);
			offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();

			bottomBar.setBackgroundResource(R.color.DarkerBlue);
			bottomBar.setAlpha(1.0f);
			topBar.setBackgroundResource(R.color.LighterBlue);
			topBar.setAlpha(0.5f);
		} else {
			offBoardPiece.setBackgroundResource(R.drawable.sealmoveanimationclick);
			offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();

			topBar.setBackgroundResource(R.color.DarkerBlue);
			topBar.setAlpha(1.0f);
			bottomBar.setBackgroundResource(R.color.LighterBlue);
			bottomBar.setAlpha(0.5f);
		}

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
		rollButton.setVisibility(View.INVISIBLE);

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
