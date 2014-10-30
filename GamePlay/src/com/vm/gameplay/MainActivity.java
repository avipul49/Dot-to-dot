package com.vm.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.vm.gameplay.gameplay.GameBoardFragment;
import com.vm.gameplay.logging.LogginUtil;
import com.vm.gameplay.model.Player;
import com.vm.gameplay.model.Theme;

public class MainActivity extends BaseGameActivity implements
		View.OnClickListener, RealTimeMessageReceivedListener,
		RoomStatusUpdateListener, RoomUpdateListener,
		OnInvitationReceivedListener, GamePlayInterface {

	private static final String LEADERBOARD_ID = "CgkI1permasHEAIQCQ";

	private static final int SECOND_PLAYER_COLOUR = Color.parseColor("#FEE401");

	private static final int FIRST_PLAYER_COLOUR = Color.parseColor("#AFE90C");
	/*
	 * API INTEGRATION SECTION. This section contains the code that integrates
	 * the game with the Google Play game services API.
	 */

	// Debug tag
	final static boolean ENABLE_DEBUG = true;
	final static String TAG = "ButtonClicker2000";

	// Request codes for the UIs that we show with startActivityForResult:
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_INVITATION_INBOX = 10001;
	final static int RC_WAITING_ROOM = 10002;

	private static final int REQUEST_LEADERBOARD = 10003;

	// Room ID where the currently active game is taking place; null if we're
	// not playing.
	String mRoomId = null;

	// Are we playing in multiplayer mode?
	boolean mMultiplayer = false;

	// The participants in the currently active game
	ArrayList<Participant> mParticipants = null;

	// My participant ID in the currently active game
	String mMyId = null;

	// If non-null, this is the id of the invitation we received via the
	// invitation listener
	String mIncomingInvitationId = null;

	// Message buffer for sending messages
	byte[] mMsgBuf = new byte[2];
	private Room mRoom;
	private String mCreatorId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		enableDebugLog(ENABLE_DEBUG, TAG);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);

		// set up a click listener for everything we care about
		for (int id : CLICKABLES) {
			findViewById(id).setOnClickListener(this);
		}
	}

	/**
	 * Called by the base class (BaseGameActivity) when sign-in has failed. For
	 * example, because the user hasn't authenticated yet. We react to this by
	 * showing the sign-in button.
	 */
	@Override
	public void onSignInFailed() {
		Log.d(TAG, "Sign-in failed.");
		switchToScreen(R.id.screen_sign_in);
	}

	/**
	 * Called by the base class (BaseGameActivity) when sign-in succeeded. We
	 * react by going to our main screen.
	 */
	@Override
	public void onSignInSucceeded() {
		Log.d(TAG, "Sign-in succeeded.");

		// register listener so we are notified if we receive an invitation to
		// play
		// while we are in the game
		Games.Invitations.registerInvitationListener(getApiClient(), this);

		// if we received an invite via notification, accept it; otherwise, go
		// to main screen
		if (getInvitationId() != null) {
			acceptInviteToRoom(getInvitationId());
			return;
		}
		switchToMainScreen();
	}

	@Override
	public void onClick(View v) {
		Intent intent;

		switch (v.getId()) {
		case R.id.local:
			// MessageDialog md = new MessageDialog(this, 0);
			// md.show();
			// intent = new Intent(this, GameSetupActivity.class);
			// startActivity(intent);
			// LogginUtil.logEvent(this, "User action", "Game selected",
			// "Multiplayer local game", 0);
			// break;
		case R.id.button_single_player:
		case R.id.button_single_player_2:
			LogginUtil.logEvent(this, "User action", "Game selected",
					"Single player game", 0);
			ArrayList<Player> players = new ArrayList<Player>();
			com.google.android.gms.games.Player player = Games.Players
					.getCurrentPlayer(getApiClient());
			Player gpPLayer = new Player(player.getDisplayName(),
					FIRST_PLAYER_COLOUR);
			players.add(gpPLayer);
			startGamePlay(null, players, 0, true);
			break;

		case R.id.button_sign_in:
			// user wants to sign in
			if (!verifyPlaceholderIdsReplaced()) {
				showAlert("Error: sample not set up correctly. Please see README.");
				return;
			}
			beginUserInitiatedSignIn();
			break;
		case R.id.button_sign_out:
			// user wants to sign out
			signOut();
			switchToScreen(R.id.screen_sign_in);
			break;
		case R.id.button_invite_players:
			// show list of invitable players
			intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(
					getApiClient(), 1, 1);
			switchToScreen(R.id.screen_wait);
			startActivityForResult(intent, RC_SELECT_PLAYERS);
			break;
		case R.id.button_see_invitations:
			// show list of pending invitations
			intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
			switchToScreen(R.id.screen_wait);
			startActivityForResult(intent, RC_INVITATION_INBOX);
			break;
		case R.id.button_accept_popup_invitation:
			// user wants to accept the invitation shown on the invitation popup
			// (the one we got through the OnInvitationReceivedListener).
			acceptInviteToRoom(mIncomingInvitationId);
			mIncomingInvitationId = null;
			break;
		case R.id.button_quick_game:
			// user wants to play against a random opponent right now
			startQuickGame();
			break;

		}
	}

	void startQuickGame() {
		// quick-start a game with 1 randomly selected opponent
		final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
		Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
				MIN_OPPONENTS, MAX_OPPONENTS, 0);
		RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
		rtmConfigBuilder.setMessageReceivedListener(this);
		rtmConfigBuilder.setRoomStatusUpdateListener(this);
		rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		switchToScreen(R.id.screen_wait);
		keepScreenOn();
		resetGameVars();
		Games.RealTimeMultiplayer.create(getApiClient(),
				rtmConfigBuilder.build());
	}

	@Override
	public void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		super.onActivityResult(requestCode, responseCode, intent);

		switch (requestCode) {
		case RC_SELECT_PLAYERS:
			// we got the result from the "select players" UI -- ready to create
			// the room
			handleSelectPlayersResult(responseCode, intent);
			break;
		case RC_INVITATION_INBOX:
			// we got the result from the "select invitation" UI (invitation
			// inbox). We're
			// ready to accept the selected invitation:
			handleInvitationInboxResult(responseCode, intent);
			break;
		case RC_WAITING_ROOM:
			// we got the result from the "waiting room" UI.
			if (responseCode == Activity.RESULT_OK) {
				// ready to start playing
				Log.d(TAG, "Starting game (waiting room returned OK).");
				startGame(true);
			} else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
				// player indicated that they want to leave the room
				leaveRoom();
			} else if (responseCode == Activity.RESULT_CANCELED) {
				// Dialog was cancelled (user pressed back key, for instance).
				// In our game,
				// this means leaving the room too. In more elaborate games,
				// this could mean
				// something else (like minimizing the waiting room UI).
				leaveRoom();
			}
			break;
		case REQUEST_LEADERBOARD:
			break;
		}
	}

	// Handle the result of the "Select players UI" we launched when the user
	// clicked the
	// "Invite friends" button. We react by creating a room with those players.
	private void handleSelectPlayersResult(int response, Intent data) {
		if (response != Activity.RESULT_OK) {
			Log.w(TAG, "*** select players UI cancelled, " + response);
			switchToMainScreen();
			return;
		}

		Log.d(TAG, "Select players UI succeeded.");

		// get the invitee list
		final ArrayList<String> invitees = data
				.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
		Log.d(TAG, "Invitee count: " + invitees.size());

		// get the automatch criteria
		Bundle autoMatchCriteria = null;
		int minAutoMatchPlayers = data.getIntExtra(
				Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
		int maxAutoMatchPlayers = data.getIntExtra(
				Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
		if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
					minAutoMatchPlayers, maxAutoMatchPlayers, 0);
			Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
		}

		// create the room
		Log.d(TAG, "Creating room...");
		RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
		rtmConfigBuilder.addPlayersToInvite(invitees);
		rtmConfigBuilder.setMessageReceivedListener(this);
		rtmConfigBuilder.setRoomStatusUpdateListener(this);
		if (autoMatchCriteria != null) {
			rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		}
		switchToScreen(R.id.screen_wait);
		keepScreenOn();
		resetGameVars();
		Games.RealTimeMultiplayer.create(getApiClient(),
				rtmConfigBuilder.build());
		Log.d(TAG, "Room created, waiting for it to be ready...");
	}

	// Handle the result of the invitation inbox UI, where the player can pick
	// an invitation
	// to accept. We react by accepting the selected invitation, if any.
	private void handleInvitationInboxResult(int response, Intent data) {
		if (response != Activity.RESULT_OK) {
			Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
			switchToMainScreen();
			return;
		}

		Log.d(TAG, "Invitation inbox UI succeeded.");
		Invitation inv = data.getExtras().getParcelable(
				Multiplayer.EXTRA_INVITATION);

		// accept invitation
		acceptInviteToRoom(inv.getInvitationId());
	}

	// Accept the given invitation.
	void acceptInviteToRoom(String invId) {
		// accept the invitation
		Log.d(TAG, "Accepting invitation: " + invId);
		RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
		roomConfigBuilder.setInvitationIdToAccept(invId)
				.setMessageReceivedListener(this)
				.setRoomStatusUpdateListener(this);
		switchToScreen(R.id.screen_wait);
		keepScreenOn();
		resetGameVars();
		Games.RealTimeMultiplayer.join(getApiClient(),
				roomConfigBuilder.build());
	}

	// Activity is going to the background. We have to leave the current room.
	@Override
	public void onStop() {
		Log.d(TAG, "**** got onStop");

		// if we're in a room, leave it.
		leaveRoom();

		// stop trying to keep the screen on
		stopKeepingScreenOn();

		switchToScreen(R.id.screen_wait);
		super.onStop();
	}

	// Activity just got to the foreground. We switch to the wait screen because
	// we will now
	// go through the sign-in flow (remember that, yes, every time the Activity
	// comes back to the
	// foreground we go through the sign-in flow -- but if the user is already
	// authenticated,
	// this flow simply succeeds and is imperceptible).
	@Override
	public void onStart() {
		switchToScreen(R.id.screen_wait);
		super.onStart();
	}

	// Handle back key to make sure we cleanly leave a game if we are in the
	// middle of one
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
			leaveRoom();
			return true;
		}
		return super.onKeyDown(keyCode, e);
	}

	// Leave the room.
	@Override
	public void leaveRoom() {
		Log.d(TAG, "Leaving room.");
		mSecondsLeft = 0;
		stopKeepingScreenOn();
		if (mRoomId != null) {
			Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
			mRoomId = null;
			switchToScreen(R.id.screen_wait);
		} else {
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
					getApiClient(), LEADERBOARD_ID), REQUEST_LEADERBOARD);
			switchToMainScreen();
		}
	}

	// Show the waiting room UI to track the progress of other players as they
	// enter the
	// room and get connected.
	void showWaitingRoom(Room room) {
		// minimum number of players required for our game
		// For simplicity, we require everyone to join the game before we start
		// it
		// (this is signaled by Integer.MAX_VALUE).
		final int MIN_PLAYERS = Integer.MAX_VALUE;
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(
				getApiClient(), room, MIN_PLAYERS);

		// show waiting room UI
		startActivityForResult(i, RC_WAITING_ROOM);
	}

	// Called when we get an invitation to play a game. We react by showing that
	// to the user.
	@Override
	public void onInvitationReceived(Invitation invitation) {
		// We got an invitation to play a game! So, store it in
		// mIncomingInvitationId
		// and show the popup on the screen.
		mIncomingInvitationId = invitation.getInvitationId();
		((TextView) findViewById(R.id.incoming_invitation_text))
				.setText(invitation.getInviter().getDisplayName() + " "
						+ getString(R.string.is_inviting_you));
		switchToScreen(mCurScreen); // This will show the invitation popup
	}

	@Override
	public void onInvitationRemoved(String invitationId) {
		if (mIncomingInvitationId.equals(invitationId)) {
			mIncomingInvitationId = null;
			switchToScreen(mCurScreen); // This will hide the invitation popup
		}
	}

	/*
	 * CALLBACKS SECTION. This section shows how we implement the several games
	 * API callbacks.
	 */

	// Called when we are connected to the room. We're not ready to play yet!
	// (maybe not everybody
	// is connected yet).
	@Override
	public void onConnectedToRoom(Room room) {
		Log.d(TAG, "onConnectedToRoom.");

		// get room ID, participants and my ID:
		mRoomId = room.getRoomId();
		mParticipants = room.getParticipants();
		mMyId = room.getParticipantId(Games.Players
				.getCurrentPlayerId(getApiClient()));
		mCreatorId = room.getCreatorId();
		mRoom = room;
		// print out the list of participants (for debug purposes)
		Log.d(TAG, "Room ID: " + mRoomId);
		Log.d(TAG, "My ID " + mMyId);
		Log.d(TAG, "<< CONNECTED TO ROOM>>");
	}

	// Called when we've successfully left the room (this happens a result of
	// voluntarily leaving
	// via a call to leaveRoom(). If we get disconnected, we get
	// onDisconnectedFromRoom()).
	@Override
	public void onLeftRoom(int statusCode, String roomId) {
		// we have left the room; return to main screen.
		Log.d(TAG, "onLeftRoom, code " + statusCode);
		switchToMainScreen();
	}

	// Called when we get disconnected from the room. We return to the main
	// screen.
	@Override
	public void onDisconnectedFromRoom(Room room) {
		mRoomId = null;
		showGameError();
	}

	// Show error message about game being cancelled and return to main screen.
	void showGameError() {
		showAlert(getString(R.string.game_problem));
		switchToMainScreen();
	}

	// Called when room has been created
	@Override
	public void onRoomCreated(int statusCode, Room room) {
		Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
			showGameError();
			return;
		}

		// show the waiting room UI
		showWaitingRoom(room);
	}

	// Called when room is fully connected.
	@Override
	public void onRoomConnected(int statusCode, Room room) {
		Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
			showGameError();
			return;
		}
		updateRoom(room);
	}

	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
			showGameError();
			return;
		}

		// show the waiting room UI
		showWaitingRoom(room);
	}

	// We treat most of the room update callbacks in the same way: we update our
	// list of
	// participants and update the display. In a real game we would also have to
	// check if that
	// change requires some action like removing the corresponding player avatar
	// from the screen,
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
		if (mParticipants != null) {
			updatePeerScoresDisplay();
		}
	}

	/*
	 * GAME LOGIC SECTION. Methods that implement the game's rules.
	 */

	// Current state of the game:
	int mSecondsLeft = -1; // how long until the game ends (seconds)
	final static int GAME_DURATION = 20; // game duration, seconds.
	int mScore = 0; // user's current score
	private GameBoardFragment mFragment;

	// Reset game variables in preparation for a new game.
	void resetGameVars() {
		mSecondsLeft = GAME_DURATION;
		mScore = 0;
		mParticipantScore.clear();
		mFinishedParticipants.clear();
	}

	// Start the gameplay phase of the game.
	void startGame(boolean multiplayer) {
		mMultiplayer = multiplayer;
		updateScoreDisplay();
		broadcastScore(false);
		// switchToScreen(R.id.screen_game);
		ArrayList<Player> players = new ArrayList<Player>();
		int me = 1;
		if (mCreatorId.equals(mMyId)) {
			me = 0;
			startGamePlay(null, players, me, false);
		}
		// run the gameTick() method every second to update the game.
		// final Handler h = new Handler();
		// h.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// if (mSecondsLeft <= 0)
		// return;
		// gameTick();
		// h.postDelayed(this, 1000);
		// }
		// }, 1000);
	}

	// indicates the player scored one point
	void scoreOnePoint() {
		if (mSecondsLeft <= 0)
			return; // too late!
		++mScore;
		updateScoreDisplay();
		updatePeerScoresDisplay();

		// broadcast our new score to our peers
		broadcastScore(false);
	}

	/*
	 * COMMUNICATIONS SECTION. Methods that implement the game's network
	 * protocol.
	 */

	// Score of other participants. We update this as we receive their scores
	// from the network.
	Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

	// Participants who sent us their final score.
	Set<String> mFinishedParticipants = new HashSet<String>();

	// Called when we receive a real-time message from the network.
	// Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
	// indicating
	// whether it's a final or interim score. The second byte is the score.
	// There is also the
	// 'S' message, which indicates that the game should start.
	@Override
	public void onRealTimeMessageReceived(RealTimeMessage rtm) {

		byte[] buf = rtm.getMessageData();
		String sender = rtm.getSenderParticipantId();
		Theme theme = new Theme("");
		String bMessage = new String(buf);
		System.out.println();
		String[] message = bMessage.split("::");
		switch (Integer.parseInt(message[0])) {
		case 0:
			ArrayList<Player> players = new ArrayList<Player>();
			if (message.length >= 5) {
				theme.setup(message[1]);
				theme.setRow(Integer.parseInt(message[2]));
				theme.setCol(Integer.parseInt(message[3]));
				theme.setId(Integer.parseInt(message[4]));
			}
			int me = 1;
			boolean singlePlayer = false;
			startGamePlay(theme.getBoard(), players, me, singlePlayer);
			break;
		case 1:
			Intent intent = new Intent();
			intent.setAction(FindGameActivity.STR_MESSAGE_READ);
			intent.putExtra("data", buf);
			this.sendBroadcast(intent);
			break;
		default:
			break;
		}
		// Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int)
		// buf[1]);
		//
		// if (buf[0] == 'F' || buf[0] == 'U') {
		// // score update.
		// int existingScore = mParticipantScore.containsKey(sender) ?
		// mParticipantScore
		// .get(sender) : 0;
		// int thisScore = (int) buf[1];
		// if (thisScore > existingScore) {
		// // this check is necessary because packets may arrive out of
		// // order, so we
		// // should only ever consider the highest score we received, as
		// // we know in our
		// // game there is no way to lose points. If there was a way to
		// // lose points,
		// // we'd have to add a "serial number" to the packet.
		// mParticipantScore.put(sender, thisScore);
		// }
		//
		// // update the scores on the screen
		// updatePeerScoresDisplay();
		//
		// // if it's a final score, mark this participant as having finished
		// // the game
		// if ((char) buf[0] == 'F') {
		// mFinishedParticipants.add(rtm.getSenderParticipantId());
		// }
		// }
	}

	private void startGamePlay(int[] board, ArrayList<Player> players, int me,
			boolean singlePlayer) {
		switchToScreen(R.id.screen_game);
		if (!singlePlayer)
			for (Participant participant : mRoom.getParticipants()) {

				if (participant.getParticipantId().equals(mCreatorId)) {
					Player player = new Player(participant.getDisplayName(),
							FIRST_PLAYER_COLOUR);
					players.add(0, player);
				} else {
					Player player = new Player(participant.getDisplayName(),
							SECOND_PLAYER_COLOUR);
					players.add(player);
				}
			}
		mFragment = new GameBoardFragment(new Intent(), this, players, me,
				this, board, singlePlayer);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.screen_game, mFragment).commit();
	}

	// Broadcast my score to everybody else.
	public void broadcastScore(boolean finalScore) {
		if (!mMultiplayer)
			return; // playing single-player mode

		// First byte in message indicates whether it's a final score or not
		mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

		// Second byte is the score.
		mMsgBuf[1] = (byte) mScore;

		// Send to every other participant.
		for (Participant p : mParticipants) {
			if (p.getParticipantId().equals(mMyId))
				continue;
			if (p.getStatus() != Participant.STATUS_JOINED)
				continue;
			// if (finalScore) {
			// final score notification must be sent via reliable message

			// } else {
			// // it's an interim score notification, so we can use unreliable
			// Games.RealTimeMultiplayer.sendUnreliableMessage(getApiClient(),
			// mMsgBuf, mRoomId, p.getParticipantId());
			// }
		}
	}

	/*
	 * UI SECTION. Methods that implement the game's UI.
	 */

	// This array lists everything that's clickable, so we can install click
	// event handlers.
	final static int[] CLICKABLES = { R.id.button_accept_popup_invitation,
			R.id.button_invite_players, R.id.button_quick_game,
			R.id.button_see_invitations, R.id.button_sign_in,
			R.id.button_sign_out, R.id.button_single_player,
			R.id.button_single_player_2, R.id.local };

	// This array lists all the individual screens our game has.
	final static int[] SCREENS = { R.id.screen_game, R.id.screen_main,
			R.id.screen_sign_in, R.id.screen_wait };
	int mCurScreen = -1;

	void switchToScreen(int screenId) {
		// make the requested screen visible; hide all others.
		for (int id : SCREENS) {
			findViewById(id).setVisibility(
					screenId == id ? View.VISIBLE : View.GONE);
		}
		mCurScreen = screenId;

		// should we show the invitation popup?
		boolean showInvPopup;
		if (mIncomingInvitationId == null) {
			// no invitation, so no popup
			showInvPopup = false;
		} else if (mMultiplayer) {
			// if in multiplayer, only show invitation on main screen
			showInvPopup = (mCurScreen == R.id.screen_main);
		} else {
			// single-player: show on main screen and gameplay screen
			showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
		}
		findViewById(R.id.invitation_popup).setVisibility(
				showInvPopup ? View.VISIBLE : View.GONE);
	}

	void switchToMainScreen() {
		switchToScreen(isSignedIn() ? R.id.screen_main : R.id.screen_sign_in);
	}

	// updates the label that shows my score
	void updateScoreDisplay() {
	}

	// formats a score as a three-digit number
	String formatScore(int i) {
		if (i < 0)
			i = 0;
		String s = String.valueOf(i);
		return s.length() == 1 ? "00" + s : s.length() == 2 ? "0" + s : s;
	}

	// updates the screen with the scores from our peers
	void updatePeerScoresDisplay() {
		// ((TextView) findViewById(R.id.score0)).setText(formatScore(mScore)
		// + " - Me");
		// int[] arr = { R.id.score1, R.id.score2, R.id.score3 };
		// int i = 0;
		//
		// if (mRoomId != null) {
		// for (Participant p : mParticipants) {
		// String pid = p.getParticipantId();
		// if (pid.equals(mMyId))
		// continue;
		// if (p.getStatus() != Participant.STATUS_JOINED)
		// continue;
		// int score = mParticipantScore.containsKey(pid) ? mParticipantScore
		// .get(pid) : 0;
		// ((TextView) findViewById(arr[i])).setText(formatScore(score)
		// + " - " + p.getDisplayName());
		// ++i;
		// }
		// }
		//
		// for (; i < arr.length; ++i) {
		// ((TextView) findViewById(arr[i])).setText("");
		// }
	}

	/*
	 * MISC SECTION. Miscellaneous methods.
	 */

	/**
	 * Checks that the developer (that's you!) read the instructions. IMPORTANT:
	 * a method like this SHOULD NOT EXIST in your production app! It merely
	 * exists here to check that anyone running THIS PARTICULAR SAMPLE did what
	 * they were supposed to in order for the sample to work.
	 */
	boolean verifyPlaceholderIdsReplaced() {
		final boolean CHECK_PKGNAME = true; // set to false to disable check
											// (not recommended!)

		// Did the developer forget to change the package name?
		if (CHECK_PKGNAME && getPackageName().startsWith("com.google.example.")) {
			Log.e(TAG,
					"*** Sample setup problem: "
							+ "package name cannot be com.google.example.*. Use your own "
							+ "package name.");
			return false;
		}

		// Did the developer forget to replace a placeholder ID?
		int res_ids[] = new int[] { R.string.app_id };
		for (int i : res_ids) {
			if (getString(i).equalsIgnoreCase("ReplaceMe")) {
				Log.e(TAG,
						"*** Sample setup problem: You must replace all "
								+ "placeholder IDs in the ids.xml file by your project's IDs.");
				return false;
			}
		}
		return true;
	}

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

	@Override
	public void sendMove(String message) {
		if (mParticipants != null) {
			for (Participant p : mParticipants) {
				if (p.getParticipantId().equals(mMyId))
					continue;
				if (p.getStatus() != Participant.STATUS_JOINED)
					continue;
				Games.RealTimeMultiplayer
						.sendReliableMessage(getApiClient(), null,
								message.getBytes(), mRoomId,
								p.getParticipantId());
			}
		}
	}

	@Override
	public void gameOver(int score) {
		Games.Leaderboards.submitScore(getApiClient(), LEADERBOARD_ID, score);
	}
}
