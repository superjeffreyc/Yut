package com.example.yutnori;


import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {

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
		
		sealJumpImageView.setX(200f);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
}
