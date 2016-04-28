package com.jeffreychan.yutnori;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class TitleScreenActivity extends Activity implements OnClickListener {

	Button startButton;
	Button helpButton;
	Button quitButton;

	Button onePlayerButton;
	Button twoPlayerButton;
	Button backButton;

	RelativeLayout rl;
	RelativeLayout rl1;
	RelativeLayout rl2;

	TextView loading;

	ImageView title;

	Context context = this;

	TranslateAnimation leftToRight, rightToLeft;

	private MediaPlayer mp;
	private final static int MAX_VOLUME = 100;

	int width;      // Screen width
	int height;     // Screen height
	int midX;       // Top left corner of a centered button
	int mpPos = 0;

	boolean isLeft = false;     // Are the initial buttons (Start, How To Play, Quit) off screen to the left

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
		mp.seekTo(mpPos);

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

		title = new ImageView(this);
		title.setLayoutParams(new RelativeLayout.LayoutParams((int) (height*4.0/10.0), (int) (height*3.0/10.0)));
		title.setBackgroundResource(R.drawable.yut);
		title.setX((int) (width/2.0 - height*2.0/10.0));
		title.setY((int) (height/10.0));
		rl.addView(title);

		/*
		 * #########################################################################################
		 *                                Begin setup of title screen buttons
		 * #########################################################################################
		 */

		midX = width/4;         // Top left corner of a centered button

		rl1 = new RelativeLayout(this);
		rl1.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		rl1.setX(0);

		rl2 = new RelativeLayout(this);
		rl2.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		rl2.setX(width);

		startButton = new Button(this);
		startButton.setBackgroundResource(R.drawable.startbutton);
		startButton.setId(View.generateViewId());
		startButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		startButton.setOnClickListener(this);
		startButton.setX(midX);
		startButton.setY((int) (height * 6.0 / 10.0));
		rl1.addView(startButton);

		helpButton = new Button(this);
		helpButton.setBackgroundResource(R.drawable.howtoplay);
		helpButton.setId(View.generateViewId());
		helpButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		helpButton.setOnClickListener(this);
		helpButton.setX(midX);
		helpButton.setY((int) (height * 7.0 / 10.0));
		rl1.addView(helpButton);

		quitButton = new Button(this);
		quitButton.setBackgroundResource(R.drawable.quit);
		quitButton.setId(View.generateViewId());
		quitButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		quitButton.setOnClickListener(this);
		quitButton.setX(midX);
		quitButton.setY((int) (height * 8.0 / 10.0));
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

		int ANIMATION_DURATION = 600;

		/*
		 * Animation for moving all title screen buttons to the left.
		 * Called when the start button is clicked.
		 */
		rightToLeft = new TranslateAnimation(0, -width, 0, 0);
		rightToLeft.setDuration(ANIMATION_DURATION);
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
		leftToRight.setDuration(ANIMATION_DURATION);
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

	@Override
	public void onPause() {
		super.onPause();
		if (mp.isPlaying()) {
			mp.pause();
			mpPos = mp.getCurrentPosition();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mp.seekTo(mpPos);
		mp.start();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mp.isPlaying()) {
			mp.stop();
			mp.release();
		}
	}

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

	@Override
	public void onClick(View v) {
		if(v.getId() == onePlayerButton.getId() || v.getId() == twoPlayerButton.getId()) {
			showLoading();

			final boolean isOnePlayer = (v.getId() == onePlayerButton.getId());

			Handler handler = new Handler();
			handler.post(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(context, BoardActivity.class);
					intent.putExtra("Computer", isOnePlayer);
					intent.putExtra("Song", mp.getCurrentPosition());
					startActivity(intent);
					finish();
				}
			});
		}
		else if (v.getId() == startButton.getId()){
			showModeButtons();
		}
		else if (v.getId() == backButton.getId()) {
			showInitialButtons();
		}
		else if (v.getId() == helpButton.getId()){
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			ScrollView sv = new ScrollView(this);
			TextView tv = new TextView(this);
			tv.setPadding(0, 40, 0, 40);
			tv.setText(R.string.guide);
			tv.setTextSize(20f);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
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
	}

	private void showInitialButtons(){
		rl1.startAnimation(leftToRight);
		rl2.startAnimation(leftToRight);

	}

	private void showModeButtons(){
		rl1.startAnimation(rightToLeft);
		rl2.startAnimation(rightToLeft);
	}

	private void showLoading(){
		startButton.setVisibility(View.INVISIBLE);
		helpButton.setVisibility(View.INVISIBLE);
		quitButton.setVisibility(View.INVISIBLE);
		twoPlayerButton.setVisibility(View.INVISIBLE);
		onePlayerButton.setVisibility(View.INVISIBLE);
		backButton.setVisibility(View.INVISIBLE);

		loading.setVisibility(View.VISIBLE);
	}
}