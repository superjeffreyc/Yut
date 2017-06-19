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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import com.google.example.games.basegameutils.BaseGameUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Online mode for Yut
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

	Context context = this;
	Thread thread;

	// Room ID where the currently active game is taking place; null if we're
	// not playing.
	String mRoomId = null;
	int version = 0;

	// The participants in the currently active game
	ArrayList<Participant> mParticipants = null;
	ArrayList<String> participantIds = new ArrayList<>();

	// My participant ID in the currently active game
	String mMyId = null;
	// Opponent participant ID in the currently active game
	String opponentId = null;

	SparseIntArray IDtoRID = new SparseIntArray();
	SparseIntArray RIDtoID = new SparseIntArray();

	boolean isSendingData = false;
	boolean hasNetworkError = false;

	int frame = 0;
	int currentAckFrame = 0;

	static Semaphore room_id_semaphore = new Semaphore(1);  // Controls access to mRoomId
	static Semaphore current_frame_semaphore = new Semaphore(1);  // Controls access to currentAckFrame


	/*
	 * Message buffer for sending messages
	 *
	 * Byte 0 = Op
	 * Bytes 1-4 = An integer that is op specific. -1 if unused.
	 * Bytes 5-8 = Frame Number
	 */
	byte[] mMsgBuf = new byte[9];

	int resendCount = 0;

	private final RealTimeMultiplayer.ReliableMessageSentCallback mReliableMessageSentCallback =
			new RealTimeMultiplayer.ReliableMessageSentCallback() {
				@Override
				public void onRealTimeMessageSent(final int statusCode, final int tokenId,
				                                  final String recipientParticipantId) {

					if (statusCode != GamesStatusCodes.STATUS_OK) {
						hasNetworkError = true;
						leaveRoom();
					}
				}
			};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		findViewById(R.id.button_quick_game).setOnClickListener(this);
	}

	/*
	 * Returns the layout for this activity
	 */
	@Override
	protected int getLayoutId(){
		return R.layout.online;
	}

	/*
	 * Main logic of the game.
	 *
	 * First, check to see if the user is currently on the game screen. If so, make sure the user is allowed to click.
	 * If the user is allowed to click, process it here.
	 */
	@Override
	public void onClick(View v) {

		if (mCurScreen == R.id.rl) {
			if (isGameOver) return;
			if (turn == 1) return; // not your turn
			if (rollButton.getVisibility() == View.VISIBLE && v.getId() != rollButton.getId()) return;
			if (isRollInProgress || isMoveInProgress || isSendingData) return;

			isSendingData = true;
			frame++;
		}

		if (v.getId() == R.id.button_quick_game) {
			switchToScreen(R.id.screen_wait);
			getVersion();
			verifyVersion();
		}
		else if (v.getId() == R.id.rollButton) { // Called when roll button is clicked
			handleRoll(0);
			broadcastClick(Op.CLICK_ROLL_BUTTON, rollAmount, frame);
		}
		else if (v.getId() == finish.getId()) {
			movePiece(32, Move.NORMAL); // 32 = finish location
			broadcastClick(Op.CLICK_FINISH, -1, frame);
		}
		else if (v.getId() == offBoardPiece.getId()) {  // Image that represents both players' off board pieces
			showPossibleTiles(players[0].findAvailablePiece());
			broadcastClick(Op.CLICK_OFF_BOARD_PIECE, -1, frame);
		}
		else if (tile_ids.contains(v.getId())){    // Activates on tile click
			handleTileClick(v);
			broadcastClick(Op.CLICK_TILE, RIDtoID.get(v.getId()), frame);
		}
		else if (player_ids.contains(v.getId())){  // Activates on animal click; animal covers tile
			handlePlayerClick(v);
			broadcastClick(Op.CLICK_PLAYER, RIDtoID.get(v.getId()), frame);
		}
		else {
			hidePossibleTiles();    // Cancel move by clicking anything else
			broadcastClick(Op.CLICK_EMPTY, -1, frame);
		}
	}

	/*
	 * Start searching for a random opponent.
	 */
	void startQuickGame() {
		// quick-start a game with 1 randomly selected opponent
		final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
		Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
				MAX_OPPONENTS, 0);
		RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
		rtmConfigBuilder.setMessageReceivedListener(this);
		rtmConfigBuilder.setRoomStatusUpdateListener(this);
		rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		keepScreenOn();
		resetGame();
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

	// Activity is going to the background. We have to disconnect.
	@Override
	public void onStop() {
		userPressedLeave = true;
		switchToScreen(R.id.screen_wait);
		disconnect();
		super.onStop();
	}

	protected void endAnimation(){
		if (currentPiece.getLocation() != -1) {
			super.endAnimation();
			if (animationError) leaveRoom();
		}
	}

	// Activity just got to the foreground. We switch to the wait screen because we will now
	// go through the sign-in flow (remember that, yes, every time the Activity comes back to the
	// foreground we go through the sign-in flow -- but if the user is already authenticated,
	// this flow simply succeeds and is imperceptible).
	@Override
	public void onStart() {
		if (!client.isConnected()) {
			switchToScreen(R.id.screen_wait);
			client.connect();
		}
		else {
			switchToScreen(R.id.screen_main);
		}
		super.onStart();
	}

	// Handle back key to make sure we cleanly leave a game if we are in the middle of one
	@Override
	public void onBackPressed() {
		if (mCurScreen == R.id.rl) super.onBackPressed();
		else quit();
	}

	// Leave the room.
	void leaveRoom() {

		disconnect();

		resendCount = 0;
		opponentId = null;

		switchToMainScreen();
	}

	void disconnect() {
		stopKeepingScreenOn();

		try {
			room_id_semaphore.acquire();

			if (mRoomId != null) {
				Games.RealTimeMultiplayer.leave(client, this, mRoomId);
				client.disconnect();    	// Disconnect from Google Play Services
				mRoomId = null;
			}
		} catch (Exception e) {
			Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
		} finally {
			room_id_semaphore.release();
		}
	}

	// Show the waiting room UI to track the progress of other players as they enter the room and get connected.
	void showWaitingRoom(Room room) {
		// Require everyone to join the game before we start it (signaled by Integer.MAX_VALUE).
		final int MIN_PLAYERS = Integer.MAX_VALUE;
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(client, room, MIN_PLAYERS);

		// Launch the default waiting room UI
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
		try {
			room_id_semaphore.acquire();
			if(mRoomId == null) mRoomId = room.getRoomId();
		} catch (Exception e) {
			Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
		} finally {
			room_id_semaphore.release();
		}

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
		try {
			room_id_semaphore.acquire();
			mRoomId = null;
		} catch (Exception e) {
			Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
		} finally {
			room_id_semaphore.release();
		}
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
		try {
			room_id_semaphore.acquire();
			mRoomId = room.getRoomId();
		} catch (Exception e) {
			Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
		} finally {
			room_id_semaphore.release();
		}

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
			if (mCurScreen == R.id.rl) checkRoomStatus();
		}
	}

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

	// Start the game and determine who goes first
	void startGame() {
		checkRoomStatus();

		// Clear previous IDs
		participantIds.clear();

		// Determine who goes first by sorting all the participant ids. Whoever is first goes first.
		for (Participant p : mParticipants) {
			participantIds.add(p.getParticipantId());
		}
		Collections.sort(participantIds);
		if (mMyId.equals(participantIds.get(0))) {
			turn = 0;
			board.setPlayerTurn(0);
			oppTurn = 1;
			rollButton.setVisibility(View.VISIBLE);
			opponentId = participantIds.get(1);
		}
		else {
			turn = 1;
			board.setPlayerTurn(1);
			oppTurn = 0;
			rollButton.setVisibility(View.INVISIBLE);
			opponentId = participantIds.get(0);
		}

		updateTurnText();
		offBoardPiece.setBackgroundResource(avatarIds[turn][1]);
		currentPiece = players[turn].pieces[0];
		currentPieceImage = playerOnBoardImages[turn][0];

		if (turn == 1) {
			bottomBar.setBackgroundResource(R.drawable.bar1);
			bottomBar.setAlpha(1.0f);
			topBar.setBackgroundResource(R.drawable.bar2);
			topBar.setAlpha(0.25f);
		} else {
			topBar.setBackgroundResource(R.drawable.bar1);
			topBar.setAlpha(1.0f);
			bottomBar.setBackgroundResource(R.drawable.bar2);
			bottomBar.setAlpha(0.25f);
		}

		// Set up conversions from intermediate IDs to view IDs
		setupIDs();

		switchToScreen(R.id.rl);
	}

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

	// Called when we receive a real-time message from the network.
	@Override
	public void onRealTimeMessageReceived(RealTimeMessage rtm) {
		if (mCurScreen != R.id.rl) return;

		byte[] buf = rtm.getMessageData();
		String sender = rtm.getSenderParticipantId();

		// Try to verify the sender of the data
		if (!sender.equals(opponentId)) {
			leaveRoom();
			return;
		}

		// Get the data and frame from the message
		byte[] arr1 = { buf[1], buf[2], buf[3], buf[4] };
		byte[] arr2 = { buf[5], buf[6], buf[7], buf[8] };
		ByteBuffer bb1 = ByteBuffer.wrap(arr1); // big-endian by default
		ByteBuffer bb2 = ByteBuffer.wrap(arr2); // big-endian by default
		int data = bb1.getInt();
		int frame = bb2.getInt();

		// Already received this frame
		if (buf[0] != Op.ACK && frame <= this.frame) {
			broadcastClick(Op.ACK, -1, frame);  // Re-send ACK
			return;
		}

		if (buf[0] == Op.CLICK_ROLL_BUTTON) {
			handleRoll(data);
		}
		else if (buf[0] == Op.CLICK_FINISH) {
			movePiece(32, Move.NORMAL); // 32 = finish location
		}
		else if (buf[0] == Op.CLICK_OFF_BOARD_PIECE) {  // Image that represents both players' off board pieces
			showPossibleTiles(players[1].findAvailablePiece());
		}
		else if (buf[0] == Op.CLICK_TILE){    // Activates on tile click
			handleTileClick(findViewById(IDtoRID.get(data)));
		}
		else if (buf[0] == Op.CLICK_PLAYER){  // Activates on animal click; animal covers tile
			handlePlayerClick(findViewById(IDtoRID.get(data)));
		}
		else if (buf[0] == Op.CLICK_EMPTY){
			hidePossibleTiles();    // Cancel move by clicking anything else
		}
		else if (buf[0] == Op.ACK) {
			if (frame == this.frame) {
				try {
					current_frame_semaphore.acquire();
					currentAckFrame = frame;
					isSendingData = false;
					resendCount = 0;
				} catch (Exception e) {
					Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
				} finally {
					current_frame_semaphore.release();
				}
			}
		}
		else {  // Invalid op
			leaveRoom();
			return;
		}

		// If this is not an ACK, tell the other user that we received the message
		if (buf[0] != Op.ACK ) {
			if (frame == this.frame + 1) {          // ACK
				try {
					current_frame_semaphore.acquire();
					this.frame += 1;
					currentAckFrame = frame;
				} catch (Exception e) {
					Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
				} finally {
					current_frame_semaphore.release();
				}

				broadcastClick(Op.ACK, -1, frame);
			}
			else {                                  // Too far out of sync
				leaveRoom();
			}
		}
	}

	// Broadcast my score to everybody else.
	void broadcastClick(byte op, int viewId, int frame) {

		// First byte in message indicates the type of click
		mMsgBuf[0] = op;

		// Bytes 1-4 indicate the viewId (if applicable)
		ByteBuffer b = ByteBuffer.allocate(8);
		b.order(ByteOrder.BIG_ENDIAN);
		b.putInt(viewId);
		b.putInt(frame);
		byte[] bytes = b.array();
		mMsgBuf[1] = bytes[0];
		mMsgBuf[2] = bytes[1];
		mMsgBuf[3] = bytes[2];
		mMsgBuf[4] = bytes[3];
		mMsgBuf[5] = bytes[4];
		mMsgBuf[6] = bytes[5];
		mMsgBuf[7] = bytes[6];
		mMsgBuf[8] = bytes[7];

		// Send to opponent
		for (Participant p : mParticipants) {
			if (p.getParticipantId().equals(mMyId))
				continue;
			if (p.getStatus() != Participant.STATUS_JOINED)
				continue;

			// Send the data
			try {
				Games.RealTimeMultiplayer.sendReliableMessage(client, mReliableMessageSentCallback, mMsgBuf, mRoomId, p.getParticipantId());
			}
			catch (Exception e) {
				leaveRoom();
				return;
			}

			if (op != Op.ACK) {
				waitForACK(frame);
			}
		}
	}

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

	// This array lists all the individual screens our game has.
	final static int[] SCREENS = {
			R.id.screen_main, R.id.screen_wait, R.id.rl
	};

	void switchToScreen(int screenId) {

		if (mCurScreen == R.id.rl && !userPressedLeave) {
			if (mParticipants != null) {
				for (Participant p: mParticipants) {
					if (p.getParticipantId().equals(mMyId))
						continue;
					// Status can be STATUS_LEFT or STATUS_JOINED (doesn't seem to update quick enough)
					if (p.getStatus() == Participant.STATUS_LEFT || p.getStatus() == Participant.STATUS_JOINED) {
						Toast t = Toast.makeText(this, "Opponent has left the game.", Toast.LENGTH_SHORT);
						t.show();
					}
				}
			}
		}

		userPressedLeave = false;

		// make the requested screen visible; hide all others.
		for (int id : SCREENS) {
			findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
		}
		mCurScreen = screenId;

		if (mCurScreen == R.id.rl) checkRoomStatus();
	}

	void switchToMainScreen() {
		if (!client.isConnected()) {
			switchToScreen(R.id.screen_wait);
			client.connect();
		}
		else {
			switchToScreen(R.id.screen_main);
		}
	}

    /*
     * MISC SECTION. Miscellaneous methods.
     */


	// Sets the flag to keep this screen on. It's recommended to do that during
	// the handshake when setting up a game, because if the screen turns off, the
	// game will be cancelled.
	void keepScreenOn() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	// Clears the flag that keeps the screen on.
	void stopKeepingScreenOn() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/*
	 * Called from this activity's layout
	 */
	public void exit(View view) {
		quit();
	}

	protected void resetGame() {

		resetNetworkingVars();

		resetImages();

		resetGameState();
	}

	/*
	 * Create mappings of agreed upon IDs to the IDs of the views
	 */
	protected void setupIDs() {

		// Reset the dictionary of ID conversions
		IDtoRID.clear();
		RIDtoID.clear();

		int ID = 0;
		for(ImageView tile : tiles) {
			IDtoRID.put(ID, tile.getId());
			RIDtoID.put(tile.getId(), ID);
			ID++;
		}

		// Make sure both clients have these view IDs in the same order
		if (turn == 0) {
			for (int i = 0; i < 4; i++) {
				IDtoRID.put(ID, playerOnBoardImages[0][i].getId());
				RIDtoID.put(playerOnBoardImages[0][i].getId(), ID);
				ID++;
			}
			for (int i = 0; i < 4; i++) {
				IDtoRID.put(ID, playerOnBoardImages[1][i].getId());
				RIDtoID.put(playerOnBoardImages[1][i].getId(), ID);
				ID++;
			}
		}
		else {
			for (int i = 0; i < 4; i++) {
				IDtoRID.put(ID, playerOnBoardImages[1][i].getId());
				RIDtoID.put(playerOnBoardImages[1][i].getId(), ID);
				ID++;
			}
			for (int i = 0; i < 4; i++) {
				IDtoRID.put(ID, playerOnBoardImages[0][i].getId());
				RIDtoID.put(playerOnBoardImages[0][i].getId(), ID);
				ID++;
			}
		}
	}

	/*
	 * Verify that the room still has 2 users
	 */
	void checkRoomStatus() {
		if (mParticipants != null) {

			if (mParticipants.size() != 2) {
				leaveRoom();
				return;
			}

			for (Participant p: mParticipants) {
				if (p.getParticipantId().equals(mMyId))
					continue;
				if (p.getStatus() == Participant.STATUS_LEFT) {
					leaveRoom();
					return;
				}
			}
		}
	}

	void getVersion() {

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try  {
					Document doc = Jsoup.connect("https://superjeffreyc.github.io/yut_version").get();
					version = Integer.parseInt(doc.body().text());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();

		// Wait for the thread to finish
		try {
			thread.join();
		}
		catch (InterruptedException e) {
			version = 0;
		}
	}

	void verifyVersion() {
		if (version == 0) {
			Toast t = Toast.makeText(context, "Failed to verify app version. Please make sure you are connected to the Internet and try again.", Toast.LENGTH_SHORT);
			t.show();
			switchToMainScreen();
		}
		else if (version > BuildConfig.VERSION_CODE) {
			AlertDialog.Builder adb = new AlertDialog.Builder(context);
			TextView tv = new TextView(context);
			tv.setPadding(0, 40, 0, 40);
			tv.setText(R.string.update);
			tv.setTextSize(20f);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			adb.setView(tv);
			adb.setPositiveButton("Update", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					quit();
					Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.jeffreychan.yunnori"));
					startActivity(intent);
				}
			});
			adb.setNegativeButton("Later", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					quit();
				}
			});
			adb.show();
		}
		else {
			startQuickGame();            // User wants to play against a random opponent right now
		}
	}

	void waitForACK(final int waitFrame) {
		final int RESEND_DELAY_IN_MILLISECONDS = 500;
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					current_frame_semaphore.acquire();
					if (!hasNetworkError && waitFrame - currentAckFrame == 1) {

						resendCount++;

						if (resendCount > 20) {
							leaveRoom();  // No response from opponent after 20 attempts to resend
						}
						else {
							// Re-send the data
							try {
								Games.RealTimeMultiplayer.sendReliableMessage(client, mReliableMessageSentCallback, mMsgBuf, mRoomId, opponentId);
							}
							catch (Exception e) {
								hasNetworkError = true;
								leaveRoom();
							}

							// Check again in a bit
							handler.postDelayed(this, RESEND_DELAY_IN_MILLISECONDS);
						}
					}
				} catch (Exception e) {
					Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
				} finally {
					current_frame_semaphore.release();
				}
			}
		}, RESEND_DELAY_IN_MILLISECONDS);
	}

	protected void showGameOverDialog() {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		TextView tv = new TextView(this);
		tv.setPadding(0, 40, 0, 40);

		String winner = "\nYou now have " + Shop.Instance.getCoins() + " coin(s)";
		if (players[0].hasWon()) winner = "You win!\n\nYou have earned 1 coin!" + winner;
		else winner = "Opponent wins!\n\nYou still earned 1 coin!" + winner;
		tv.setText(winner);
		tv.setTextSize(20f);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		adb.setView(tv);

		adb.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				userPressedLeave = true;
				leaveRoom();
			}
		});
		adb.setNeutralButton("Rate", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				userPressedLeave = true;
				leaveRoom();
				Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.jeffreychan.yunnori"));
				startActivity(intent);
			}
		});
		adb.show();
	}

	protected void resetNetworkingVars() {
		// Reset frames
		try {
			current_frame_semaphore.acquire();
			frame = 0;
			currentAckFrame = 0;
		} catch (Exception e) {
			Log.e("Yut: OnlineActivity", Log.getStackTraceString(e));
		} finally {
			current_frame_semaphore.release();
		}

		// Clear message buffer
		for (int i = 0; i < 9; i++) {
			mMsgBuf[i] = 0;
		}
	}

	protected void resetGameState() {

		turn = 0;
		oppTurn = 1;

		orderIndex = 0;

		// Reset Board values
		board.reset();

		// Reset Player values and their pieces
		players[0].reset();
		players[1].reset();

		isRollDone = false;
		canRoll = true;
		isEndTurn = false;
		isGameOver = false;
		isMoveInProgress = false;
		isRollInProgress = false;
		isSendingData = false;
		hasNetworkError = false;
		capture = false;
	}

	protected void resetImages() {
		hidePossibleTiles();
		updateOffBoardImages();

		offBoardPiece.setVisibility(View.INVISIBLE);
		offBoardPieceAnimation.stop();
		offBoardPieceAnimation.selectDrawable(0);

		tips.setVisibility(View.INVISIBLE);
		tips.setText(playerTips[turn]);
		offBoardPiece.setBackgroundResource(avatarIds[turn][1]);

		offBoardPieceAnimation = (AnimationDrawable) offBoardPiece.getBackground();

		for (int i = 0; i < board.MAX_ROLLS; i++) {
			rollSlot[i].setBackgroundResource(R.drawable.white_marker);
		}

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				playerAnimation[i][j].stop();
				playerAnimation[i][j] = (AnimationDrawable) playerOnBoardImages[i][j].getBackground();

				players[i].pieces[j].setLocation(-1);
				playerOffBoardImages[i][j].setBackgroundResource(avatarIds[i][0]);

				playerOnBoardImages[i][j].setBackgroundResource(avatarIds[i][1]);
				playerOnBoardImages[i][j].setX(-tiles[0].getX());
				playerOnBoardImages[i][j].clearAnimation();
				playerOnBoardImages[i][j].setVisibility(View.VISIBLE);
			}
		}

	}

	protected void startNextAnimation() {
		super.startNextAnimation();
		if (animationError) leaveRoom();
	}
}
