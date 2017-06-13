package com.jeffreychan.yutnori;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class GameActivity extends Activity implements OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	boolean isRollDone;         // Did the stick throwing animation finish
	boolean canRoll = true;     // Did the user roll 4 or 5
	boolean isEndTurn;          // Is it the end of the turn
	boolean capture;            // Did previous move eat enemy piece
	boolean isGameOver;         // Is the game over
	boolean isMoveInProgress;

	String connectedStatus = "";
	String[] playerTips = new String[2];

	int rollAmount;
	int turn = 0;
	int oppTurn = 1;
	int rollSlotIndex = 0;
	int MAX_TILES = 29;
	int mpPos;                  // Current position in the song (Updates when the activity is paused)
	int MOVE_DURATION = 150;    // Length of animation for piece movement
	float heightOffset;

	double moveSize = 0;           // Used for animation movement horizontally or vertically
	double diagonalMoveSize = 0;   // Used for animation movement diagonally

	char[] order;               // Used to determine order of movements for animating movement
	int orderIndex = 0;         // Indicates the current step in animating movement

	Move currentMoveType;

	Board board = new Board();
	Player[] players = new Player[2];
	Piece currentPiece;

	TextView turnText;
	AutoResizeTextView tips;

	TranslateAnimation up;
	TranslateAnimation down;
	TranslateAnimation left;
	TranslateAnimation right;
	TranslateAnimation upRight;
	TranslateAnimation downRight;
	TranslateAnimation upLeft;
	TranslateAnimation downLeft;
	TranslateAnimation finishing;

	AnimationDrawable fallingSticks;
	AnimationDrawable offBoardPieceAnimation;
	AnimationDrawable finishAnimation;

	AnimationDrawable[] tilesAnimation = new AnimationDrawable[MAX_TILES];

	AnimationDrawable[][] playerAnimation = new AnimationDrawable[2][4];

	ImageView offBoardPiece;
	ImageView currentPieceImage;
	ImageView finish;
	ImageView sticks;
	ImageView topBar;
	ImageView bottomBar;

	ImageView[] playerLogo = new ImageView[2];
	ImageView[] rollSlot = new ImageView[5];
	ImageView[] tiles = new ImageView[MAX_TILES];

	ImageView[][] playerOnBoardImages = new ImageView[2][4];
	ImageView[][] playerOffBoardImages = new ImageView[2][4];

	Integer[][] avatarIds = new Integer[2][7];  // Holds the image ids for each player, depending on the avatar chosen

	Button rollButton;
	RelativeLayout rl;

	GoogleApiClient client;

	// Used to access stored information on device
	SharedPreferences prefs;

	boolean soundOn = true;

	MediaPlayer mp;
	final static int MAX_VOLUME = 100;

	boolean[] isMarked = new boolean[MAX_TILES];    // Represents whether a tile is highlighted yellow or not

	Integer[][] moveSet;    // Represents the current possible move set

	TreeSet<Integer> specialTiles = new TreeSet<>(Arrays.asList(0, 5, 10, 15, 22)); // These tiles are colored differently
	ArrayList<Integer> tile_ids = new ArrayList<>();    // Contains the click ids for all tiles
	ArrayList<Integer> player_ids = new ArrayList<>();  // Contains the click ids for all player pieces

	// Request codes for the UIs that we show with startActivityForResult:
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_INVITATION_INBOX = 10001;
	final static int RC_WAITING_ROOM = 10002;

	// Request code used to invoke sign in user interactions.
	final static int RC_SIGN_IN = 9001;

	// Are we currently resolving a connection failure?
	boolean mResolvingConnectionFailure = false;

	// Has the user clicked the sign-in button?
	boolean mSignInClicked = false;

	// Set to true to automatically start the sign in flow when the Activity starts.
	// Set to false to require the user to click the button in order to sign in.
	boolean mAutoStartSignInFlow = true;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(getLayoutId());

		rl = findViewById(R.id.rl);
		rollButton = findViewById(R.id.rollButton);

		loadAvatars();

		// Gets mode selected from TitleScreenActivity
		connectedStatus = getIntent().getExtras().getString("SignedIn");
		mpPos = getIntent().getExtras().getInt("Song");

		// Set up GoogleApiClient
		client = new GoogleApiClient.Builder(this)
					.addApi(Games.API)
					.addScope(Games.SCOPE_GAMES)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();

		// Create media player for background song
		mp = MediaPlayer.create(this, R.raw.song);
		mp.setLooping(true);
		mp.seekTo(mpPos);

		// Formula to modify volume from https://stackoverflow.com/questions/5215459/android-mediaplayer-setvolume-function
		int soundVolume = 75;
		final float volume = (float) (1 - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)));
		mp.setVolume(volume, volume);

		// Grab saved option for sound on/off
		prefs = getSharedPreferences("sounds", Context.MODE_PRIVATE);
		int savedSoundOption = prefs.getInt("background", 1);
		if (savedSoundOption == 0) {
			soundOn = false;
		}
		else {
			mp.start();
			soundOn = true;
		}

		// Get screen size and adjust based on ad size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y - AdSize.SMART_BANNER.getHeightInPixels(this);
		heightOffset = (float) (height/20.0);

		// Set up ad at bottom of screen
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				AdView mAdView = findViewById(R.id.ad_view);
				AdRequest adRequest = new AdRequest.Builder()
						.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
						.build();
				mAdView.loadAd(adRequest);
			}
		});

		players[0] = new Player();
		players[1] = new Player();

		// Prevent multi-touch
		rl.setMotionEventSplittingEnabled(false);

		/* <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 *                                                  BEGIN BOARD SETUP
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 */

		// Set up seal area
		topBar = new ImageView(this);
		topBar.setLayoutParams(new RelativeLayout.LayoutParams(width/2, height/10));
		topBar.setBackgroundResource(R.drawable.bar1);
		rl.addView(topBar);

		// Set up penguin area
		bottomBar = new ImageView(this);
		bottomBar.setLayoutParams(new RelativeLayout.LayoutParams(width/2, height/10));
		bottomBar.setBackgroundResource(R.drawable.bar2);
		bottomBar.setRotation(180f);
		bottomBar.setX(width/2);
		bottomBar.setAlpha(0.25f);
		rl.addView(bottomBar);

		// Set up tiles
		double boardSize = (height*0.6);
		if(height*0.6 > width) boardSize = width;
		double padding = (boardSize/50.0);
		double space = (boardSize/25.0);
		double tileSize = ((boardSize - padding*2 - space*5) / 6);
		moveSize = (tileSize + space);    // Used for animations

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
				tiles[i].setX((float) (tiles[9].getX() - tileSize - space));
				tiles[i].setY(tiles[5 - (i - 10)].getY());
			}
			else if(i < 20) {
				tiles[i].setX((tiles[9 - (i - 16)].getX()));
				tiles[i].setY(tiles[15].getY());
			}
			else if(i < 25) {
				tiles[i].setX((float) (width / 2 - tileSize / 2 + (22 - i) * ((tiles[0].getX() - (width / 2 - tileSize / 2))) / 3));
				tiles[i].setY((float) (height * 0.4 - tileSize / 2 - (22 - i) * ((tiles[0].getY() - (height * 0.4 - tileSize / 2))) / 3));
			}
			else {
				int j = i;
				if(i > 26) j ++;
				tiles[i].setX((float) (width / 2 - tileSize / 2 + (j - 27) * ((tiles[0].getX() - (width / 2 - tileSize / 2))) / 3));
				tiles[i].setY((float) (height * 0.4 - tileSize / 2 + (j - 27) * ((tiles[0].getY() - (height * 0.4 - tileSize / 2))) / 3));
			}
			rl.addView(tiles[i]);
		}

		for (ImageView iv : tiles){
			iv.setY(iv.getY() + heightOffset/2);
		}

		diagonalMoveSize = Math.abs(tiles[21].getY() - tiles[20].getY());

		// Set up lines behind the tiles
		ImageView board_lines = new ImageView(this);
		board_lines.setBackgroundResource(R.drawable.board_lines);
		double dim = (boardSize - 2 * padding - tileSize);   // Width of board lines on screen
		double line_width = (11.0/500) * dim;  // Thickness of line is 11 pixels in a 500x500 image
		dim += line_width;  // Account for left shift of image
		board_lines.setLayoutParams(new RelativeLayout.LayoutParams((int) dim, (int) dim));
		board_lines.setX((float) (tiles[10].getX() + tileSize/2 - line_width/2));
		board_lines.setY((float) (tiles[10].getY() + tileSize/2 - line_width/2));
		rl.addView(board_lines);

		// Tiles go in front of the lines
		for(ImageView iv : tiles) {
			iv.bringToFront();
		}

		// Set up arrows
		ImageView[] arrows = new ImageView[5];
		int arrowSize = (int) (3.0*tileSize/5.0);
		int pad = (int) (tileSize/5.0);
		int[] arr = new int[]{0, 5, 10, 22, 15}; // tiles with arrows
		for (int i = 0; i < 5; i++){
			arrows[i] = new ImageView(this);
			arrows[i].setBackgroundResource(R.drawable.arrow);
			arrows[i].setLayoutParams(new RelativeLayout.LayoutParams(arrowSize, arrowSize));
			arrows[i].setX(tiles[arr[i]].getX() + pad);
			arrows[i].setY(tiles[arr[i]].getY() + pad);

			if (i == 0) arrows[i].setRotation(-90f);
			else if (i == 1) arrows[i].setRotation(135f);
			else if (i == 2 || i == 3) arrows[i].setRotation(45f);

			rl.addView(arrows[i]);
		}

		// Set up start text
		ImageView start = new ImageView(this);
		int startSize = (int) (3.0*tileSize/4.0);
		pad = (int) (tileSize/8.0);
		start.setBackgroundResource(R.drawable.start_tile);
		start.setLayoutParams(new RelativeLayout.LayoutParams(startSize, startSize));
		start.setX(tiles[0].getX() + pad);
		start.setY(tiles[0].getY() + pad);
		rl.addView(start);

		// Set up roll slots
		double rollSize = (int) ((width - padding - 4 * space)/5.0);
		double spaceSize = space;
		if (height/10.0 - padding < rollSize) {
			rollSize = height/10.0 - padding;
			spaceSize = (width - 5 * rollSize - padding)/4.0;
		}
		for (int i = 0; i < 5; i++) {
			rollSlot[i] = new ImageView(this);
			rollSlot[i].setId(View.generateViewId());
			rollSlot[i].setLayoutParams(new RelativeLayout.LayoutParams((int) rollSize, (int) rollSize));
			rollSlot[i].setBackgroundResource(R.drawable.white_marker);
			rollSlot[i].setX((float) (0.5 * padding + i * spaceSize + i * rollSize));
			rollSlot[i].setY((float) (heightOffset*1.5 + (8.5 * height / 10.0 - 0.5 * rollSize)));
			rl.addView(rollSlot[i]);
		}

		//Set up TextView for guiding player
		tips = new AutoResizeTextView(this);
		tips.setId(View.generateViewId());
		tips.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (height / 20.0)));
		tips.setY(heightOffset + (int) (height * 7.7 / 10.0));
		tips.setGravity(Gravity.CENTER);
		tips.setText(R.string.click_me);
		tips.setTextColor(Color.BLACK);
		tips.setVisibility(View.INVISIBLE);
		rl.addView(tips);

		// Set up player bars
		int iconSize = (int) ((width/2 - padding - 2 * space)/4.0);
		for (int i = 0; i < 2; i++) {
			// ----------------------------------------------------------Player Logo
			playerLogo[i] = new ImageView(this);
			playerLogo[i].setId(View.generateViewId());
			playerLogo[i].setLayoutParams(new RelativeLayout.LayoutParams(iconSize, iconSize));

			playerLogo[i].setBackgroundResource(avatarIds[i][6]);

			if (i == 0) playerLogo[i].setX((float) (0.5 * padding));
			else playerLogo[i].setX((float) (width - iconSize - (0.5 * padding)));

			playerLogo[i].setY((float) (0.5 * height / 10.0 - 0.5 * iconSize));

			rl.addView(playerLogo[i]);
		}

		// -----------------------------Player pieces indicating how many unfinished pieces are left
		int miniIconSize = (int) ((width/2 - width/24 - padding - iconSize - space)/4.0);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				if (i == 0){
					playerOffBoardImages[i][j] = new ImageView(this);
					playerOffBoardImages[i][j].setId(View.generateViewId());
					playerOffBoardImages[i][j].setLayoutParams(new RelativeLayout.LayoutParams(miniIconSize, miniIconSize));
					playerOffBoardImages[i][j].setBackgroundResource(avatarIds[i][0]);
					playerOffBoardImages[i][j].setX((float) (0.5*padding + iconSize + space/4 + j * space/8 + j * miniIconSize));
					playerOffBoardImages[i][j].setY((float) (0.5 * height / 10.0 - 0.5 * miniIconSize));
					rl.addView(playerOffBoardImages[i][j]);
				}
				else {
					playerOffBoardImages[i][3-j] = new ImageView(this);
					playerOffBoardImages[i][3-j].setId(View.generateViewId());
					playerOffBoardImages[i][3-j].setLayoutParams(new RelativeLayout.LayoutParams(miniIconSize, miniIconSize));
					playerOffBoardImages[i][3-j].setBackgroundResource(avatarIds[i][0]);
					playerOffBoardImages[i][3-j].setX((float) (width - 0.5*padding - iconSize - space/4 - miniIconSize - j * space/8 - j * miniIconSize));
					playerOffBoardImages[i][3-j].setY((float) (0.5 * height / 10.0 - 0.5 * miniIconSize));
					rl.addView(playerOffBoardImages[i][3-j]);
				}
			}
		}

		// Set up player characters that will move on the board
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				playerOnBoardImages[i][j] = new ImageView(this);
				playerOnBoardImages[i][j].setOnClickListener(this);
				playerOnBoardImages[i][j].setId(View.generateViewId());
				playerOnBoardImages[i][j].setLayoutParams(new RelativeLayout.LayoutParams((int) tileSize, (int) tileSize));
				playerOnBoardImages[i][j].setX(-width);
				player_ids.add(playerOnBoardImages[i][j].getId());
				rl.addView(playerOnBoardImages[i][j]);

				playerOnBoardImages[i][j].setBackgroundResource(avatarIds[i][1]);
				playerAnimation[i][j] = (AnimationDrawable) playerOnBoardImages[i][j].getBackground();
			}
		}

		// Set up character that represents off board pieces
		offBoardPiece = new ImageView(this);
		offBoardPiece.setOnClickListener(this);
		offBoardPiece.setId(View.generateViewId());
		offBoardPiece.setLayoutParams(new RelativeLayout.LayoutParams((int) (width/5.0), (int) (height/10.0)));
		offBoardPiece.setX((float) (width/2.0 - width/10.0));
		offBoardPiece.setY(heightOffset + (float) (6.9*height/10.0));
		offBoardPiece.setBackgroundResource(avatarIds[0][1]);
		offBoardPiece.setVisibility(View.INVISIBLE);
		rl.addView(offBoardPiece);
		offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();
		currentPieceImage = offBoardPiece;

		// Roll button animation
		AnimationDrawable rollFlash = (AnimationDrawable) rollButton.getBackground();
		rollFlash.start();

		// Set up finish image
		finish = new ImageView(this);
		finish.setOnClickListener(this);
		finish.setId(View.generateViewId());
		finish.setLayoutParams(new RelativeLayout.LayoutParams((int) (width/2.5), (int) (height/10.0)));
		finish.setX((float) (width - width/2.5));
		finish.setY(heightOffset + (float) (6.9*height/10.0));
		finish.setBackgroundResource(R.drawable.finishflash);
		finish.setVisibility(View.INVISIBLE);
		rl.addView(finish);

		// Finish button animation
		finishAnimation = (AnimationDrawable) finish.getBackground();
		finishAnimation.start();

		// Set up TextView for indicating player turn
		turnText = new TextView(this);
		turnText.setId(View.generateViewId());
		turnText.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (height * 2 / 10.0)));
		turnText.setY(heightOffset + (int) (height * 2 / 10.0));
		turnText.setGravity(Gravity.CENTER);
		String text = "Player 1's Turn";
		turnText.setText(text);
		turnText.setTextColor(Color.WHITE);
		turnText.setBackgroundColor(Color.parseColor("#56AFC1"));
		turnText.setAlpha(0.8f);    // Set slight transparency so users can see pieces behind it
		turnText.setTextSize(30f);
		rl.addView(turnText);

		// Set up sticks to be rolled
		sticks = new ImageView(this);
		sticks.setId(View.generateViewId());
		sticks.setLayoutParams(new RelativeLayout.LayoutParams(width/2, height/2));
		sticks.setX((float) (width/4.0));
		sticks.setY(heightOffset + (float) (height/4.0));
		sticks.setVisibility(View.INVISIBLE);
		rl.addView(sticks);

		/* <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 *                                                  END BOARD SETUP
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 * <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
		 */

		/*
		 * -----------------------------------------------------------------------------------------------
		 * ----------------------------------------- START ANIMATION SETUP -------------------------------
		 * -----------------------------------------------------------------------------------------------
		 */

		up = new TranslateAnimation(0, 0, 0, (float) -moveSize);
		up.setDuration(MOVE_DURATION);
		up.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setY((float) (currentPieceImage.getY() - moveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		down = new TranslateAnimation(0, 0, 0, (float) moveSize);
		down.setDuration(MOVE_DURATION);
		down.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setY((float) (currentPieceImage.getY() + moveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		left = new TranslateAnimation(0, (float) -moveSize, 0, 0);
		left.setDuration(MOVE_DURATION);
		left.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setX((float) (currentPieceImage.getX() - moveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		right = new TranslateAnimation(0, (float) moveSize, 0, 0);
		right.setDuration(MOVE_DURATION);
		right.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setX((float) (currentPieceImage.getX() + moveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		upRight = new TranslateAnimation(0, (float) diagonalMoveSize, 0, (float) -diagonalMoveSize);
		upRight.setDuration(MOVE_DURATION);
		upRight.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setX((float) (currentPieceImage.getX() + diagonalMoveSize));
				currentPieceImage.setY((float) (currentPieceImage.getY() - diagonalMoveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		downRight = new TranslateAnimation(0, (float) diagonalMoveSize, 0, (float) diagonalMoveSize);
		downRight.setDuration(MOVE_DURATION);
		downRight.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setX((float) (currentPieceImage.getX() + diagonalMoveSize));
				currentPieceImage.setY((float) (currentPieceImage.getY() + diagonalMoveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		upLeft = new TranslateAnimation(0, (float) -diagonalMoveSize, 0, (float) -diagonalMoveSize);
		upLeft.setDuration(MOVE_DURATION);
		upLeft.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setX((float) (currentPieceImage.getX() - diagonalMoveSize));
				currentPieceImage.setY((float) (currentPieceImage.getY() - diagonalMoveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		downLeft = new TranslateAnimation(0, (float) -diagonalMoveSize, 0, (float) diagonalMoveSize);
		downLeft.setDuration(MOVE_DURATION);
		downLeft.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setX((float) (currentPieceImage.getX() - diagonalMoveSize));
				currentPieceImage.setY((float) (currentPieceImage.getY() + diagonalMoveSize));
				if (orderIndex < order.length) startNextAnimation();
				else endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		finishing = new TranslateAnimation(0, (float) -diagonalMoveSize, 0, (float) diagonalMoveSize);
		finishing.setDuration(MOVE_DURATION);
		finishing.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentPieceImage.clearAnimation();
				currentPieceImage.setX((float) (currentPieceImage.getX() - diagonalMoveSize));
				currentPieceImage.setY((float) (currentPieceImage.getY() + diagonalMoveSize));
				currentPieceImage.setVisibility(View.GONE);
				endAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});

		/*
		 * -----------------------------------------------------------------------------------------------
		 * ----------------------------------------- END ANIMATION SETUP ---------------------------------
		 * -----------------------------------------------------------------------------------------------
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	return true; }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { return super.onOptionsItemSelected(item);	}

	public void onConnected(Bundle connectionHint){}

	@Override
	public void onConnectionSuspended(int i) {
		client.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		if (mResolvingConnectionFailure) {
			return;
		}

		if (mSignInClicked || mAutoStartSignInFlow) {
			mAutoStartSignInFlow = false;
			mSignInClicked = false;
			mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, client,
					connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
		}

	}

	/*
	 * If the activity is placed in the background, save the current position of the song
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (mp.isPlaying()) {
			mp.pause();
			mpPos = mp.getCurrentPosition();
		}
	}

	/*
	 * When this activity resumes, return to the saved position in the song
	 */
	@Override
	public void onResume() {
		super.onResume();

		if (soundOn) {
			mp.seekTo(mpPos);
			mp.start();
		}
	}

	/*
	 * When this activity is closed, stop the song
	 */
	@Override
	public void onStop() {
		super.onStop();
		if (mp.isPlaying()) {
			mp.stop();
			mp.release();
		}
	}

	@Override
	public void onStart() {
		if (!client.isConnected()) {
			client.connect();
		}
		super.onStart();
	}

	/**
	 * Once a player has won, prevent any further click events
	 * Otherwise, call the handleClick method if it is not the computer's turn
	 * Also, check that an animation is not in progress
	 *
	 * @param v The view being clicked on
	 */
	@Override
	public void onClick(View v) {
		if (isGameOver) return;

		if (v.getId() == R.id.rollButton) { // Called when roll button is clicked
			handleRoll();
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
	 * Handles logic for tile clicks
	 *
	 * The STACK and CAPTURE moves here only applies to the computer because a user
	 * would not be able to click the tile since having a piece on the tile
	 * would cover the tile and make it un-clickable.
	 *
	 * However, the computer AI has access to the tile
	 *
	 * @param v The tile being clicked on
	 */
	protected void handleTileClick(View v){
		Move m = Move.NORMAL;
		for (int i = 0; i < MAX_TILES; i++) {
			if (v.getId() == tiles[i].getId() && isMarked[i]) {
				for (int j = 0; j < 4; j++){
					if (players[turn].pieces[j].getLocation() == i){
						m = Move.STACK;
						break;
					} else if (players[oppTurn].pieces[j].getLocation() == i){
						m = Move.CAPTURE;
						break;
					}
				}

				movePiece(i, m);
				break;
			}
		}
	}

	/**
	 * Handles the logic for player character clicks. There are three possible situations
	 * when a player character is clicked:
	 *
	 * 1) User wants to see possible move locations
	 * 2) User wants to stack multiple team piece(s)
	 * 3) User wants to capture opponent's piece(s)
	 *
	 * @param v The player character being clicked on
	 */
	protected void handlePlayerClick(View v){
		if (isRollDone) {
			for (int i = 0; i < 4; i++) {

				int myLocation = players[turn].pieces[i].getLocation();
				int oppLocation = players[oppTurn].pieces[i].getLocation();

				// Show possible move locations
				if (v.getId() == playerOnBoardImages[turn][i].getId() && myLocation != -1 && myLocation != 32 && !isMarked[players[turn].pieces[i].getLocation()]) {
					showPossibleTiles(i);
				}
				// Same team
				else if (v.getId() == playerOnBoardImages[turn][i].getId() && myLocation != -1 && myLocation != 32 && isMarked[players[turn].pieces[i].getLocation()]) {
					movePiece(players[turn].pieces[i].getLocation(), Move.STACK);
				}
				// Opponent's pieces
				else if (v.getId() == playerOnBoardImages[oppTurn][i].getId() && oppLocation != -1 && oppLocation != 32 && isMarked[players[oppTurn].pieces[i].getLocation()]) {
					movePiece(players[oppTurn].pieces[i].getLocation(), Move.CAPTURE);
				}
			}
		}
	}

	/**
	 * Highlight possible move locations for the currently selected piece.
	 *
	 * @param i The index of the piece to be moved
	 */
	protected void showPossibleTiles(int i){

		hidePossibleTiles();

		currentPiece = players[turn].pieces[i];
		currentPieceImage = playerOnBoardImages[turn][i];

		moveSet = players[turn].pieces[i].calculateMoveset(board.rollArray);
		for (Integer[] move : moveSet) {
			int location = move[0];

			if (location == 32) finish.setVisibility(View.VISIBLE);
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

		if (turn == 1) tips.setText(R.string.opponent);
		else {
			if (finish.getVisibility() == View.VISIBLE) tips.setText(R.string.click_finish);
			else tips.setText(R.string.click_yellow);
		}
	}

	/**
	 * Moves the currently selected piece to a new location.
	 * Additional actions are required if the move involved stacking your own piece or capturing an enemy piece
	 *
	 * @param dest The location destination to move to
	 * @param m The type of Move: STACK, CAPTURE, NORMAL
	 */
	protected void movePiece(int dest, Move m){

		isMoveInProgress = true;
		hidePossibleTiles();

		offBoardPiece.setVisibility(View.INVISIBLE);
		tips.setVisibility(View.INVISIBLE);

		if (currentPiece.getLocation() == -1) {
			players[turn].addNumPieces(1);
			currentPieceImage.setX(tiles[0].getX());
			currentPieceImage.setY(tiles[0].getY());
		}

		// Find the roll amount
		int numMoves = 0;
		for (int i = 0; i < 5; i++){
			if (moveSet[i][0] == dest){
				numMoves = moveSet[i][1];
				break;
			}
		}
		calculateAnimationOrder(dest, numMoves);
		startNextAnimation();

		currentPiece.setLocation(dest);
		currentMoveType = m;
	}

	/*
	 * Calculates the path the piece needs to take to move to it's destination and saves it to the
	 * char array, order
	 */
	protected void calculateAnimationOrder(int dest, int numMoves){
		order = board.calculatePath(currentPiece.getLocation(), dest, numMoves);
	}

	protected void loadAvatars(){
		String[] s = Shop.Instance.getAnimals(this);
		for (int i = 0; i < 2; i++) {
			avatarIds[i] = Shop.Instance.getImageArray(s[i]);
			playerTips[i] = "Press any moving " + s[i].toLowerCase();
		}
	}

	// To be implemented by subclass
	protected int getLayoutId(){
		return -1;
	}

	/*
	 * Gets next animation in the animation order list
	 *
	 * U = Move up
	 * D = Move down
	 * L = Move left
	 * R = Move right
	 * A = Move up and right
	 * B = Move down and right
	 * C = Move down and left
	 * E = Move up and left
	 * F = Set visibility to gone
	 *
	 * Shown as an image:
	 *
	 * E U A
	 * L   R
	 * C D B
	 */
	protected void startNextAnimation(){
		if (order[orderIndex] == 'U') currentPieceImage.startAnimation(up);
		else if (order[orderIndex] == 'D') currentPieceImage.startAnimation(down);
		else if (order[orderIndex] == 'L') currentPieceImage.startAnimation(left);
		else if (order[orderIndex] == 'R') currentPieceImage.startAnimation(right);
		else if (order[orderIndex] == 'A') currentPieceImage.startAnimation(upRight);
		else if (order[orderIndex] == 'B') currentPieceImage.startAnimation(downRight);
		else if (order[orderIndex] == 'C') currentPieceImage.startAnimation(downLeft);
		else if (order[orderIndex] == 'E') currentPieceImage.startAnimation(upLeft);
		else if (order[orderIndex] == 'F') currentPieceImage.startAnimation(finishing);
		orderIndex++;
	}

	/*
	 * End of animation
	 */
	protected void endAnimation(){
		orderIndex = 0;

		if (currentMoveType == Move.STACK) stack();
		else if (currentMoveType == Move.CAPTURE) capture();

		removeRoll();

		// Check for win
		if (currentPiece.getLocation() == 32){
			players[turn].addScore(currentPiece.getValue());
			currentPieceImage.setX(-currentPieceImage.getX());
			hidePossibleTiles();

			if (players[turn].hasWon()) endGame();
		}

		// Set off board piece visible if not the end of game
		if (!isGameOver && board.numberOfRolls() != 0 && currentMoveType != Move.CAPTURE){
			if (!board.hasOnlyNegativeRoll()) offBoardPiece.setVisibility(View.VISIBLE);
			tips.setVisibility(View.VISIBLE);
		}

		endMove();
		isMoveInProgress = false;
	}

	/**
	 * Land on your own piece.
	 *
	 * Sends all your other pieces at that location off the board but increases the value of the current piece.
	 */
	protected void stack(){
		for (int j = 0; j < 4; j++) {
			if (players[turn].pieces[j].getLocation() == currentPiece.getLocation() && currentPiece != players[turn].pieces[j]) {
				currentPiece.addValue(players[turn].pieces[j].getValue());

				playerOnBoardImages[turn][j].setX(-currentPieceImage.getWidth());
				players[turn].pieces[j].setLocation(-1);
				players[turn].pieces[j].resetValue();

				playerOnBoardImages[turn][j].setBackgroundResource(avatarIds[turn][1]);

			}
		}

		currentPieceImage.setBackgroundResource(avatarIds[turn][currentPiece.getValue()]);

		for (int j = 0; j < 4; j++){
			playerAnimation[turn][j].stop();
			playerAnimation[turn][j] = (AnimationDrawable) playerOnBoardImages[turn][j].getBackground();
			playerAnimation[turn][j].start();
		}
	}

	/**
	 * Land on an opponent's piece.
	 *
	 * Send all their pieces off the board and roll again.
	 */
	protected void capture(){
		for (int j = 0; j < 4; j++) {
			if (players[oppTurn].pieces[j].getLocation() == currentPiece.getLocation()) {
				playerOnBoardImages[oppTurn][j].setX(-currentPieceImage.getWidth());
				players[oppTurn].pieces[j].setLocation(-1);
				players[oppTurn].subtractNumPieces(players[oppTurn].pieces[j].getValue());
				players[oppTurn].pieces[j].resetValue();

				playerOnBoardImages[oppTurn][j].setBackgroundResource(avatarIds[oppTurn][1]);
			}
		}
		if (turn == 0) rollButton.setVisibility(View.VISIBLE);
		capture = true;
	}

	/**
	 * Handles the determination of the amount rolled when the roll button is clicked
	 * Decides what should happen next based on roll.
	 * Ex: Rolling 4 or 5 allows the user to roll again. Rolling -1 with no pieces on the board ends the turn.
	 *
	 * Once the rolling phase is completed, prompt the user to make a move with an appropriate message
	 */
	protected void handleRoll(){
		rollAmount = board.throwSticks();
		rollButton.setVisibility(View.INVISIBLE);
		turnText.setVisibility(View.INVISIBLE);

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
				updateRollArray(rollAmount);

				if ((rollAmount == 4 || rollAmount == 5) && rollSlotIndex < 4) {
					rollSlotIndex++;
					String text;
					if (turn == 1) text = "Opponent Roll Again!";
					else text = "Roll Again!";

					turnText.setText(text);
					turnText.setVisibility(View.VISIBLE);
				}
				else if (rollAmount == -1 && rollSlotIndex == 0 && players[turn].hasNoPiecesOnBoard()) isEndTurn = true;
				else {
					canRoll = false;
				}
			}
		}, 990);

		// Hide the sticks 1 second after the roll is shown
		Handler handler2 = new Handler();
		handler2.postDelayed(new Runnable() {
			public void run() {

				hideSticks();

				if (isEndTurn) endTurn();
				else if (canRoll) {
					if (turn == 0) rollButton.setVisibility(View.VISIBLE);
					else rollButton.setVisibility(View.INVISIBLE);
				}
				else {
					isRollDone = true;

					int posCount = 0;
					for (int i : board.rollArray) {
						if (i != 0 && i != -1) {
							posCount++;
							break;
						}
					}

					tips.setVisibility(View.VISIBLE);

					if (players[turn].getNumPieces() < 4 && posCount > 0) {
						offBoardPiece.setVisibility(View.VISIBLE);
						offBoardPieceAnimation.start();

						if (players[turn].hasNoPiecesOnBoard()) tips.setText(R.string.click_me);
					} else if (players[turn].hasAllPiecesOnBoard()){
						tips.setText(playerTips[turn]);
					}

					for (int j = 0; j < 4; j++){
						playerAnimation[turn][j].stop();
						playerAnimation[turn][j] = (AnimationDrawable) playerOnBoardImages[turn][j].getBackground();
						playerAnimation[turn][j].start();
					}

					if (turn == 1) {
						tips.setText(R.string.opponent);
					}
				}
			}
		}, 1990);
	}

	/**
	 * Update the board's rollArray and call function to update the roll slot images
	 *
	 * @param rollAmount The roll to be added
	 */
	protected void updateRollArray(int rollAmount){
		board.addRoll(rollAmount);
		updateRollSlots(rollSlotIndex, rollAmount);
		fallingSticks.setVisible(false, false);
	}

	/**
	 * Hide the sticks once the rolling is complete
	 */
	protected void hideSticks(){
		sticks.setVisibility(View.INVISIBLE);
		fallingSticks.setVisible(false, false);
	}

	/**
	 * Stop all tiles from flashing yellow and prompt user to select a piece
	 */
	protected void hidePossibleTiles() {

		finish.setVisibility(View.INVISIBLE);

		if (players[0].hasNoPiecesOnBoard() && turn == 0) tips.setText(R.string.click_me);
		else if (turn == 0) tips.setText(playerTips[0]);

		for (int i = 0; i < MAX_TILES; i++) {
			isMarked[i] = false;

			if (!specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.blue_marker);
			} else if (specialTiles.contains(i)) {
				tiles[i].setBackgroundResource(R.drawable.orange_marker);
			}
		}
	}

	/**
	 * Removes the first occurrence of a roll from the roll slots
	 */
	protected void removeRoll() {

		// Find the roll that was used by the current piece
		int value = 0;
		for (Integer[] m : moveSet) {
			if (m[0] == currentPiece.getLocation()) {
				value = m[1];
				break;
			}
		}

		// Remove the roll from the board array and update the roll slot images
		board.removeRoll(value);
		int count = 0;
		for (int k = 0; k < board.rollArray.length; k++){
			if (board.rollArray[k] != 0) count++;
		}
		rollSlotIndex = count;

		if (rollSlotIndex == 5) rollSlotIndex = 4;

		for(int j = 0; j < 5; ++j) {
			updateRollSlots(j, board.rollArray[j]);
		}
	}

	/**
	 * Show completed pieces as images with a gold medal
	 * Hide pieces that are on the board
	 * Otherwise, show images as a plain seal or penguin
	 */
	protected void updateOffBoardImages(){
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < players[i].getScore(); j++) {
				if (i == 0) {
					playerOffBoardImages[i][j].setVisibility(View.VISIBLE);
					playerOffBoardImages[i][j].setBackgroundResource(avatarIds[i][5]);
				}
				else {
					playerOffBoardImages[i][3-j].setVisibility(View.VISIBLE);
					playerOffBoardImages[i][3-j].setBackgroundResource(avatarIds[i][5]);
				}
			}
			for (int j = players[i].getScore(); j < players[i].getNumPieces(); j++) {
				if (i == 0) playerOffBoardImages[i][j].setVisibility(View.INVISIBLE);
				else playerOffBoardImages[i][3-j].setVisibility(View.INVISIBLE);
			}
			for (int j = players[i].getNumPieces(); j < 4; j++) {
				if (i == 0) playerOffBoardImages[i][j].setVisibility(View.VISIBLE);
				else playerOffBoardImages[i][3-j].setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Update the images in the roll slots near the bottom
	 *
	 * @param index The slot to update
	 * @param roll The roll amount
	 */
	protected void updateRollSlots(int index, int roll){
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

	/**
	 * Prepares the board for the next player's turn
	 */
	protected void prepareForNextTurn(){
		turn = board.getPlayerTurn();
		oppTurn = (turn + 1) % 2;

		offBoardPiece.setVisibility(View.INVISIBLE);
		offBoardPieceAnimation.stop();
		offBoardPieceAnimation.selectDrawable(0);

		tips.setVisibility(View.INVISIBLE);
		tips.setText(playerTips[turn]);
		if (turn == 1) {
			offBoardPiece.setBackgroundResource(avatarIds[turn][1]);

			bottomBar.setBackgroundResource(R.drawable.bar1);
			bottomBar.setAlpha(1.0f);
			topBar.setBackgroundResource(R.drawable.bar2);
			topBar.setAlpha(0.25f);
		} else {
			offBoardPiece.setBackgroundResource(avatarIds[turn][1]);

			topBar.setBackgroundResource(R.drawable.bar1);
			topBar.setAlpha(1.0f);
			bottomBar.setBackgroundResource(R.drawable.bar2);
			bottomBar.setAlpha(0.25f);
		}

		offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();

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

		rollSlotIndex = 0;
		isRollDone = false;
		canRoll = true;
		isEndTurn = false;
		if (turn == 0) rollButton.setVisibility(View.VISIBLE);

		String text;
		if (turn == 1) text = "Opponent's Turn";
		else text = "Your Turn";

		turnText.setText(text);
		turnText.setVisibility(View.VISIBLE);
	}

	/**
	 * End the current player's turn
	 */
	protected void endTurn(){
		board.endTurn();
		prepareForNextTurn();
	}

	/**
	 * If the user made a move (STACK, CAPTURE, or NORMAL),
	 * prepare the board for another move or end the turn
	 */
	protected void endMove(){}

	/**
	 * Displays an AlertDialog with the winner and asks if the user wants to play again.
	 * Prevents buttons and text from appearing.
	 */
	protected void endGame(){}

}
