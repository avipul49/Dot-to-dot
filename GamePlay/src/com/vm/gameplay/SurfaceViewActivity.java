package com.vm.gameplay;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.vm.gameplay.animation.AnimationCallback;
import com.vm.gameplay.animation.ScoreUpdateAnimation;
import com.vm.gameplay.dialog.DialogCallback;
import com.vm.gameplay.dialog.MessageDialog;
import com.vm.gameplay.gameplay.GamePlay;
import com.vm.gameplay.gameplay.OnScoreListener;
import com.vm.gameplay.logging.LogginUtil;
import com.vm.gameplay.model.Computer;
import com.vm.gameplay.model.GameState;
import com.vm.gameplay.model.Line;
import com.vm.gameplay.model.Player;
import com.vm.gameplay.service.BluetoothGameService;

public class SurfaceViewActivity extends BaseActivity implements
		SurfaceHolder.Callback, DialogCallback, AnimationCallback,
		OnTouchListener, OnScoreListener {
	private SurfaceView surface;
	private SurfaceHolder holder;
	private int width;
	private int height;
	private int cellWidth = 100;
	private TextView tvPlayer[] = new TextView[2];
	private TextView tvScore[] = new TextView[2];
	private LinearLayout llPlayers;
	private BluetoothGameService mGameService;
	private boolean restart = false;
	private boolean finish = false;
	private int color;
	private GameState gameState;
	private GamePlay gamePlay;
	private int score[] = new int[2];

	@SuppressLint("NewApi")
	public void init() {
		setContentView(R.layout.game_play_view);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		boolean bluetooth = getIntent().getBooleanExtra("bluetooth", false);
		boolean singlePlayer = getIntent().getBooleanExtra("singlePlayer",
				false);

		initUIElements();
		initGameState(singlePlayer, bluetooth);
		initBluetoothService(singlePlayer, bluetooth);
		setupScoreTextViews(bluetooth);
		holder = surface.getHolder();
		holder.addCallback(this);
		surface.setOnTouchListener(this);
	}

	private void setupScoreTextViews(boolean bluetooth) {
		setupPlayerTextViews(0, gameState.getPlayer(0));
		int color = Color.BLACK;
		String name = "Waiting";
		if (!bluetooth) {
			color = gameState.getPlayer(1).getColor();
			name = gameState.getPlayer(1).getName() + " [0]";
		}
		setupPlayerTextViews(1, color, name);

		tvScore[0].setCompoundDrawablesWithIntrinsicBounds(R.drawable.tern, 0,
				0, 0);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (!gameState.isStart()) {
			Toast.makeText(SurfaceViewActivity.this,
					"Waiting for user to join", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!gameState.isMyTern()) {
			Toast.makeText(SurfaceViewActivity.this,
					gameState.getCurrentPlayer().getName() + "'s Turn",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (gameState.isStart() && checkFrame(event.getX(), event.getY())) {
			Line line = gamePlay.getSelectedLine(event.getX(), event.getY());
			move(line);
			if (gameState.isBluetooth()) {
				String out = "1::" + line.getStart().x / cellWidth + "::"
						+ line.getStart().y / cellWidth + "::"
						+ line.getEnd().x / cellWidth + "::" + line.getEnd().y
						/ cellWidth;
				mGameService.write(out.getBytes());
			}
		}
		return false;
	}

	private void setupPlayerTextViews(int index, Player player) {
		setupPlayerTextViews(index, player.getColor(), player.getName()
				+ " [0]");
	}

	private void setupPlayerTextViews(int index, int color, String name) {
		tvPlayer[index].setBackgroundColor(color);
		tvScore[index].setText(name);
		tvScore[index].setBackgroundColor(color);
	}

	private void initBluetoothService(boolean singlePlayer, boolean bluetooth) {
		mGameService = BluetoothGameService.getInstance(this);
		if (bluetooth) {
			mGameService.start();
			LogginUtil.logEvent(this, "Game play", "Game started",
					"Bluetooth waiting", 0);
		} else {
			if (singlePlayer) {
				LogginUtil.logEvent(this, "Game play", "Game started",
						"Single player", 0);
			} else {
				LogginUtil.logEvent(this, "Game play", "Game started",
						"two player local", 0);
			}
		}
	}

	private void initGameState(boolean singlePlayer, boolean bluetooth) {
		gameState = new GameState(
				((GameApplication) getApplicationContext()).getThemes());
		gameState.setThemeIndex(getIntent().getIntExtra("theme", 1));
		gameState.configureTheme(getIntent().getIntExtra("row", 7), getIntent()
				.getIntExtra("col", 5), getIntent().getIntArrayExtra("board"));
		ArrayList<Player> players1 = getIntent().getParcelableArrayListExtra(
				"players");
		gameState.setPlayers(players1);

		gameState.configurePlayers(singlePlayer, bluetooth);
	}

	private void initUIElements() {
		tvScore[0] = (TextView) findViewById(R.id.score1);
		tvScore[1] = (TextView) findViewById(R.id.score2);
		tvPlayer[0] = (TextView) findViewById(R.id.player1);
		tvPlayer[1] = (TextView) findViewById(R.id.player2);
		llPlayers = (LinearLayout) findViewById(R.id.players);
		surface = (SurfaceView) findViewById(R.id.mysurface);
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(FindGameActivity.STR_MESSAGE_DEVICE_NAME);
		intentFilter.addAction(FindGameActivity.STR_MESSAGE_READ);
		intentFilter.addAction(FindGameActivity.STR_MESSAGE_STATE_CHANGE);
		intentFilter.addAction(FindGameActivity.STR_MESSAGE_WRITE);
		this.registerReceiver(receiver, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	private void move(Line line) {
		if (line != null) {
			color = gameState.getCurrentPlayer().getColor();
			if (isValidMove(line)) {
				if (gamePlay.drawLine(line))
					changePlayer();
				initCanvas();
			}
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (gameState.isComputersTern()) {
						move(((Computer) gameState.getPlayers().get(1))
								.getNextMove());
					}
				}
			}, 300);
		}
	}

	private boolean isValidMove(Line line) {
		return !gamePlay.getLines().contains(line)
				&& checkFrame(line.getEnd().x, line.getEnd().y)
				&& checkFrame(line.getStart().x, line.getStart().y);
	}

	private void changePlayer() {
		gameState.changePlayer();
		tvScore[0].setCompoundDrawablesWithIntrinsicBounds(
				gameState.getPlayer() == 0 ? R.drawable.tern : 0, 0, 0, 0);
		tvScore[1].setCompoundDrawablesWithIntrinsicBounds(
				gameState.getPlayer() == 0 ? 0 : R.drawable.tern, 0, 0, 0);
	}

	@Override
	public void onBackPressed() {
		if (finish) {
			super.onBackPressed();
		} else {
			finish = true;
			Toast.makeText(this, "Press again to go back", Toast.LENGTH_SHORT)
					.show();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					finish = false;
				}
			}, 3000);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		String out = null;
		switch (item.getItemId()) {
		case R.id.restart:
			if (restart) {
				restart();
				if (gameState.isBluetooth()) {
					out = "3::1";
					mGameService.write(out.getBytes());
				}
			} else {
				restart = true;
				Toast.makeText(this, "Press again to restart",
						Toast.LENGTH_SHORT).show();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						restart = false;
					}
				}, 3000);
			}
			break;
		case R.id.undo:
			if (gameState.canUndo()) {
				undo();
				if (gameState.isBluetooth()) {
					out = "2::0";
					mGameService.write(out.getBytes());
				}
			} else {
				Toast.makeText(this, "Can't undo", Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void undo() {
		gamePlay.undo();
		initCanvas();
		changePlayer();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (height == 0) {
			adjustHeightWidth();
			gameState.getTheme().createDrawables(getResources(), height, width,
					cellWidth);
			if (!gameState.isBoardCreated())
				gameState.getTheme().createBoard(gameState.getTotal());
			restart();
		} else {
			initCanvas();
		}
	}

	private void adjustHeightWidth() {
		height = surface.getMeasuredHeight();
		width = surface.getMeasuredWidth();
		cellWidth = Math.min(width / (gameState.getTheme().getCol() + 1),
				height / (gameState.getTheme().getRow() + 1));
		height = (gameState.getTheme().getRow() + 1) * cellWidth;
		width = (gameState.getTheme().getCol() + 1) * cellWidth;
		gamePlay = new GamePlay(cellWidth, height, width, gameState, this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	private void initCanvas() {

		Canvas canvas = null;
		try {
			canvas = holder.lockCanvas();
			synchronized (holder) {
				gameState
						.getTheme()
						.getBackgroundDrawable()
						.setBounds(0, 0, surface.getWidth(),
								surface.getHeight());
				gameState.getTheme().getBackgroundDrawable().draw(canvas);
				// canvas.drawColor(Color.TRANSPARENT);
				Paint p = new Paint();

				for (Rect rectanlge : gamePlay.getRects1()) {
					p.setColor(gameState.getPlayers().get(0).getColor());
					canvas.drawRect(rectanlge, p);
				}
				for (Rect rectanlge : gamePlay.getRects2()) {
					p.setColor(gameState.getPlayers().get(1).getColor());
					canvas.drawRect(rectanlge, p);
				}
				p.setColor(Color.WHITE);
				int boardIndex = 0;
				for (int i = cellWidth / 2; i <= width - cellWidth / 2; i = i
						+ cellWidth) {
					for (int j = cellWidth / 2; j <= height - cellWidth / 2; j = j
							+ cellWidth) {
						canvas.drawCircle(i, j, 5, p);
						if (i < width - cellWidth && j < height - cellWidth) {
							int index = gameState.getTheme().getBoard()[boardIndex];
							boardIndex++;
							gameState
									.getTheme()
									.getCollectionDrawable(index)
									.setBounds(i + cellWidth / 3,
											j + cellWidth / 3,
											i + cellWidth * 2 / 3,
											j + cellWidth * 2 / 3);
							gameState.getTheme().getCollectionDrawable(index)
									.draw(canvas);
							if (gameState.getTheme().isBonus(index))
								gamePlay.getBonuses().add(new Point(i, j));
							if (gameState.getTheme().isPanulty(index))
								gamePlay.getPanultis().add(new Point(i, j));
						}
					}
				}
				int i = 0;
				for (Line line : gamePlay.getLines()) {
					Paint pp = new Paint();
					if (i == gamePlay.getLines().size() - 1)
						pp.setColor(color);
					else
						pp.setColor(Color.WHITE);
					pp.setStrokeWidth(5);
					canvas.drawLine(line.getStart().x, line.getStart().y,
							line.getEnd().x, line.getEnd().y, pp);
					i++;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
				holder.unlockCanvasAndPost(canvas);
			}
		}
	}

	private boolean checkFrame(float x, float y) {
		return x >= cellWidth / 2 && y >= cellWidth / 2 && x < width
				&& y < height;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void restart() {
		if (gameState.isSinglePlayer())
			((Computer) gameState.getPlayers().get(1)).setupMoves(height,
					width, cellWidth);
		gamePlay.clear();
		tvScore[0].setText(gameState.getPlayer(0).getName() + " [0]");
		if (gameState.isStart())
			tvScore[1].setText(gameState.getPlayer(1).getName() + " [0]");
		gameState.setPlayer(0);
		tvScore[0].setCompoundDrawablesWithIntrinsicBounds(R.drawable.tern, 0,
				0, 0);
		tvScore[1].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		initCanvas();
		startAnimation();
	}

	@Override
	public void onScoreUpdate(int curScore, int marked) {
		// score[0] = score1;
		score[gameState.getPlayer()] = curScore;
		StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
		String p1 = gameState.getCurrentPlayer().getName() + " [" + curScore
				+ "]";

		SpannableString str1 = new SpannableString(p1);
		str1.setSpan(bss, p1.indexOf('[') + 1, p1.indexOf(']'), 0);

		tvScore[gameState.getPlayer()].setText(str1);

		startAnimation();
		if (marked == gameState.getTotal()) {
			displayGameOverMessage();
		}
	}

	private void displayGameOverMessage() {
		MessageDialog dialog = new MessageDialog(this, 1);
		dialog.setCallback(this);
		dialog.setScores(gameState.getPlayers(), score, new int[] {
				gamePlay.getRects1().size(), gamePlay.getRects2().size() },
				gameState.getTheme().getGems());
		dialog.show();
	}

	@Override
	public void onDialogAction(int dialogId, Action action) {
		restart();
	}

	private void startAnimation() {
		llPlayers.startAnimation(new ScoreUpdateAnimation(this));
	}

	@Override
	public void onAnimate(float t) {
		int player1Score, b;

		if (gamePlay.getRects1().size() == 0
				&& gamePlay.getRects2().size() == 0) {
			player1Score = 1;
			b = 1;
		} else {
			player1Score = gamePlay.getRects1().size();
			b = gamePlay.getRects2().size();
		}

		LayoutParams lp1 = (LayoutParams) tvPlayer[0].getLayoutParams();
		lp1.weight = lp1.weight + (player1Score - lp1.weight) * t;

		LayoutParams lp2 = (LayoutParams) tvPlayer[1].getLayoutParams();
		lp2.weight = lp2.weight + (b - lp2.weight) * t;
		tvPlayer[0].setLayoutParams(lp1);
		tvPlayer[1].setLayoutParams(lp2);
		llPlayers.setWeightSum(llPlayers.getWeightSum()
				+ (player1Score + b - llPlayers.getWeightSum()) * t);
		tvPlayer[0].requestLayout();
		tvPlayer[1].requestLayout();
		llPlayers.requestLayout();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (gameState.isBluetooth())
			mGameService.stop();
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		public void onReceive(android.content.Context context, Intent intent) {
			String action = intent.getAction();
			switch (Integer.parseInt(action)) {
			case FindGameActivity.MESSAGE_STATE_CHANGE:

				switch (intent.getIntExtra("state",
						BluetoothGameService.STATE_NONE)) {
				case BluetoothGameService.STATE_CONNECTED:
					mGameService
							.write(gameState.getStartGameInstructionBytes());
					break;
				case BluetoothGameService.STATE_CONNECTING:
					break;
				case BluetoothGameService.STATE_LISTEN:
				case BluetoothGameService.STATE_NONE:
					finish();
					break;
				}
				break;
			case FindGameActivity.MESSAGE_WRITE:
				break;
			case FindGameActivity.MESSAGE_READ:
				byte[] readBuf = (byte[]) intent.getByteArrayExtra("data");
				String readMessage = new String(readBuf, 0, intent.getIntExtra(
						"length", 0));
				String[] message = readMessage.split("::");
				switch (Integer.parseInt(message[0])) {
				case 0:
					gameState.startGame(message);
					setupPlayerTextViews(1, gameState.getPlayer(1));
					LogginUtil.logEvent(SurfaceViewActivity.this, "Game play",
							"Game started", "Bluetooth joined", 0);
					break;
				case 1:
					Point start = new Point(Integer.parseInt(message[1])
							* cellWidth + cellWidth / 2,
							Integer.parseInt(message[2]) * cellWidth
									+ cellWidth / 2);
					Point end = new Point(Integer.parseInt(message[3])
							* cellWidth + cellWidth / 2,
							Integer.parseInt(message[4]) * cellWidth
									+ cellWidth / 2);
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
				break;
			case FindGameActivity.MESSAGE_DEVICE_NAME:
				break;

			}
		}

	};

}