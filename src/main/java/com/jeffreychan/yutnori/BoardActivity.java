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
	Piece currentPiece;
	ImageView[] playerOneImages, playerTwoImages, moveButtons, rollSlot, tiles, playerOneChars, playerTwoChars;
	ImageView sticks, move1, move2, move3, move4, move5, moveMinus1, playerIcon, p1current, p2current;
	AnimationDrawable fallingSticks, rollFlash, playerIconAnimation;
	AnimationDrawable[] tilesAnimation, playerOneCharsAnimation, playerTwoCharsAnimation;
	Button roll;
	int rollAmount, turn = 0, counter = 0, width, height, playerOneCurrentPiece = 0, playerTwoCurrentPiece = 0, MAX_TILES = 29;
	TextView rollText;
	LinearLayout topBar, bottomBar;
	int[] playerOnePieceSelected, playerTwoPieceSelected;
	boolean[] isPlayerOnePieceSelectable, isPlayerTwoPieceSelectable, isMarked;
	boolean isReady, canRoll = true, isEndTurn, moveDone, isPlayerIconSelected, capture;
	TreeSet<Integer> specialTiles = new TreeSet<>();
	RelativeLayout rl;
	ArrayList<Integer> tile_ids = new ArrayList<>();
	ArrayList<Integer> player_ids = new ArrayList<>();
	int[][] moveSet = new int[1][2];

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
		isPlayerOnePieceSelectable = new boolean[4];
		isPlayerTwoPieceSelectable = new boolean[4];
		for (int i = 0; i < 4; i++){
			isPlayerOnePieceSelectable[i] = true;
			isPlayerTwoPieceSelectable[i] = true;
		}

		specialTiles.add(0);
		specialTiles.add(5);
		specialTiles.add(10);
		specialTiles.add(15);
		specialTiles.add(22);

		playerIcon = (ImageView) findViewById(R.id.playerIcon);
		playerIcon.setBackgroundResource(R.drawable.sealmoveanimation);
		playerIconAnimation = (AnimationDrawable) playerIcon.getBackground();
		playerIcon.setOnClickListener(this);

		topBar = (LinearLayout) findViewById(R.id.topBar);
		bottomBar = (LinearLayout) findViewById(R.id.bottomBar);

		playerOneImages = new ImageView[4];
		playerTwoImages = new ImageView[4];
		playerOneChars = new ImageView[4];
		playerTwoChars = new ImageView[4];
		playerOneCharsAnimation = new AnimationDrawable[4];
		playerTwoCharsAnimation = new AnimationDrawable[4];

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
//		float offset = height / 10; // Seal line

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
		sticks.bringToFront();
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

			playerOneChars[i] = new ImageView(this);
			playerOneChars[i].setOnClickListener(this);
			playerOneChars[i].setId(View.generateViewId());
			playerOneChars[i].setLayoutParams(new RelativeLayout.LayoutParams((int) tileSize, (int) tileSize));
			playerOneChars[i].setBackgroundResource(R.drawable.sealmoveanimation);
			playerOneChars[i].setX(-width);
			playerOneCharsAnimation[i] = (AnimationDrawable) playerOneChars[i].getBackground();
			player_ids.add(playerOneChars[i].getId());
			rl.addView(playerOneChars[i]);

			playerTwoChars[i] = new ImageView(this);
			playerTwoChars[i].setOnClickListener(this);
			playerTwoChars[i].setId(View.generateViewId());
			playerTwoChars[i].setLayoutParams(new RelativeLayout.LayoutParams((int) tileSize, (int) tileSize));
			playerTwoChars[i].setBackgroundResource(R.drawable.penguinjumpanimation);
			playerTwoChars[i].setX(-width);
			playerTwoCharsAnimation[i] = (AnimationDrawable) playerTwoChars[i].getBackground();
			player_ids.add(playerTwoChars[i].getId());
			rl.addView(playerTwoChars[i]);

		}

		moveButtons = new ImageView[5];
		for (int i = 0; i < 4; i++){
			moveButtons[i] = new ImageView(this);
			moveButtons[i].setId(i + 10);
			moveButtons[i].setOnClickListener(this);

		}

		playerOnePieceSelected = new int[4];
		playerTwoPieceSelected = new int[4];

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

	private void showPossibleTiles(int pl, int pi){

		currentPiece = players[pl].pieces[pi];
		if (pl == 0){
			p1current = playerOneChars[pi];
		} else {
			p2current = playerTwoChars[pi];
		}

		moveSet = players[pl].pieces[pi].calculateMoveset(board.rollArray);
		for (int[] move : moveSet) {
			if (move[0] != -1) {


				if (specialTiles.contains(move[0])){
					tiles[move[0]].setBackgroundResource(R.drawable.orangemarkerflash);
				} else {
					tiles[move[0]].setBackgroundResource(R.drawable.bluemarkerflash);
				}
				tilesAnimation[move[0]] = (AnimationDrawable) tiles[move[0]].getBackground();
				tilesAnimation[move[0]].start();
				isMarked[move[0]] = true;
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

		isPlayerIconSelected = false;

		for (int i = 0; i < 4; i++){
			isPlayerOnePieceSelectable[i] = true;
			isPlayerTwoPieceSelectable[i] = true;
		}
	}

	private void highlightPlayerImages(int pl, int piece){

		hidePossibleTiles();
		playerIconAnimation.stop();

		if (pl == 0){
			playerIcon.setBackgroundResource(R.drawable.seal_highlighted);
		} else {
			playerIcon.setBackgroundResource(R.drawable.penguin_highlighted);
		}

		showPossibleTiles(pl, piece);

	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.playerIcon && turn == 0 && isReady) {
			int p = findAvailablePiece(0);
			if (p != -1) {
				highlightPlayerImages(0, p);
			}
		}
		else if (v.getId() == R.id.playerIcon && turn == 1 && isReady) {
			int p = findAvailablePiece(1);
			if (p != -1) {
				highlightPlayerImages(1, p);
			}
		}
		else if (tile_ids.contains(v.getId())){    // Activates on tile click
			for (int i = 0; i < MAX_TILES; i++){
				if (v.getId() == tiles[i].getId() && isMarked[i]){

					movePiece(i);
					moveDone = true;
					// TODO
				}
			}
		}
		else if (player_ids.contains(v.getId())){  // Activates on animal click; animal covers tile
			for (int i = 0; i < 4; i++){
				if (v.getId() == playerOneChars[i].getId() && turn == 0 && isReady && !isMarked[players[0].pieces[i].getLocation()]) {
					highlightPlayerImages(0, i);
				}
				else if (v.getId() == playerTwoChars[i].getId() & turn == 1 && isReady && !isMarked[players[1].pieces[i].getLocation()]) {
					highlightPlayerImages(1, i);
				}
				// Land on player1 piece
				else if (v.getId() == playerOneChars[i].getId() && isMarked[players[0].pieces[i].getLocation()]){

					if (turn == 0) { // SAME TEAM
						movePiece(players[0].pieces[i].getLocation());
					} else { // OPPOSITE TEAM; send target to graveyard
						movePiece(players[0].pieces[i].getLocation());
						for (int j = 0; j < 4; j++) {
							if (players[0].pieces[j].getLocation() == currentPiece.getLocation()) {
								playerOneChars[j].setX(-width);
								players[0].pieces[j].setLocation(-1);
								playerOneCurrentPiece--;
							}
						}
						roll.setVisibility(View.VISIBLE);
						capture = true;
					}
					moveDone = true;
				}
				// Land on player2 piece
				else if (v.getId() == playerTwoChars[i].getId() && isMarked[players[1].pieces[i].getLocation()]){

					if (turn == 1) {
						movePiece(players[1].pieces[i].getLocation());
					} else {
						movePiece(players[1].pieces[i].getLocation());
						for (int j = 0; j < 4; j++){
							if (players[1].pieces[j].getLocation() == currentPiece.getLocation()){
								playerTwoChars[j].setX(-width);
								players[1].pieces[j].setLocation(-1);
								playerTwoCurrentPiece--;
							}
						}
						roll.setVisibility(View.VISIBLE);
						capture = true;
					}

					moveDone = true;
				}
			}
		}
		else if (v.getId() == R.id.rollButton) {
			handleRoll();
		}

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

			if (!capture) {
				int counter = 0;
				for (int i : board.rollArray) {
					if (i != 0) {
						counter++;
						break;
					}
				}
				if (counter == 0 && isReady) endTurn();
			}

			capture = false;
			hidePossibleTiles();
		}
	}

	private int findAvailablePiece(int pl){
		for (int i = 0; i < 4; i++){
			if (players[pl].pieces[i].getLocation() == -1){
				return i;
			}
		}
		return -1;
	}

	private void movePiece(int i){

		if (turn == 0 && currentPiece.getLocation() == -1) playerOneCurrentPiece++;
		if (turn == 1 && currentPiece.getLocation() == -1) playerTwoCurrentPiece++;

		currentPiece.setLocation(i);

		if (turn == 0) {
			p1current.setX(tiles[i].getX());
			p1current.setY(tiles[i].getY());
		} else {
			p2current.setX(tiles[i].getX());
			p2current.setY(tiles[i].getY());
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
				else if (rollAmount == -1 && counter == 0 && turn == 0 && playerOneCurrentPiece == 0) isEndTurn = true;
				else if (rollAmount == -1 && counter == 0 && turn == 1 && playerTwoCurrentPiece == 0) isEndTurn = true;
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
					if ((turn == 0 && playerOneCurrentPiece < 4) || (turn == 1 && playerTwoCurrentPiece < 4)) {
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
			bottomBar.setAlpha(1.0f);
			topBar.setBackgroundResource(R.color.LighterBlue);
			topBar.setAlpha(0.5f);
		} else {
			playerIcon.setBackgroundResource(R.drawable.sealmoveanimation);
			playerIconAnimation = (AnimationDrawable) playerIcon.getBackground();

			topBar.setBackgroundResource(R.color.DarkerBlue);
			topBar.setAlpha(1.0f);
			bottomBar.setBackgroundResource(R.color.LighterBlue);
			bottomBar.setAlpha(0.5f);
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
		isPlayerIconSelected = false;

		for (int i = 0; i < 4; i++){
			playerOneImages[i].setBackgroundResource(R.drawable.seal1);
			playerTwoImages[i].setBackgroundResource(R.drawable.penguin1);
			playerOnePieceSelected[i] = 0;
			playerTwoPieceSelected[i] = 0;
			isPlayerOnePieceSelectable[i] = true;
			isPlayerTwoPieceSelectable[i] = true;
		}

		isEndTurn = false;

	}

	private void removeRoll(int i) {
		board.removeRoll(i);
		counter = board.rollArray.length;
		for(int j = 0; j < 5; ++j) {
			switch (board.rollArray[j]) {
				case -1:
					rollSlot[j].setBackgroundResource(R.drawable.circleminus1);
					break;
				case 1:
					rollSlot[j].setBackgroundResource(R.drawable.circle1);
					break;
				case 2:
					rollSlot[j].setBackgroundResource(R.drawable.circle2);
					break;
				case 3:
					rollSlot[j].setBackgroundResource(R.drawable.circle3);
					break;
				case 4:
					rollSlot[j].setBackgroundResource(R.drawable.circle4);
					break;
				case 5:
					rollSlot[j].setBackgroundResource(R.drawable.circle5);
					break;
				default:
			}
		}
	}

}
