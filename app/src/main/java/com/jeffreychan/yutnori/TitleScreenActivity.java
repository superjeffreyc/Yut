package com.jeffreychan.yutnori;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;
import java.util.Calendar;

import me.grantland.widget.AutofitTextView;

public class TitleScreenActivity extends Activity implements OnClickListener, OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private static int RC_SIGN_IN = 9001;

	private boolean mResolvingConnectionFailure = false;
	private boolean mAutoStartSignInFlow = true;
	private boolean mSignInClicked = false;

	Button startButton;
	Button helpButton;
	Button settingsButton;
	Button quitButton;
	Button onePlayerButton;
	Button twoPlayerButton;
	Button shopButton;
	Button backButton;
	Button switchButton;

	Spinner p1spin;
	Spinner p2spin;

	RelativeLayout rl;
	RelativeLayout rl1;
	RelativeLayout rl2;
	RelativeLayout snowLayout1;
	RelativeLayout snowLayout2;

	TextView loading;
	TextView noAvatarText;

	ImageView title;
	ImageView firstImage;
	ImageView secondImage;

	ImageView[][] snow = new ImageView[2][10];

	Context context = this;

	TranslateAnimation leftToRight;
	TranslateAnimation rightToLeft;
	TranslateAnimation down1;
	TranslateAnimation down2;

	private MediaPlayer mp;
	private final static int MAX_VOLUME = 100;

	int width;      // Screen width
	int height;     // Screen height
	int midX;       // Top left corner of a centered button
	int mpPos = 0;  // Current position in the song (Updates when the activity is paused)

	int[] player_id = new int[2];       // Holds each player's avatar id

	boolean isLeft = false;     // Are the initial buttons (Start, How To Play, Quit) off screen to the left
	boolean hasClickedSignIn;   // Was sign in clicked?
	boolean fromBoard;          // Is this activity being launched from BoardActivity?

	GoogleApiClient client;
	String signInStatus = "Sign In";
	String connectedStatus = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.titlescreen);

		// Create media player for background song
		mp = MediaPlayer.create(this, R.raw.song);
		mp.setLooping(true);
		if (getIntent().hasExtra("Song")) mpPos = getIntent().getExtras().getInt("Song");
		if (getIntent().hasExtra("Board")) fromBoard = true;
		if (getIntent().hasExtra("SignedIn")) connectedStatus = getIntent().getExtras().getString("SignedIn");


		mp.seekTo(mpPos);

		// Create client to connect to Google Play Games
		client = new GoogleApiClient.Builder(this)
				.addApi(Games.API)
				.addScope(Games.SCOPE_GAMES)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		if (!fromBoard || connectedStatus.equals("Connected")) client.connect();
		if (client.isConnected()) signInStatus = "Sign Out";

		// Formula to modify volume from https://stackoverflow.com/questions/5215459/android-mediaplayer-setvolume-function
		int soundVolume = 75;
		final float volume = (float) (1 - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)));
		mp.setVolume(volume, volume);

		mp.start();

		rl = (RelativeLayout) findViewById(R.id.rl);    // All views will be placed in this layout

		// Get screen dimensions
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;

		// Create title image
		title = new ImageView(this);
		title.setLayoutParams(new RelativeLayout.LayoutParams((int) (height*4.0/10.0), (int) (height*3.0/10.0)));
		title.setBackgroundResource(R.drawable.yut);
		title.setX((int) (width/2.0 - height*2.0/10.0));
		title.setY((int) (height/10.0));
		rl.addView(title);

		// Set up the avatar shop
		Shop.Instance.initializeShop(context);

		/*
		 * #########################################################################################
		 *                                Begin setup of title screen buttons
		 * #########################################################################################
		 */

		midX = width/4;         // Top left corner of a centered button

		// The layout holding the start, how to play, options, and quit buttons
		rl1 = new RelativeLayout(this);
		rl1.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		rl1.setX(0);

		// The layout holding the one player, two player, and back buttons
		rl2 = new RelativeLayout(this);
		rl2.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		rl2.setX(width);

		// The layout holding the first group of snowflakes (alternates falling with the other group)
		snowLayout1 = new RelativeLayout(this);
		snowLayout1.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		snowLayout1.setY(-height);

		// The layout holding the second group of snowflakes (alternates falling with the other group)
		snowLayout2 = new RelativeLayout(this);
		snowLayout2.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		snowLayout2.setY(-height);

		startButton = new Button(this);
		startButton.setBackgroundResource(R.drawable.startbutton);
		startButton.setId(View.generateViewId());
		startButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		startButton.setOnClickListener(this);
		startButton.setX(midX);
		startButton.setY((int) (height * 5.5 / 10.0));
		rl1.addView(startButton);

		helpButton = new Button(this);
		helpButton.setBackgroundResource(R.drawable.howtoplay);
		helpButton.setId(View.generateViewId());
		helpButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		helpButton.setOnClickListener(this);
		helpButton.setX(midX);
		helpButton.setY((int) (height * 6.5 / 10.0));
		rl1.addView(helpButton);

		settingsButton = new Button(this);
		settingsButton.setBackgroundResource(R.drawable.settings);
		settingsButton.setId(View.generateViewId());
		settingsButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		settingsButton.setOnClickListener(this);
		settingsButton.setX(midX);
		settingsButton.setY((int) (height * 7.5 / 10.0));
		rl1.addView(settingsButton);

		quitButton = new Button(this);
		quitButton.setBackgroundResource(R.drawable.quit);
		quitButton.setId(View.generateViewId());
		quitButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		quitButton.setOnClickListener(this);
		quitButton.setX(midX);
		quitButton.setY((int) (height * 8.5 / 10.0));
		rl1.addView(quitButton);

		onePlayerButton = new Button(this);
		onePlayerButton.setBackgroundResource(R.drawable.oneplayer);
		onePlayerButton.setId(View.generateViewId());
		onePlayerButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		onePlayerButton.setOnClickListener(this);
		onePlayerButton.setX(midX);
		onePlayerButton.setY(startButton.getY());
		rl2.addView(onePlayerButton);

		twoPlayerButton = new Button(this);
		twoPlayerButton.setBackgroundResource(R.drawable.twoplayer);
		twoPlayerButton.setId(View.generateViewId());
		twoPlayerButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		twoPlayerButton.setOnClickListener(this);
		twoPlayerButton.setX(midX);
		twoPlayerButton.setY(helpButton.getY());
		rl2.addView(twoPlayerButton);

		shopButton = new Button(this);
		shopButton.setBackgroundResource(R.drawable.shop);
		shopButton.setId(View.generateViewId());
		shopButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		shopButton.setOnClickListener(this);
		shopButton.setX(midX);
		shopButton.setY(settingsButton.getY());
		rl2.addView(shopButton);

		backButton = new Button(this);
		backButton.setBackgroundResource(R.drawable.back);
		backButton.setId(View.generateViewId());
		backButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		backButton.setOnClickListener(this);
		backButton.setX(midX);
		backButton.setY(quitButton.getY());
		rl2.addView(backButton);

		rl.addView(rl1);
		rl.addView(rl2);
		rl.addView(snowLayout1);
		rl.addView(snowLayout2);

		// Sets up loading text (shown when BoardActivity is loading)
		loading = new TextView(this);
		loading.setId(View.generateViewId());
		loading.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		loading.setOnClickListener(this);
		loading.setGravity(Gravity.CENTER);
		loading.setTextColor(Color.BLACK);
		loading.setTextSize(20f);
		loading.setX(midX);
		loading.setY(helpButton.getY());
		loading.setText(R.string.loading);
		loading.setVisibility(View.INVISIBLE);
		rl.addView(loading);

		/*
		 * #########################################################################################
		 *                                End setup of title screen buttons
		 * #########################################################################################
		 */

		int BUTTON_SLIDE_DURATION = 600;
		int SNOW_FALL_DURATION = 9000;

		/*
		 * Animation for moving all title screen buttons to the left.
		 * Called when the start button is clicked.
		 */
		rightToLeft = new TranslateAnimation(0, -width, 0, 0);
		rightToLeft.setDuration(BUTTON_SLIDE_DURATION);
		rightToLeft.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				setInitialButtonClickable(false);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				shiftButtonsLeft();
				setModeButtonClickable(true);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
		});

		/*
		 * Animation for moving all title screen buttons to the right.
		 * Called when the back button is clicked or the phone's back button is pressed.
		 */
		leftToRight = new TranslateAnimation(0, width, 0, 0);
		leftToRight.setDuration(BUTTON_SLIDE_DURATION);
		leftToRight.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				setModeButtonClickable(false);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				shiftButtonsRight();
				setInitialButtonClickable(true);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
		});

		// Animation for falling snow for the first group of snowflakes
		down1 = new TranslateAnimation(0, 0, 0, height*2);
		down1.setDuration(SNOW_FALL_DURATION);
		down1.setRepeatCount(-1);
		down1.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				for (int j = 0; j < snow[0].length; j++){
					snow[0][j].setX((int) (Math.random() * (width - width/50)));
					snow[0][j].setY((int) (Math.random() * (height - width/50)));
				}
			}
		});

		// Animation for falling snow for the second group of snowflakes
		down2 = new TranslateAnimation(0, 0, 0, height*2);
		down2.setDuration(SNOW_FALL_DURATION);
		down2.setRepeatCount(-1);
		down2.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				for (int j = 0; j < snow[1].length; j++){
					snow[1][j].setX((int) (Math.random() * (width - width/50)));
					snow[1][j].setY((int) (Math.random() * (height - width/50)));
				}
			}
		});

		for (int i = 0; i < snow.length; i++) {
			for (int j = 0; j < snow[i].length; j++) {
				snow[i][j] = new ImageView(this);
				snow[i][j].setLayoutParams(new RelativeLayout.LayoutParams(width / 50, width / 50));
				snow[i][j].setBackgroundResource(R.drawable.snow);
				snow[i][j].setX((int) (Math.random() * (width - width/50)));
				snow[i][j].setY((int) (Math.random() * (height - width/50)));

				if (i == 0) snowLayout1.addView(snow[i][j]);
				else snowLayout2.addView(snow[i][j]);
			}
		}

		// Start the falling snow for the first group of snowflakes
		snowLayout1.startAnimation(down1);

		// Delay the falling snow for the second group of snowflakes
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
				snowLayout2.startAnimation(down2);
            }
        }, SNOW_FALL_DURATION/2);

		rl1.bringToFront();
		rl2.bringToFront();

		/*
		 * Set up background image depending on the time of day
		 */
		Calendar time = Calendar.getInstance();
		int hour = time.get(Calendar.HOUR_OF_DAY);
		if (hour >= 0 && hour < 5) rl.setBackgroundResource(R.drawable.backgroundnight);
		else if (hour >= 5 && hour < 9) rl.setBackgroundResource(R.drawable.backgrounddawn);
		else if (hour >= 9 && hour < 18) rl.setBackgroundResource(R.drawable.backgroundnoon);
		else if (hour >= 18 && hour < 21) rl.setBackgroundResource(R.drawable.backgrounddawn);
		else if (hour >= 21 && hour <= 23) rl.setBackgroundResource(R.drawable.backgroundnight);
	}

	/*
	 * Sets the clickability of the initial buttons
	 */
	private void setInitialButtonClickable(boolean b){
		startButton.setClickable(b);
		helpButton.setClickable(b);
		quitButton.setClickable(b);
	}

	/*
	 * Sets the clickability of the mode buttons
	 */
	private void setModeButtonClickable(boolean b){
		onePlayerButton.setClickable(b);
		twoPlayerButton.setClickable(b);
		backButton.setClickable(b);
	}

	/*
	 * Sets the new locations of the buttons after the right to left animation ends.
	 * Sets the mode buttons clickable
	 */
	private void shiftButtonsLeft(){
		if (!isLeft) {
			isLeft = true;

			rl1.clearAnimation();
			rl2.clearAnimation();

			rl1.setX(-width);
			rl2.setX(0);

			setModeButtonClickable(true);
		}
	}

	/*
	 * Sets the new locations of the buttons after the left to right animation ends.
	 * Sets the starting buttons clickable
	 */
	private void shiftButtonsRight(){
		if (isLeft){
			isLeft = false;

			rl1.clearAnimation();
			rl2.clearAnimation();

			rl1.setX(0);
			rl2.setX(width);

			setInitialButtonClickable(true);
		}
	}


	public void onConnected(Bundle connectionHint){
		signInStatus = "Sign Out";
	}

	public void onConnectionSuspended(int cause){
		// Attempt to reconnect
		client.connect();
	}

	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		if (mResolvingConnectionFailure) {
			// already resolving
			return;
		}

		// if the sign-in button was clicked or if auto sign-in is enabled,
		// launch the sign-in flow
		if (mSignInClicked || mAutoStartSignInFlow) {
			mAutoStartSignInFlow = false;
			mSignInClicked = false;
			mResolvingConnectionFailure = true;

			// Attempt to resolve the connection failure using BaseGameUtils.
			// The R.string.signin_other_error value should reference a generic
			// error string in your strings.xml file, such as "There was
			// an issue with sign-in, please try again later."
			if (!BaseGameUtils.resolveConnectionFailure(this,
					client, connectionResult,
					RC_SIGN_IN, "There was an issue with sign-in, please try again later.")) {
				mResolvingConnectionFailure = false;
			}
		}

		// Put code here to display the sign-in button
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
		mp.seekTo(mpPos);
		mp.start();
		if (hasClickedSignIn) {
			hasClickedSignIn = false;
			client.connect();
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

		client.disconnect();
	}

	/*
	 * If back is pressed while the mode buttons are displayed, shift the buttons right to show the initial buttons
	 * Otherwise, quit the app
	 */
	@Override
	public void onBackPressed() {
		if (isLeft) showInitialButtons();
		else finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void signInClicked() {
		mSignInClicked = true;
		hasClickedSignIn = true;
		client.connect();
	}

	private void signOutClicked() {
		mSignInClicked = false;
		Games.signOut(client);
		client.disconnect();
		if (!client.isConnected()){
			Toast savedToast = Toast.makeText(getApplicationContext(), "Successfully disconnected", Toast.LENGTH_SHORT);
			savedToast.show();
		}

		signInStatus = "Sign In";
	}

	protected void onActivityResult(int requestCode, int resultCode,
	                                Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			mSignInClicked = false;
			mResolvingConnectionFailure = false;
			if (resultCode == RESULT_OK) {
				client.connect();
			} else {
				// Bring up an error dialog to alert the user that sign-in
				// failed. The R.string.signin_failure should reference an error
				// string in your strings.xml file that tells the user they
				// could not be signed in, such as "Unable to sign in."
//				BaseGameUtils.showActivityResultError(this,	requestCode, resultCode, R.string.signin_failure);
				Toast savedToast = Toast.makeText(getApplicationContext(), "Unable to sign in", Toast.LENGTH_SHORT);
				savedToast.show();
			}
		}
	}

	/*
	 * Handles all clicks
	 */
	@Override
	public void onClick(View v) {
		if(v.getId() == onePlayerButton.getId() || v.getId() == twoPlayerButton.getId()) {  // Start playing the game
			showLoading();

			final boolean isOnePlayer = (v.getId() == onePlayerButton.getId());

			Handler handler = new Handler();
			handler.post(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(context, BoardActivity.class);
					intent.putExtra("Computer", isOnePlayer);
					if (client != null && client.isConnected()) intent.putExtra("SignedIn", "Connected");
					else intent.putExtra("SignedIn", "Disconnected");
					intent.putExtra("Song", mp.getCurrentPosition());
					startActivity(intent);
					finish();
				}
			});
		}
		else if (v.getId() == startButton.getId()){
			showModeButtons();  // start animation of right to left
		}
		else if (switchButton != null && v.getId() == switchButton.getId()){
			Shop.Instance.switchAvatars();

			float x = firstImage.getX();
			firstImage.setX(secondImage.getX());
			secondImage.setX(x);

			ImageView tempImage = firstImage;
			firstImage = secondImage;
			secondImage = tempImage;

			p1spin.setSelection(0);
			p2spin.setSelection(0);
		}
		else if (v.getId() == backButton.getId()) {
			showInitialButtons();   // start animation of left to right
		}
		else if (v.getId() == shopButton.getId()) {
			showShop();             // Show the shop interface
		}
		else if (v.getId() == helpButton.getId()){  // Bring up how to play dialog
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("How to play");
			ScrollView sv = new ScrollView(this);
			TextView tv = new TextView(this);
			tv.setPadding(0, 40, 0, 40);
			tv.setText(R.string.guide);
			tv.setTextSize(20f);
			tv.setGravity(Gravity.CENTER);
			sv.addView(tv);
			adb.setView(sv);
			adb.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			adb.show();
		}
		else if (v.getId() == quitButton.getId()){
			finish();
		}
		else if (v.getId() == settingsButton.getId()){  // Bring up settings dialog
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			final CharSequence[] items = {"Share", "Credits", "Rate", "Achievements", signInStatus, "Close"};
			adb.setTitle("Options");
			adb.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0){
						String message = "I am playing a board game called Yut! Try it out at https://play.google.com/store/apps/details?id=com.jeffreychan.yunnori";
						Intent share = new Intent(Intent.ACTION_SEND);
						share.setType("text/plain");
						share.putExtra(Intent.EXTRA_TEXT, message);
						startActivity(Intent.createChooser(share, "Share"));
					}
					else if (item == 1){
						AlertDialog.Builder adb = new AlertDialog.Builder(context);
						adb.setTitle("Credits");
						ScrollView sv = new ScrollView(context);
						TextView tv = new TextView(context);
						tv.setPadding(0, 40, 0, 40);
						tv.setText(R.string.credits);
						tv.setTextSize(20f);
						tv.setGravity(Gravity.CENTER);
						sv.addView(tv);
						adb.setView(sv);
						adb.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.cancel();
							}
						});
						adb.show();
					}
					else if (item == 2){
						Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.jeffreychan.yunnori"));
						startActivity(intent);
					}
					else if (item == 3){
						if (client.isConnected()) {
							startActivityForResult(Games.Achievements.getAchievementsIntent(client), 0);
						} else {
							Toast savedToast = Toast.makeText(getApplicationContext(), "You must be signed in to view achievements", Toast.LENGTH_SHORT);
							savedToast.show();
						}
					}
					else if (item == 4){
						if (signInStatus.equals("Sign In")) signInClicked();
						else signOutClicked();
					}
				}
			});
			adb.show();
		}
	}

	/*
	 * Start animation to shift all buttons right (true X,Y locations do not change though)
	 */
	private void showInitialButtons(){
		rl1.startAnimation(leftToRight);
		rl2.startAnimation(leftToRight);

	}

	/*
	 * Start animation to shift all buttons left (true X,Y locations do not change though)
	 */
	private void showModeButtons(){
		rl1.startAnimation(rightToLeft);
		rl2.startAnimation(rightToLeft);
	}

	/*
	 * Hide all buttons and show the loading text
	 */
	private void showLoading(){
		startButton.setVisibility(View.INVISIBLE);
		helpButton.setVisibility(View.INVISIBLE);
		settingsButton.setVisibility(View.INVISIBLE);
		quitButton.setVisibility(View.INVISIBLE);
		twoPlayerButton.setVisibility(View.INVISIBLE);
		onePlayerButton.setVisibility(View.INVISIBLE);
		shopButton.setVisibility(View.INVISIBLE);
		backButton.setVisibility(View.INVISIBLE);

		loading.setVisibility(View.VISIBLE);
	}

	/*
	 * The main dialog for the avatar shop
	 * Allows the user to either set the avatars for each player or buy a new avatar
	 */
	private void showShop(){

		// Create alert dialog for the shop
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Avatar Shop");

		// Create a linear layout to hold all the items in this dialog
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER);

		// Create a welcome message
		TextView welcomeText = new TextView(this);
		welcomeText.setPadding(0, 40, 0, 40);
		welcomeText.setText(R.string.welcome);
		welcomeText.setTextSize(20f);
		welcomeText.setGravity(Gravity.CENTER);
		linearLayout.addView(welcomeText);

		// Create a welcome image
		ImageView welcomeImage = new ImageView(this);
		welcomeImage.setBackgroundResource(R.drawable.welcome);
		welcomeImage.setLayoutParams(new LinearLayout.LayoutParams(width/2, width/3));
		linearLayout.addView(welcomeImage);

		// Holds the two buttons for setting avatars or buying avatars
		LinearLayout buttons = new LinearLayout(this);
		buttons.setOrientation(LinearLayout.HORIZONTAL);

		// Set up button for changing avatars
		TextView setAvatarButton = new AutofitTextView(this);
		setAvatarButton.setBackgroundResource(R.drawable.blank);
		setAvatarButton.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 2));
		setAvatarButton.setText(R.string.change);
		setAvatarButton.setGravity(Gravity.CENTER);
		setAvatarButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setAvatar();
			}
		});

		// Create spaces in between buttons
		Space space1 = new Space(this);
		space1.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 1));
		Space space2 = new Space(this);
		space2.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 1));
		Space space3 = new Space(this);
		space3.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 1));

		// Set up button for buying avatars
		TextView buyAvatarButton = new AutofitTextView(this);
		buyAvatarButton.setBackgroundResource(R.drawable.blank);
		buyAvatarButton.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 2));
		buyAvatarButton.setText(R.string.buy);
		buyAvatarButton.setGravity(Gravity.CENTER);
		buyAvatarButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buyAvatar();
			}
		});

		// Add all views to the dialog
		buttons.addView(space1);
		buttons.addView(setAvatarButton);
		buttons.addView(space2);
		buttons.addView(buyAvatarButton);
		buttons.addView(space3);

		linearLayout.addView(buttons);
		adb.setView(linearLayout);

		// Leave shop button
		adb.setPositiveButton("Leave Avatar Shop", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});

		// Show the AlertDialog
		adb.show();
	}

	/*
     * The dialog for buying new avatars
     * Allows the user to buy a new avatar for use in the game
     */
	private void buyAvatar(){

		// Dialog for buying avatars
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Buy Avatar");

		// LinearLayout to hold everything in this dialog
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		// Message describing this dialog to the user
		TextView spendText = new TextView(this);
		spendText.setPadding(0, 40, 0, 40);
		spendText.setText(R.string.spend);
		spendText.setTextSize(20f);
		spendText.setGravity(Gravity.CENTER);
		linearLayout.addView(spendText);

		// Displays the number of coins the user has
		final TextView coinText = new TextView(this);
		coinText.setPadding(0, 40, 0, 40);
		String currentCoins = "You have " + Shop.Instance.getCoins() + " coin(s) remaining";
		coinText.setText(currentCoins);
		coinText.setTextSize(20f);
		coinText.setGravity(Gravity.CENTER);
		linearLayout.addView(coinText);

		// This will hold all the possible animals to buy
		ScrollView sv = new ScrollView(this);
		LinearLayout list = new LinearLayout(this);
		list.setOrientation(LinearLayout.VERTICAL);

		// Get the list of locked avatars and display them
		ArrayList<String> avatars = Shop.Instance.getLockedAvatars();

		// Message saying to check back in the future for new avatars
		noAvatarText = new TextView(this);
		noAvatarText.setPadding(0, 40, 0, 40);
		noAvatarText.setText(R.string.future);
		noAvatarText.setTextSize(20f);
		noAvatarText.setGravity(Gravity.CENTER);
		noAvatarText.setVisibility(View.GONE);
		linearLayout.addView(noAvatarText);

		// If no rows to display, show the check back later message
		if (avatars.size() == 0){
			noAvatarText.setVisibility(View.VISIBLE);
		}

		for (final String s : avatars){

			// Set up row for this avatar
			final LinearLayout rowItem = new LinearLayout(this);
			rowItem.setOrientation(LinearLayout.HORIZONTAL);
			rowItem.setGravity(Gravity.CENTER);

			// Set up image for this avatar
			ImageView avatarImage = new ImageView(this);
			avatarImage.setBackgroundResource(Shop.Instance.getAnim(s));
			avatarImage.setLayoutParams(new LinearLayout.LayoutParams(0, height / 5, 8));
			rowItem.addView(avatarImage);
			AnimationDrawable sealAnim = (AnimationDrawable) avatarImage.getBackground();
			sealAnim.start();

			Space space1 = new Space(this);
			space1.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 1));
			rowItem.addView(space1);

			// Set up buy button for this avatar
			Button buyButton = new Button(this);
			buyButton.setBackgroundResource(R.drawable.blank);
			String buyText = Shop.Instance.getCost(s) + " Coins";
			buyButton.setText(buyText);
			buyButton.setLayoutParams(new LinearLayout.LayoutParams(0, height / 7, 6));
			rowItem.addView(buyButton);
			buyButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					// Try to make purchase
					boolean isSuccess = Shop.Instance.makePurchase(s);
					if (isSuccess){     // User has enough coins
						rowItem.setVisibility(View.GONE);
						String currentCoins = "You have " + Shop.Instance.getCoins() + " coin(s) remaining";
						coinText.setText(currentCoins);

						Toast savedToast = Toast.makeText(getApplicationContext(), "Purchase successful", Toast.LENGTH_SHORT);
						savedToast.show();

						if (Shop.Instance.getLockedAvatars().size() == 0) noAvatarText.setVisibility(View.VISIBLE);

					} else {            // User does not have enough coins
						Toast savedToast = Toast.makeText(getApplicationContext(), "Not enough coins", Toast.LENGTH_SHORT);
						savedToast.show();
					}
				}
			});

			Space space2 = new Space(this);
			space2.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 1));
			rowItem.addView(space2);

			// Add the row to the list of avatars to buy
			list.addView(rowItem);
		}

		// Add everything into the dialog
		sv.addView(list);
		linearLayout.addView(sv);
		adb.setView(linearLayout);

		// Back button
		adb.setPositiveButton("Back", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});

		// Display the AlertDialog
		adb.show();
	}

	private void setAvatar(){

		// Set the avatar images to the saved avatars
		String[] s = Shop.Instance.getAnimals();
		for (int i = 0; i < 2; i++) player_id[i] = Shop.Instance.getImage(s[i]);

		// Create an alert dialog for changing avatars
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Change Avatar");

		// Create a linear layout to hold all the items
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		TextView tv = new TextView(this);
		tv.setPadding(0, 40, 0, 40);
		tv.setText(R.string.set_avatar);
		tv.setTextSize(20f);
		tv.setGravity(Gravity.CENTER);
		linearLayout.addView(tv);

		/*
		 * -----------------------------------------------------------------------------------------
		 *                                Create Player Text
		 * -----------------------------------------------------------------------------------------
		 */

		// Linear layout to hold all the player text
		LinearLayout players = new LinearLayout(this);
		players.setOrientation(LinearLayout.HORIZONTAL);
		players.setPadding(0, 50, 0, 0);

		// Create player 1 text
		TextView player1 = new TextView(this);
		player1.setText(R.string.player1);
		player1.setLayoutParams(new LinearLayout.LayoutParams(0, height/20, 2));
		player1.setTextSize(20f);
		player1.setGravity(Gravity.CENTER);
		players.addView(player1);

		// Create space in between player texts
		Space space = new Space(this);
		space.setLayoutParams(new LinearLayout.LayoutParams(0, height/20, 1));
		players.addView(space);

		// Create player 2 text
		TextView player2 = new TextView(this);
		player2.setText(R.string.player2);
		player2.setLayoutParams(new LinearLayout.LayoutParams(0, height/20, 2));
		player2.setTextSize(20f);
		player2.setGravity(Gravity.CENTER);
		players.addView(player2);

		// Add all views to the linear layout
		linearLayout.addView(players);

		/*
		 * -----------------------------------------------------------------------------------------
		 *                                   Create Player Icons
		 * -----------------------------------------------------------------------------------------
		 */

		// Linear layout to hold player icons
		LinearLayout icons = new LinearLayout(this);
		icons.setOrientation(LinearLayout.HORIZONTAL);
		icons.setGravity(Gravity.CENTER);

		// Avatar for player 1
		firstImage = new ImageView(this);
		firstImage.setBackgroundResource(player_id[0]);
		firstImage.setLayoutParams(new LinearLayout.LayoutParams(0, height/5, 2));
		icons.addView(firstImage);

		// Switch button that allows the player avatars to be swapped
		switchButton = new Button(this);
		switchButton.setBackgroundResource(R.drawable.switchx);
		switchButton.setLayoutParams(new LinearLayout.LayoutParams(0, height/7, 1));
		switchButton.setOnClickListener(this);
		icons.addView(switchButton);

		// Avatar for player 2
		secondImage = new ImageView(this);
		secondImage.setBackgroundResource(player_id[1]);
		secondImage.setLayoutParams(new LinearLayout.LayoutParams(0, height/5, 2));
		icons.addView(secondImage);

		// Add all views to the linear layout
		linearLayout.addView(icons);

		/*
		 * -----------------------------------------------------------------------------------------
		 *                             Create Player Drop Down Menus
		 * -----------------------------------------------------------------------------------------
		 */

		// Linear layout for the dropdown menus
		LinearLayout menus = new LinearLayout(this);
		menus.setOrientation(LinearLayout.HORIZONTAL);
		menus.setPadding(0, 0, 0, 100);
		menus.setGravity(Gravity.CENTER);

		/*
		 * Populate drop down menus with unlocked avatars
		 * Default selection is the word "Select"
		 */
		ArrayList<String> list = new ArrayList<>();
		list.add("Select");
		list.addAll(Shop.Instance.getUnlockedAvatars());

		// Dropdown menu for player 1
		p1spin = new Spinner(this);
		p1spin.setGravity(Gravity.CENTER);
		p1spin.setId(View.generateViewId());
		p1spin.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_center_text, list);
		adapter.setDropDownViewResource(R.layout.spinner_center_text);
		p1spin.setOnItemSelectedListener(this);
		p1spin.setAdapter(adapter);
		p1spin.setSelection(0);
		menus.addView(p1spin);

		// Space in between dropdown menus
		Space menuSpace = new Space(this);
		menuSpace.setLayoutParams(new LinearLayout.LayoutParams(0, height/10, 1));
		menus.addView(menuSpace);

		// Dropdown menu for player 2
		p2spin = new Spinner(this);
		p2spin.setGravity(Gravity.CENTER);
		p2spin.setId(View.generateViewId());
		p2spin.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
		ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.spinner_center_text, list);
		adapter2.setDropDownViewResource(R.layout.spinner_center_text);
		p2spin.setAdapter(adapter2);
		p2spin.setOnItemSelectedListener(this);
		p2spin.setSelection(0);
		menus.addView(p2spin);

		// Add all views to the linear layout
		linearLayout.addView(menus);

		// Set the view for the dialog
		adb.setView(linearLayout);

		// Save button - checks that the two avatars are not the same
		adb.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String[] s = Shop.Instance.getAnimals();

				if (s[0].equals(s[1])){
					Shop.Instance.reset();
					Toast savedToast = Toast.makeText(getApplicationContext(), "Cannot have duplicate animals", Toast.LENGTH_SHORT);
					savedToast.show();
				}
				else {
					Shop.Instance.saveAvatars();
					dialog.cancel();
				}
			}
		});

		// Back button
		adb.setNegativeButton("Back", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Shop.Instance.reset();
				dialog.cancel();
			}
		});

		// Show the dialog
		adb.show();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String[] s = Shop.Instance.getAnimals();

		if (parent.getId() == p1spin.getId()) {
			String item = (String) parent.getItemAtPosition(position);
			if (!item.equalsIgnoreCase("Select")) {
				if (!s[1].equalsIgnoreCase(item)) {
					Shop.Instance.changeAvatar(1, item);
					firstImage.setBackgroundResource(Shop.Instance.getImage(item));
				} else {
					p1spin.setSelection(0);
					Toast savedToast = Toast.makeText(getApplicationContext(), "Cannot have duplicate animals", Toast.LENGTH_SHORT);
					savedToast.show();
				}
			}
		}
		else if (parent.getId() == p2spin.getId()) {
			String item = (String) parent.getItemAtPosition(position);
			if (!item.equalsIgnoreCase("Select")) {
				if (!s[0].equalsIgnoreCase(item)) {
					Shop.Instance.changeAvatar(2, item);
					secondImage.setBackgroundResource(Shop.Instance.getImage(item));
				} else {
					p2spin.setSelection(0);
					Toast savedToast = Toast.makeText(getApplicationContext(), "Cannot have duplicate animals", Toast.LENGTH_SHORT);
					savedToast.show();
				}
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
}