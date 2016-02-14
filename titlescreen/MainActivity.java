package com.jeffreychan.yutnori;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.atsukoshimizu.yutnori.R;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.titlescreen);
		ImageView penguinJumpImageView = (ImageView) findViewById(R.id.penguinjumpimageview);
		penguinJumpImageView.setBackgroundResource(R.drawable.penguinjumpanimation);
		AnimationDrawable penguinJumpAnimation = (AnimationDrawable) penguinJumpImageView.getBackground();
		penguinJumpAnimation.start();

		ImageView sealJumpImageView = (ImageView) findViewById(R.id.sealmoveimageview);
		sealJumpImageView.setBackgroundResource(R.drawable.sealmoveanimation);
		AnimationDrawable sealJumpAnimation = (AnimationDrawable) sealJumpImageView.getBackground();
		sealJumpAnimation.start();

		ImageView background = (ImageView) findViewById(R.id.titleBackground);
		background.setBackgroundResource(R.drawable.background);

		Button startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		startButton.setX(300f);
		startButton.setY(500f);

		sealJumpImageView.setX(200f);

        textView.setGravity(Gravity.CENTER_HORIZONTAL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_board, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		startActivity(new Intent(this, BoardActivity.class));
	}
}