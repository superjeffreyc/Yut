package com.jeffreychan.yutnori;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.view.ViewGroup.LayoutParams;

public class MainActivity extends Activity implements OnClickListener {

	boolean firstTime = true;
	ImageView penguinJumpImageView, sealJumpImageView, fallingStickImageView, background;
	AnimationDrawable penguinJumpAnimation, sealJumpAnimation, fallingStickAnimation;
	Button startButton;
	int width, height;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.titlescreen);


		penguinJumpImageView = (ImageView) findViewById(R.id.penguinjumpimageview);
		penguinJumpImageView.setBackgroundResource(R.drawable.penguinjumpanimation);
		penguinJumpAnimation = (AnimationDrawable) penguinJumpImageView.getBackground();
		penguinJumpAnimation.start();

		sealJumpImageView = (ImageView) findViewById(R.id.sealmoveimageview);
		sealJumpImageView.setBackgroundResource(R.drawable.sealmoveanimation);
		sealJumpAnimation = (AnimationDrawable) sealJumpImageView.getBackground();
		sealJumpAnimation.start();

//        fallingStickImageView = (ImageView) findViewById(R.id.fallingstickimageview);
//        fallingStickImageView.setBackgroundResource(R.drawable.fallingstickanimation);
//        fallingStickAnimation = (AnimationDrawable) fallingStickImageView.getBackground();
//        fallingStickAnimation.start();

//		background = (ImageView) findViewById(R.id.titleBackground);
//		background.setBackgroundResource(R.drawable.background);

		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;





	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (firstTime){
			//sealJumpImageView.setLayoutParams(new LayoutParams(width/4, width/4));
			//penguinJumpImageView.setLayoutParams(new LayoutParams(width/4, width/4));

			sealJumpImageView.setX(2.75f * width / 5.0f);
			sealJumpImageView.setY(0.9f * height / 2.0f);

			penguinJumpImageView.setX(0);
			penguinJumpImageView.setY(0.9f * height / 2.0f);

//			startButton.setY(3.0f * height / 4.0f);
//			startButton.setX(width / 3.0f);

			firstTime = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		//	int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}/ as you specify a parent activity in AndroidManifest.xml.
//
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, BoardActivity.class);
		try {
			startActivity(intent);
		} catch (Exception e){

		}
	}
}