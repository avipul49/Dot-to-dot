package com.vm.gameplay.gameplay;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.vm.gameplay.GameApplication;
import com.vm.gameplay.GamePlayInterface;
import com.vm.gameplay.MainActivity;
import com.vm.gameplay.R;
import com.vm.gameplay.dialog.DialogCallback;
import com.vm.gameplay.dialog.MessageDialog;
import com.vm.gameplay.model.BoardDimensions;
import com.vm.gameplay.model.Computer;
import com.vm.gameplay.model.GameState;
import com.vm.gameplay.model.Line;
import com.vm.gameplay.model.Player;
import com.vm.gameplay.model.Theme;
import com.vm.gameplay.service.MyService;

@SuppressLint("ClickableViewAccessibility")
public class GameBoardFragment extends Fragment implements
		SurfaceHolder.Callback, DialogCallback, OnTouchListener,
		OnScoreListener {
	private SurfaceView surface;
	private SurfaceHolder holder;
	private BoardDimensions boardDimensions = new BoardDimensions();
	private TextView tvPlayer[] = new TextView[2];
	private TextView tvScore[] = new TextView[2];
	private TextView tvTurn;
	private TextView tvTimer;
	private GameState gameState;
	private GamePlay gamePlay;
	private int score[] = new int[2];
	private TextView tvLeftScore;
	private TextView tvRightScore;
	private Toast notMyTurnToast;
	private TextView notification;
	private GameBoard gameBoard;
	private Intent intent;
	private Context context;
	private View view;
	private ArrayList<Player> players;
	private int me;
	private GamePlayInterface gamePlayInterface;
	private Theme theme;
	private MessageDialog gameOverDialog;
	private boolean singlePlayer;
	private boolean isOnline;
	private boolean quickGame;
	TimerReceiver timerReceiver;
	private boolean leftTheGame = false;

	public GameBoardFragment(Intent intent, Context context,
			ArrayList<Player> players, int me,
			GamePlayInterface gamePlayInterface, Theme theme,
			boolean isSinglePlayer, boolean isOnline, boolean quickGame) {
		this.context = context;
		this.intent = intent;
		this.me = me;
		this.players = players;
		this.gamePlayInterface = gamePlayInterface;
		this.theme = theme;
		this.singlePlayer = isSinglePlayer;
		this.isOnline = isOnline;
		this.quickGame = quickGame;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.game_play_view, container, false);
		init();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MyService.TICK);
		filter.addAction(MyService.TIMEOUT);
		filter.addAction(MyService.LAST_TIMEOUT);
		if (timerReceiver == null) {
			timerReceiver = new TimerReceiver();
			context.registerReceiver(timerReceiver, filter);
		}
		return view;
	}

	@SuppressLint("NewApi")
	public void init() {
		initUIElements();
		initGameState(singlePlayer, isOnline, me);
		setupScoreTextViews(isOnline);
		holder = surface.getHolder();
		holder.addCallback(this);
		surface.setOnTouchListener(this);

	}

	private void setupScoreTextViews(boolean bluetooth) {
		setupPlayerTextViews(0, gameState.getPlayer(0));
		tvLeftScore.setBackgroundColor(gameState.getPlayer(0).getColor());
		int color = Color.BLACK;
		String name = "Waiting";
		tvRightScore.setBackgroundColor(gameState.getPlayer(1).getColor());
		color = gameState.getPlayer(1).getColor();
		name = gameState.getPlayer(1).getName();
		setupPlayerTextViews(1, color, name);
	}

	private void resetTimer() {
		Intent intent = new Intent();
		intent.setAction(MyService.START);
		context.sendBroadcast(intent);
		tvTimer.setTextColor(Color.BLACK);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (!gameState.isStart()) {
			Toast.makeText(context, "Waiting for user to join",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!gameState.isMyTurn()) {
			if (notMyTurnToast == null)
				notMyTurnToast = Toast
						.makeText(context, "", Toast.LENGTH_SHORT);
			notMyTurnToast.setText(String.format("Wait for %s's turn",
					gameState.getCurrentPlayer().getName()));
			notMyTurnToast.show();
			return false;
		}
		if (gameState.isStart()
				&& boardDimensions.checkFrame(event.getX(), event.getY())) {
			Line line = gamePlay.getSelectedLine(event.getX(), event.getY());
			move(line);
			if (gameState.isBluetooth()) {
				String out = "1::"
						+ (line.getStart().x - boardDimensions.getxOffset())
						/ boardDimensions.getCellWidth() + "::"
						+ (line.getStart().y - boardDimensions.getyOffset())
						/ boardDimensions.getCellWidth() + "::"
						+ (line.getEnd().x - boardDimensions.getxOffset())
						/ boardDimensions.getCellWidth() + "::"
						+ (line.getEnd().y - boardDimensions.getyOffset())
						/ boardDimensions.getCellWidth();
				gamePlayInterface.sendMove(out);
			}
		}
		return false;
	}

	private void setupPlayerTextViews(int index, Player player) {
		setupPlayerTextViews(index, player.getColor(), player.getName());
	}

	private void setupPlayerTextViews(int index, int color, String name) {
		tvPlayer[index].setBackgroundColor(color);
		tvPlayer[index].setText(name);
		tvScore[index].setBackgroundColor(color);
	}

	private void initGameState(boolean singlePlayer, boolean isOnline, int me) {
		gameState = new GameState(
				((GameApplication) context.getApplicationContext()).getThemes());
		gameState.setThemeIndex(intent.getIntExtra("theme", 0));
		gameState.configureTheme(theme);
		gameState.setPlayers(players);
		gameState.configurePlayers(singlePlayer, isOnline);
		gameState.setMe(me);
		tvTurn.setText(String.format("%s's turn", gameState.getCurrentPlayer()
				.getName()));
		if (!gameState.isBoardCreated()) {
			if (quickGame)
				gameState.createQuickBoard();
			else {
				gameState.createBoard();
				//gamePlayInterface.sendMove(gameState.getStartGameMessage());
			}
		}
	}

	private void initUIElements() {
		tvScore[0] = (TextView) view.findViewById(R.id.score1);
		tvScore[1] = (TextView) view.findViewById(R.id.score2);
		tvPlayer[0] = (TextView) view.findViewById(R.id.player1);
		tvPlayer[1] = (TextView) view.findViewById(R.id.player2);
		surface = (SurfaceView) view.findViewById(R.id.mysurface);
		tvTimer = (TextView) view.findViewById(R.id.timer);
		tvTurn = (TextView) view.findViewById(R.id.turn);
		tvLeftScore = (TextView) view.findViewById(R.id.left_score);
		tvRightScore = (TextView) view.findViewById(R.id.right_score);
		notification = (TextView) view.findViewById(R.id.notification);
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MainActivity.STR_MESSAGE_READ);
		context.registerReceiver(receiver, intentFilter);
		// if (timeRemaining != -1)
		// resetTimer(timeRemaining);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void onLeave() {
		Intent intent1 = new Intent();
		intent1.setAction(MyService.LAST_TIMEOUT);
		context.removeStickyBroadcast(intent1);
		context.unregisterReceiver(receiver);
		leftTheGame = true;
	}

	private void move(Line line) {
		if (line != null) {
			gameBoard.setLastMoveColor(gameState.getCurrentPlayer().getColor());
			if (gameBoard.isValidMove(line)) {
				resetTimer();
				if (gamePlay.drawLine(line))
					changePlayer();
				gameBoard.drawBoard();
			}
			moveComputer();
		}
	}

	private void moveComputer() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (GameBoardFragment.this.isVisible()) {
					if (gameState.isComputersTern()) {
						Line computerLine = ((Computer) gameState.getPlayers()
								.get(1)).getNextMove();
						move(computerLine);
					}
				}
			}
		}, 1000);
	}

	private void changePlayer() {
		gameState.changePlayer();
		tvTurn.setText(String.format("%s's turn", gameState.getCurrentPlayer()
				.getName()));
	}

	private void undo() {
		gamePlay.undo();
		gameBoard.drawBoard();
		changePlayer();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (boardDimensions.getHeight() == 0) {
			adjustHeightWidth();
			gameState.getTheme().createDrawables(getResources(),
					boardDimensions.getHeight(), boardDimensions.getWidth(),
					boardDimensions.getCellWidth());
			gameBoard = new GameBoard(context, surface, gamePlay, gameState,
					boardDimensions);
			restart();
		} else {
			gameBoard.drawBoard();
		}
	}

	private void adjustHeightWidth() {
		boardDimensions.setHeight(surface.getMeasuredHeight());
		boardDimensions.setWidth(surface.getMeasuredWidth());
		boardDimensions.setCellWidth(Math.min(boardDimensions.getWidth()
				/ (gameState.getTheme().getCol() + 1),
				boardDimensions.getHeight()
						/ (gameState.getTheme().getRow() + 1)));
		boardDimensions.setxOffset(gameState.getTheme().getRow() + 1);
		boardDimensions.setyOffset(gameState.getTheme().getCol() + 1);
		gamePlay = new GamePlay(boardDimensions, gameState, this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public void restart() {

		if (gameOverDialog != null && gameOverDialog.isShowing()) {
			gameOverDialog.dismiss();
		}
		if (gameState.isSinglePlayer())
			((Computer) gameState.getPlayers().get(1))
					.setupMoves(boardDimensions);
		gamePlay.clear();
		tvScore[0].setText("00");
		if (gameState.isStart()) {
			tvScore[1].setText("00");
			resetTimer();
		}
		gameState.setPlayer(0);
		gameBoard.drawBoard();
	}

	@Override
	public void onScoreUpdate(int curScore, int marked, int points) {
		score[gameState.getPlayer()] = curScore;
		tvScore[gameState.getPlayer()].setText(String.format("%02d", curScore));
		if (gameState.isGameOver(marked)) {
			if (gameState.isSinglePlayer())
				gamePlayInterface.gameOver(score[0] - score[1]);
			notification.setText("Game Over");
			startGameOverAnimation(notification);
			// displayGameOverMessage(false);
		} else {
			notification.setText(String.format("%+d", points));
			startScoreAnimation(notification);
		}
	}

	private void startGameOverAnimation(final View view) {
		AnimationSet animation = getAnimationSet(2000);
		view.setAnimation(animation);
		view.startAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
				displayGameOverMessage(false);
			}
		});
	}

	private void startScoreAnimation(final View view) {
		int duration = 1000;
		AnimationSet animation = getAnimationSet(duration);
		view.setAnimation(animation);
		view.startAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
	}

	private AnimationSet getAnimationSet(int duration) {
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
		fadeIn.setDuration(duration);

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
		fadeOut.setStartOffset(duration);
		fadeOut.setDuration(duration);

		AnimationSet animation = new AnimationSet(false); // change to false
		animation.addAnimation(fadeIn);
		animation.addAnimation(fadeOut);
		return animation;
	}

	private void displayGameOverMessage(boolean timeout) {
		if (gameOverDialog != null && gameOverDialog.isShowing())
			gameOverDialog.dismiss();
		gameOverDialog = new MessageDialog(context, 1);
		gameOverDialog.setCallback(this);
		gameOverDialog.setScores(timeout, gameState.getPlayer(), gameState
				.getPlayers(), score, new int[] { gamePlay.getRects1().size(),
				gamePlay.getRects2().size() }, gameState.getTheme().getGems());
		gameOverDialog.show();
	}

	@Override
	public void onDialogAction(int dialogId, Action action) {
		switch (action) {
		case LEAVE:
			gamePlayInterface.leaveRoom();
			break;
		case RESTART:
			gamePlayInterface.sendMove("3::1");
			restart();
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onTick(long millisUntilFinished) {

		long sec = (long) Math.floor(millisUntilFinished / 1000);
		// Log.i("", "------" + sec);
		tvTimer.setText(String.format("%02d:%02d", sec / 60, sec % 60));
		if (millisUntilFinished < 10500) {
			AnimationSet animationSet = getAnimationSet(100);
			tvTimer.startAnimation(animationSet);
			tvTimer.setTextColor(Color.RED);
			tvTimer.setTypeface(Typeface.DEFAULT_BOLD);
			if (millisUntilFinished > 9500) {
				startScoreAnimation(view.findViewById(R.id.notificationImage));
			}
		}
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		public void onReceive(android.content.Context context, Intent intent) {
			byte[] readBuf = (byte[]) intent.getByteArrayExtra("data");
			String readMessage = new String(readBuf);
			String[] message = readMessage.split("::");
			switch (Integer.parseInt(message[0])) {
			case 0:

				break;
			case 1:
				Point start = new Point(Integer.parseInt(message[1])
						* boardDimensions.getCellWidth()
						+ boardDimensions.getxOffset(),
						Integer.parseInt(message[2])
								* boardDimensions.getCellWidth()
								+ boardDimensions.getyOffset());
				Point end = new Point(Integer.parseInt(message[3])
						* boardDimensions.getCellWidth()
						+ boardDimensions.getxOffset(),
						Integer.parseInt(message[4])
								* boardDimensions.getCellWidth()
								+ boardDimensions.getyOffset());
				Line line = new Line(start, end);
				move(line);
				break;
			case 2:
				undo();
				break;
			case 3:
				restart();
				break;
			default:
				break;
			}

		}

	};

	class TimerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!leftTheGame) {
				String action = intent.getAction();
				if (action.equals(MyService.LAST_TIMEOUT)) {
					Intent intent1 = new Intent();
					intent1.setAction(MyService.LAST_TIMEOUT);
					context.removeStickyBroadcast(intent1);
					displayGameOverMessage(true);
					context.unregisterReceiver(timerReceiver);
				} else if (action.equals(MyService.TICK)) {
					onTick(intent.getLongExtra("millisUntilFinished", 0));
				} else if (action.equals(MyService.TIMEOUT)) {
					tvTimer.setText("00:00");
					tvTimer.setTextColor(Color.BLACK);
					gamePlay.timePenalty(-5);
				}
			}
		}

	}
}