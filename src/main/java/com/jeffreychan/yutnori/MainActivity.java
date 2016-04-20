package com.jeffreychan.yutnori;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	ImageView penguinJumpImageView, sealJumpImageView;
	AnimationDrawable penguinJumpAnimation, sealJumpAnimation;
	Button startButton, helpButton, settingsButton;
	int width, height;
	RelativeLayout rl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.titlescreen);

		rl = (RelativeLayout) findViewById(R.id.rl);

		penguinJumpImageView = (ImageView) findViewById(R.id.penguinjumpimageview);
		penguinJumpAnimation = (AnimationDrawable) penguinJumpImageView.getBackground();
		penguinJumpAnimation.start();

		sealJumpImageView = (ImageView) findViewById(R.id.sealmoveimageview);
		sealJumpAnimation = (AnimationDrawable) sealJumpImageView.getBackground();
		sealJumpAnimation.start();

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;

		startButton = new Button(this);
		startButton.setBackgroundResource(R.drawable.startbutton);
		startButton.setId(View.generateViewId());
		startButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		startButton.setOnClickListener(this);
		startButton.setX(width / 2 - startButton.getLayoutParams().width / 2);
		startButton.setY(height * 6 / 10);
		rl.addView(startButton);

		helpButton = new Button(this);
		helpButton.setBackgroundResource(R.drawable.helpbutton);
		helpButton.setId(View.generateViewId());
		helpButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		helpButton.setOnClickListener(this);
		helpButton.setX(width / 2 - helpButton.getLayoutParams().width / 2);
		helpButton.setY(height * 7 / 10);
		rl.addView(helpButton);

		settingsButton = new Button(this);
		settingsButton.setBackgroundResource(R.drawable.settings);
		settingsButton.setId(View.generateViewId());
		settingsButton.setLayoutParams(new RelativeLayout.LayoutParams(width / 2, height / 10));
		settingsButton.setOnClickListener(this);
		settingsButton.setX(width / 2 - settingsButton.getLayoutParams().width / 2);
		settingsButton.setY(height * 8 / 10);
		rl.addView(settingsButton);


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
		if(v.getId() == startButton.getId()) {
			Intent intent = new Intent(this, BoardActivity.class);
			startActivity(intent);
		}
		else if (v.getId() == helpButton.getId()){
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			WebView wv = new WebView(this);
			wv.loadUrl("https://en.wikipedia.org/wiki/Yut");
			wv.setVerticalScrollBarEnabled(true);
			wv.getSettings().setJavaScriptEnabled(true);
			wv.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return true;
				}
			});
			final Activity activity = this;
			wv.setWebChromeClient(new WebChromeClient() {
				private ProgressDialog mProgress;

				@Override
				public void onProgressChanged(WebView view, int progress) {
					if (mProgress == null) {
						mProgress = new ProgressDialog(activity);
						mProgress.show();
					}
					mProgress.setMessage("Loading " + String.valueOf(progress) + "%");
					if (progress == 100) {
						mProgress.dismiss();
						mProgress = null;
					}
				}
			});
			adb.setView(wv);
			adb.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			adb.show();
		}
		else if (v.getId() == settingsButton.getId()){
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			TextView tv = new TextView(this);
			tv.setPadding(0, 40, 0, 40);
			tv.setText("Coming soon!");
			tv.setTextSize(20f);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			adb.setView(tv);
			adb.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			adb.show();
		}
	}
}