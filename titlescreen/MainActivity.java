package com.example.atsukoshimizu.yutnori;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
<<<<<<< HEAD
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
=======
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.titlescreen);
>>>>>>> parent of 2abea32... added game board

        //Penguin jump animation
        ImageView penguinJumpImageView = (ImageView)findViewById(R.id.penguinjumpimageview);
        penguinJumpImageView.setBackgroundResource(R.drawable.penguinjumpanimation);
        AnimationDrawable penguinJumpAnimation = (AnimationDrawable) penguinJumpImageView.getBackground();
        penguinJumpAnimation.start();

        //Seal move animation
        ImageView sealMoveImageView = (ImageView)findViewById(R.id.sealmoveimageview);
        sealMoveImageView.setBackgroundResource(R.drawable.sealmoveanimation);
        AnimationDrawable sealMoveAnimation = (AnimationDrawable) sealMoveImageView.getBackground();
        sealMoveAnimation.start();

        penguinJumpImageView.setX(200f);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("ActioTn", null).show();
            }
        });
    }

<<<<<<< HEAD
		sealJumpImageView.setX(200f);
	}
=======
    public void startbutton(View view)
    {
        Intent intent = new Intent(this, ToActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
>>>>>>> parent of 2abea32... added game board

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
