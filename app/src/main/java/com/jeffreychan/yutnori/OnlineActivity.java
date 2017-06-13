package com.jeffreychan.yutnori;

/* Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import com.google.example.games.basegameutils.BaseGameUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Button Clicker 2000. A minimalistic game showing the multiplayer features of
 * the Google Play game services API. The objective of this game is clicking a
 * button. Whoever clicks the button the most times within a 20 second interval
 * wins. It's that simple. This game can be played with 2, 3 or 4 players. The
 * code is organized in sections in order to make understanding as clear as
 * possible. We start with the integration section where we show how the game
 * is integrated with the Google Play game services API, then move on to
 * game-specific UI and logic.
 *
 * INSTRUCTIONS: To run this sample, please set up
 * a project in the Developer Console. Then, place your app ID on
 * res/values/ids.xml. Also, change the package name to the package name you
 * used to create the client ID in Developer Console. Make sure you sign the
 * APK with the certificate whose fingerprint you entered in Developer Console
 * when creating your Client Id.
 *
 * @author Bruno Oliveira (btco), 2013-04-26
 */
public class OnlineActivity extends GameActivity
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		View.OnClickListener, RealTimeMessageReceivedListener,
		RoomStatusUpdateListener, RoomUpdateListener {

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     */
	int mCurScreen = -1;

	// Room ID where the currently active game is taking place; null if we're
	// not playing.
	String mRoomId = null;

	// The participants in the currently active game
	ArrayList<Participant> mParticipants = null;
	ArrayList<String> participantIds = new ArrayList<>();

	// My participant ID in the currently active game
	String mMyId = null;
	// Opponent participant ID in the currently active game
	String opponentId = null;

	/*
	 * Message buffer for sending messages
	 *
	 * Byte 0 = Op
	 * Bytes 1-4 = Op Specific. 0 otherwise.
	 */
	byte[] mMsgBuf = new byte[5];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);

		// set up a click listener for everything we care about
		for (int id : CLICKABLES) {
			findViewById(id).setOnClickListener(this);
		}
	}

	/*
	 * Returns the layout for this activity
	 */
	@Override
	protected int getLayoutId(){
		return R.layout.online;
	}

	@Override
	public void onClick(View v) {

		if (isGameOver) return;
		if (turn == 1) return;  // not your turn

		if (v.getId() == R.id.button_sign_in) {
			mSignInClicked = true;
			client.connect();
		}
		else if (v.getId() == R.id.button_quick_game) {
			startQuickGame();   			// User wants to play against a random opponent right now
		}
		else if (v.getId() == R.id.rollButton) { // Called when roll button is clicked
			handleRoll();
			broadcastClick(Op.CLICK_ROLL_BUTTON, rollAmount);
		}
		else if (v.getId() == finish.getId()) {
			movePiece(32, Move.NORMAL); // 32 = finish location
			broadcastClick(Op.CLICK_FINISH, -1);
		}
		else if (v.getId() == offBoardPiece.getId()) {  // Image that represents both players' off board pieces
			showPossibleTiles(players[turn].findAvailablePiece());
			broadcastClick(Op.CLICK_OFF_BOARD_PIECE, -1);
		}
		else if (tile_ids.contains(v.getId())){    // Activates on tile click
			handleTileClick(v);
			broadcastClick(Op.CLICK_TILE, v.getId());
		}
		else if (player_ids.contains(v.getId())){  // Activates on animal click; animal covers tile
			handlePlayerClick(v);
			broadcastClick(Op.CLICK_PLAYER, v.getId());
		}
		else {
			hidePossibleTiles();    // Cancel move by clicking anything else
			broadcastClick(Op.CLICK_EMPTY, -1);
		}
	}

	void startQuickGame() {
		// quick-start a game with 1 randomly selected opponent
		final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
		Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
				MAX_OPPONENTS, 0);
		RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
		rtmConfigBuilder.setMessageReceivedListener(this);
		rtmConfigBuilder.setRoomStatusUpdateListener(this);
		rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		switchToScreen(R.id.screen_wait);
		keepScreenOn();
		resetGameVars();
		Games.RealTimeMultiplayer.create(client, rtmConfigBuilder.build());
	}

	@Override
	public void onActivityResult(int requestCode, int responseCode,
	                             Intent intent) {
		super.onActivityResult(requestCode, responseCode, intent);

		switch (requestCode) {
			case RC_WAITING_ROOM:
				// we got the result from the "waiting room" UI.
				if (responseCode == Activity.RESULT_OK) {
					// ready to start playing
					startGame();
				} else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
					// player indicated that they want to leave the room
					leaveRoom();
				} else if (responseCode == Activity.RESULT_CANCELED) {
					// Dialog was cancelled (user pressed back key, for instance). In our game,
					// this means leaving the room too. In more elaborate games, this could mean
					// something else (like minimizing the waiting room UI).
					leaveRoom();
				}
				break;
			case RC_SIGN_IN:
				mSignInClicked = false;
				mResolvingConnectionFailure = false;
				if (responseCode == RESULT_OK) {
					client.connect();
				} else {
					BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
				}
				break;
		}
		super.onActivityResult(requestCode, responseCode, intent);
	}

	// Activity is going to the background. We have to leave the current room.
	@Override
	public void onStop() {
		// if we're in a room, leave it.
		leaveRoom();

		// stop trying to keep the screen on
		stopKeepingScreenOn();

		if (client != null && client.isConnected()) {
			switchToMainScreen();
		} else {
			switchToScreen(R.id.screen_sign_in);
		}
		super.onStop();
	}

	// Activity just got to the foreground. We switch to the wait screen because we will now
	// go through the sign-in flow (remember that, yes, every time the Activity comes back to the
	// foreground we go through the sign-in flow -- but if the user is already authenticated,
	// this flow simply succeeds and is imperceptible).
	@Override
	public void onStart() {
		if (client == null) {
			switchToScreen(R.id.screen_sign_in);
		} else if (!client.isConnected()) {
			switchToScreen(R.id.screen_wait);
			client.connect();
		}
		super.onStart();
	}

	// Handle back key to make sure we cleanly leave a game if we are in the middle of one
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.rl) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			TextView tv = new TextView(this);
			tv.setPadding(0, 40, 0, 40);
			tv.setText("Are you sure you want to leave this match?\nThe game will not be saved.");
			tv.setTextSize(20f);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			adb.setView(tv);
			adb.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					leaveRoom();
				}
			});
			adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			adb.show();
			return true;
		}
		else {
			quit();
			return true;
		}
	}

	// Leave the room.
	void leaveRoom() {
		stopKeepingScreenOn();
		if (mRoomId != null) {
			Games.RealTimeMultiplayer.leave(client, this, mRoomId);
			mRoomId = null;
			switchToScreen(R.id.screen_wait);
		} else {
			switchToMainScreen();
		}
	}

	// Show the waiting room UI to track the progress of other players as they enter the
	// room and get connected.
	void showWaitingRoom(Room room) {
		// minimum number of players required for our game
		// For simplicity, we require everyone to join the game before we start it
		// (this is signaled by Integer.MAX_VALUE).
		final int MIN_PLAYERS = Integer.MAX_VALUE;
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(client, room, MIN_PLAYERS);

		// show waiting room UI
		startActivityForResult(i, RC_WAITING_ROOM);
	}

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

	@Override
	public void onConnected(Bundle connectionHint) {
		switchToMainScreen();
	}

	// Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
	// is connected yet).
	@Override
	public void onConnectedToRoom(Room room) {
		//get participants and my ID:
		mParticipants = room.getParticipants();
		mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(client));

		// save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
		if(mRoomId==null)
			mRoomId = room.getRoomId();
	}

	// Called when we've successfully left the room (this happens a result of voluntarily leaving
	// via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
	@Override
	public void onLeftRoom(int statusCode, String roomId) {
		// we have left the room; return to main screen.
		switchToMainScreen();
	}

	// Called when we get disconnected from the room. We return to the main screen.
	@Override
	public void onDisconnectedFromRoom(Room room) {
		mRoomId = null;
		showGameError();
	}

	// Show error message about game being cancelled and return to main screen.
	void showGameError() {
		BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
		switchToMainScreen();
	}

	// Called when room has been created
	@Override
	public void onRoomCreated(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			showGameError();
			return;
		}

		// save room ID so we can leave cleanly before the game starts.
		mRoomId = room.getRoomId();

		// show the waiting room UI
		showWaitingRoom(room);
	}

	// Called when room is fully connected.
	@Override
	public void onRoomConnected(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			showGameError();
			return;
		}
		updateRoom(room);
	}

	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			showGameError();
			return;
		}

		// show the waiting room UI
		showWaitingRoom(room);
	}

	// We treat most of the room update callbacks in the same way: we update our list of
	// participants and update the display. In a real game we would also have to check if that
	// change requires some action like removing the corresponding player avatar from the screen,
	// etc.
	@Override
	public void onPeerDeclined(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onPeerInvitedToRoom(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onP2PDisconnected(String participant) {
	}

	@Override
	public void onP2PConnected(String participant) {
	}

	@Override
	public void onPeerJoined(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onPeerLeft(Room room, List<String> peersWhoLeft) {
		updateRoom(room);
	}

	@Override
	public void onRoomAutoMatching(Room room) {
		updateRoom(room);
	}

	@Override
	public void onRoomConnecting(Room room) {
		updateRoom(room);
	}

	@Override
	public void onPeersConnected(Room room, List<String> peers) {
		updateRoom(room);
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> peers) {
		updateRoom(room);
	}

	void updateRoom(Room room) {
		if (room != null) {
			mParticipants = room.getParticipants();
		}
	}

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

	// Start the gameplay phase of the game.
	void startGame() {
		switchToScreen(R.id.rl);

		if (mParticipants == null) leaveRoom();

		// Determine who goes first by sorting all the participant ids. Whoever is first goes first.
		for (Participant p : mParticipants) {
			participantIds.add(p.getParticipantId());
		}
		Collections.sort(participantIds);
		if (mMyId.equals(participantIds.get(0))) {
			turn = 0;
			board.playerTurn = 0;
			oppTurn = 1;
			canRoll = true;
			turnText.setText(R.string.your_turn);
			opponentId = participantIds.get(1);
		}
		else {
			turn = 1;
			board.playerTurn = 1;
			oppTurn = 0;
			turnText.setText(R.string.opponent_turn);
			canRoll = false;
			opponentId = participantIds.get(0);
		}
	}

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

	// Called when we receive a real-time message from the network.
	@Override
	public void onRealTimeMessageReceived(RealTimeMessage rtm) {
		byte[] buf = rtm.getMessageData();
		String sender = rtm.getSenderParticipantId();

		// Try to verify the sender of the data
		if (!sender.equals(opponentId)) leaveRoom();

		// Get the view that the opponent clicked
		byte[] arr = { buf[1], buf[2], buf[3], buf[4] };
		ByteBuffer wrapped = ByteBuffer.wrap(arr); // big-endian by default
		int extra = wrapped.getInt();

		if (buf[0] == Op.CLICK_ROLL_BUTTON) {
			handleRoll(extra);
		}
		else if (buf[0] == Op.CLICK_FINISH) {
			movePiece(32, Move.NORMAL); // 32 = finish location
		}
		else if (buf[0] == Op.CLICK_OFF_BOARD_PIECE) {  // Image that represents both players' off board pieces
			showPossibleTiles(players[turn].findAvailablePiece());
		}
		else if (buf[0] == Op.CLICK_TILE){    // Activates on tile click
			handleTileClick(findViewById(extra));
		}
		else if (buf[0] == Op.CLICK_PLAYER){  // Activates on animal click; animal covers tile
			handlePlayerClick(findViewById(extra));
		}
		else if (buf[0] == Op.CLICK_EMPTY){
			hidePossibleTiles();    // Cancel move by clicking anything else
		}
		else {  // Invalid op
			leaveRoom();
		}
	}

	// Broadcast my score to everybody else.
	void broadcastClick(byte op, int viewId) {

		// First byte in message indicates the type of click
		mMsgBuf[0] = op;

		// Bytes 1-4 indicate the viewId (if applicable)
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		b.putInt(viewId);
		byte[] bytes = b.array();
		mMsgBuf[1] = bytes[0];
		mMsgBuf[2] = bytes[1];
		mMsgBuf[3] = bytes[2];
		mMsgBuf[4] = bytes[3];

		// Send to opponent
		for (Participant p : mParticipants) {
			if (p.getParticipantId().equals(mMyId))
				continue;
			if (p.getStatus() != Participant.STATUS_JOINED)
				continue;

			// Send the data
			Games.RealTimeMultiplayer.sendReliableMessage(client, null, mMsgBuf,
						mRoomId, p.getParticipantId());

		}
	}

	/**
	 * Handles the determination of the amount rolled when the roll button is clicked
	 * Decides what should happen next based on roll.
	 * Ex: Rolling 4 or 5 allows the user to roll again. Rolling -1 with no pieces on the board ends the turn.
	 *
	 * Once the rolling phase is completed, prompt the user to make a move with an appropriate message
	 */
	protected void handleRoll(int rAmount){
		rollAmount = rAmount;
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

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

	// This array lists everything that's clickable, so we can install click
	// event handlers.
	final static int[] CLICKABLES = {
			R.id.button_quick_game, R.id.button_sign_in
	};

	// This array lists all the individual screens our game has.
	final static int[] SCREENS = {
			R.id.screen_main, R.id.screen_sign_in,
			R.id.screen_wait, R.id.rl
	};

	void switchToScreen(int screenId) {
		// make the requested screen visible; hide all others.
		for (int id : SCREENS) {
			findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
		}
		mCurScreen = screenId;
	}

	void switchToMainScreen() {
		if (client != null && client.isConnected()) {
			switchToScreen(R.id.screen_main);
		}
		else {
			switchToScreen(R.id.screen_sign_in);
		}
	}

    /*
     * MISC SECTION. Miscellaneous methods.
     */


	// Sets the flag to keep this screen on. It's recommended to do that during
	// the
	// handshake when setting up a game, because if the screen turns off, the
	// game will be
	// cancelled.
	void keepScreenOn() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	// Clears the flag that keeps the screen on.
	void stopKeepingScreenOn() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Exits this activity and launches the title screen
	 */
	private void quit(){
		Intent intent = new Intent(this, TitleScreenActivity.class);
		intent.putExtra("Song", mp.getCurrentPosition());
		intent.putExtra("Board", true);
		if (client != null && client.isConnected()) intent.putExtra("SignedIn", "Connected");
		else intent.putExtra("SignedIn", "Disconnected");
		startActivity(intent);
		finish();
	}

	public void exit(View view) {
		quit();
	}

	/**
	 * If the user made a move (STACK, CAPTURE, or NORMAL),
	 * prepare the board for another move or end the turn
	 */
	protected void endMove(){

		if (isGameOver) return;

		if (players[turn].hasAllPiecesOnBoard() || capture){
			offBoardPiece.setVisibility(View.INVISIBLE);
			offBoardPieceAnimation.stop();
			offBoardPieceAnimation.selectDrawable(0);
		}

		if (capture) {
			String text;
			if (turn == 1) text = "Opponent Roll Again!";
			else text = "Roll Again!";

			turnText.setText(text);
			turnText.setVisibility(View.VISIBLE);
			tips.setVisibility(View.INVISIBLE);

			isRollDone = false;
			canRoll = true;
		}

		hidePossibleTiles();
		updateOffBoardImages();

		if ((!capture && board.rollEmpty()) || (board.hasOnlyNegativeRoll() && players[turn].hasNoPiecesOnBoard())) endTurn();

		capture = false;
	}

	/**
	 * Displays an AlertDialog with the winner and asks if the user wants to play again.
	 * Prevents buttons and text from appearing.
	 */
	protected void endGame(){

		isGameOver = true;

		Shop.Instance.addCoins(1);

		updateOffBoardImages();
		rollButton.setVisibility(View.INVISIBLE);
		turnText.setVisibility(View.INVISIBLE);
		tips.setVisibility(View.INVISIBLE);

		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		TextView tv = new TextView(this);
		tv.setPadding(0, 40, 0, 40);

		// Display message notifying winner and amount of coins earned
		String winner = "\nYou now have " + Shop.Instance.getCoins() + " coin(s)";
		if (players[0].hasWon()) winner = "You win!\n\nYou have earned 1 coins!" + winner;
		else winner = "Opponent wins!\n\nYou still earned 1 coin!" + winner;
		tv.setText(winner);

		tv.setTextSize(20f);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		adb.setView(tv);
		adb.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				leaveRoom();
			}
		});
		adb.setNeutralButton("Rate", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				leaveRoom();
				Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.jeffreychan.yunnori"));
				startActivity(intent);
			}
		});
		adb.show();
	}

	protected void resetGameVars() {

	}
}