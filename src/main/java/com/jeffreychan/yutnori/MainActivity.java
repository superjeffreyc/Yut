package com.jeffreychan.yutnori;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends Activity implements OnClickListener {

	boolean firstTime = true;
	ImageView penguinJumpImageView, sealJumpImageView;
	AnimationDrawable penguinJumpAnimation, sealJumpAnimation;
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

			sealJumpImageView.setX(2.75f * width / 5.0f);
			sealJumpImageView.setY(0.9f * height / 2.0f);

			penguinJumpImageView.setX(0);
			penguinJumpImageView.setY(0.9f * height / 2.0f);

			firstTime = false;
		}
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
		Intent intent = new Intent(this, BoardActivity.class);
		startActivity(intent);
	}
}