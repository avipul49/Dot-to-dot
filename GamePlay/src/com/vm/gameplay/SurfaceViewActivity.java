package com.vm.gameplay;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
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
import com.vm.gameplay.logging.LogginUtil;
import com.vm.gameplay.model.Computer;
import com.vm.gameplay.model.Line;
import com.vm.gameplay.model.Player;
import com.vm.gameplay.model.Theme;
import com.vm.gameplay.service.BluetoothGameService;

public class SurfaceViewActivity extends BaseActivity implements
		SurfaceHolder.Callback, DialogCallback, AnimationCallback {
	private SurfaceView surface;
	private SurfaceHolder holder;
	int width;
	int height;
	private ArrayList<Line> lines = new ArrayList<Line>();
	private ArrayList<Rect> rects1 = new ArrayList<Rect>();
	private ArrayList<Rect> rects2 = new ArrayList<Rect>();
	private int cellWidth = 100;
	private int player = 0, me = 0;
	private TextView tvScore1, tvScore2, tvPlayer1, tvPlayer2;
	private LinearLayout llPlayers;
	private int total, marked;
	private boolean canUndo = false;
	private ArrayList<Player> players;
	private boolean bluetooth = false, start = false;
	private BluetoothGameService mGameService;
	private Theme theme;
	private int themeIndex = 1;
	private ArrayList<Point> bonuses = new ArrayList<Point>();
	private ArrayList<Point> panultis = new ArrayList<Point>();
	private boolean restart = false;
	private boolean finish = false;
	private boolean singlePlayer = false;
	private int color;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			String action = intent.getAction();
			switch (Integer.parseInt(action)) {
			case FindGameActivity.MESSAGE_STATE_CHANGE:

				switch (intent.getIntExtra("state",
						BluetoothGameService.STATE_NONE)) {
				case BluetoothGameService.STATE_CONNECTED:
					mGameService.write(("0::" + players.get(0) + "::" + theme
							+ "::" + theme.getRow() + "::" + theme.getCol()
							+ "::" + theme.getId()).getBytes());
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
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, intent.getIntExtra(
						"length", 0));
				Log.i("ReadMsg", readMessage);
				String[] message = readMessage.split("::");
				switch (Integer.parseInt(message[0])) {
				case 0:
					String s = "";
					if (message.length == 3) {
						s = message[2];
					}
					Player p = new Player(s, Integer.parseInt(message[1]));
					if (p.getName().isEmpty()) {
						p.setName("Player 2");
					}
					players.add(p);
					tvPlayer2.setBackgroundColor(p.getColor());
					tvScore2.setText(p.getName() + " [0]");
					tvScore2.setBackgroundColor(p.getColor());
					start = true;
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
				// Toast.makeText(SurfaceViewActivity.this, readMessage,
				// Toast.LENGTH_SHORT).show();
				// mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
				// + readMessage);
				break;
			case FindGameActivity.MESSAGE_DEVICE_NAME:
				break;

			}
		};
	};

	private int score[] = new int[2];

	public void init() {

		setContentView(R.layout.game_play_view);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		themeIndex = getIntent().getIntExtra("theme", 1);

		theme = ((GameApplication) getApplicationContext())
				.getTheme(themeIndex);
		theme.setRow(getIntent().getIntExtra("row", 7));
		theme.setCol(getIntent().getIntExtra("col", 5));
		int[] board = getIntent().getIntArrayExtra("board");
		if (board != null) {
			theme.setBoard(board);
		}
		players = getIntent().getParcelableArrayListExtra("players");
		bluetooth = getIntent().getBooleanExtra("bluetooth", false);
		mGameService = BluetoothGameService.getInstance(this);

		surface = (SurfaceView) findViewById(R.id.mysurface);
		// surface.setBackgroundResource(R.drawable.backgroumd);
		tvScore1 = (TextView) findViewById(R.id.score1);
		tvPlayer1 = (TextView) findViewById(R.id.player1);

		tvScore2 = (TextView) findViewById(R.id.score2);
		tvPlayer2 = (TextView) findViewById(R.id.player2);
		llPlayers = (LinearLayout) findViewById(R.id.players);

		if (players.get(0).getName().isEmpty()) {
			players.get(0).setName("Player 1");
		}
		tvPlayer1.setBackgroundColor(players.get(0).getColor());
		tvScore1.setText(players.get(0).getName() + " [0]");
		tvScore1.setBackgroundColor(players.get(0).getColor());
		int c = Color.BLACK;
		String n = "Waiting";
		singlePlayer = getIntent().getBooleanExtra("singlePlayer", false);
		me = 0;
		if (players.size() == 2) {
			if (players.get(1).getName().isEmpty()) {
				players.get(1).setName("Player 2");
			}
			c = players.get(1).getColor();
			n = players.get(1).getName() + " [0]";
			start = true;
			me = 1;
			LogginUtil.logEvent(this, "Game play", "Game started",
					"two player local", 0);
		} else {
			if (singlePlayer) {
				LogginUtil.logEvent(this, "Game play", "Game started",
						"Single player", 0);
				Player com = new Computer();
				players.add(com);
				c = players.get(1).getColor();
				n = players.get(1).getName() + " [0]";
				start = true;
				me = 0;
			} else {
				mGameService.start();
				LogginUtil.logEvent(this, "Game play", "Game started",
						"Bluetooth waiting", 0);
			}
		}

		tvPlayer2.setBackgroundColor(c);
		tvScore2.setText(n);
		tvScore2.setBackgroundColor(c);
		tvScore1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tern, 0, 0,
				0);
		// tvPlayer.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// PopupMenu popup = new PopupMenu(SurfaceViewActivity.this,
		// tvPlayer);
		// // Inflating the Popup using xml file
		// popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
		//
		// // registering popup with OnMenuItemClickListener
		// popup.setOnMenuItemClickListener(new
		// PopupMenu.OnMenuItemClickListener() {
		// public boolean onMenuItemClick(MenuItem item) {
		// Toast.makeText(SurfaceViewActivity.this,
		// "You Clicked : " + item.getTitle(),
		// Toast.LENGTH_SHORT).show();
		// return true;
		// }
		// });
		//
		// popup.show();// showing popup menu
		// }
		// });
		holder = surface.getHolder();
		holder.addCallback(this);

		surface.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (!start) {
					Toast.makeText(SurfaceViewActivity.this,
							"Waiting for user to join", Toast.LENGTH_SHORT)
							.show();
					return false;
				}
				if ((bluetooth || singlePlayer) && me != player) {
					Toast.makeText(SurfaceViewActivity.this,
							players.get(player).getName() + "'s Turn",
							Toast.LENGTH_SHORT).show();
					return false;
				}

				if (start && checkFrame(event.getX(), event.getY())) {

					int xM = (((int) event.getX() - (cellWidth / 2)) / cellWidth)
							* cellWidth + cellWidth / 2;
					int yM = (((int) event.getY() - cellWidth / 2) / cellWidth)
							* cellWidth + cellWidth / 2;

					int x = (event.getX() - xM) > (xM + cellWidth - event
							.getX()) ? xM + cellWidth : xM;
					int y = (event.getY() - yM) > (yM + cellWidth - event
							.getY()) ? yM + cellWidth : yM;
					Log.i("SVA", "ex=" + event.getX() + "  ey=" + event.getY());
					Log.i("SVA", "xM=" + xM + "  yM=" + yM);
					Log.i("SVA", "x=" + x + "  y=" + y);

					//
					// float tl = Math.abs(xM - event.getX())
					// + Math.abs(yM - event.getY());
					// float tr = Math.abs(xM + cellWidth - event.getX())
					// + Math.abs(yM - event.getY());
					// float bl = Math.abs(xM - event.getX())
					// + Math.abs(yM + cellWidth - event.getY());
					// float br = Math.abs(xM + cellWidth - event.getX())
					// + Math.abs(yM + cellWidth - event.getY());
					// Line tLine = new Line();
					// Point tStart = new Point(), tEnd = new Point();

					Line line = new Line();
					Point start = new Point(), end = new Point();
					if (Math.abs(x - event.getX()) < Math.abs(y - event.getY())) {
						start.x = x;
						start.y = yM;
						end.x = x;
						end.y = yM + cellWidth;

					} else {
						start.x = xM;
						start.y = y;
						end.x = xM + cellWidth;
						end.y = y;
					}
					line.setEnd(end);
					line.setStart(start);
					move(line);
					if (bluetooth) {
						String out = "1::" + line.getStart().x / cellWidth
								+ "::" + line.getStart().y / cellWidth + "::"
								+ line.getEnd().x / cellWidth + "::"
								+ line.getEnd().y / cellWidth;
						mGameService.write(out.getBytes());
					}
				}
				return false;
			}
		});
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
			color = players.get(player).getColor();
			if (!lines.contains(line)
					&& checkFrame(line.getEnd().x, line.getEnd().y)
					&& checkFrame(line.getStart().x, line.getStart().y)) {
				lines.add(line);
				if (!checkForClosedLoop(null, line)) {
					canUndo = true;
					changePlayer();
				} else {
					canUndo = false;
				}
				initCanvas();
			}

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (singlePlayer && me != player) {
						move(((Computer) players.get(1)).getNextMove());
					}
				}
			}, 300);

		}

	}

	private void changePlayer() {
		player = player == 0 ? 1 : 0;
		tvScore1.setCompoundDrawablesWithIntrinsicBounds(
				player == 0 ? R.drawable.tern : 0, 0, 0, 0);
		tvScore2.setCompoundDrawablesWithIntrinsicBounds(player == 0 ? 0
				: R.drawable.tern, 0, 0, 0);
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
				if (bluetooth) {
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
			// if(!bluetooth||)
			if (canUndo && (!bluetooth || me != player)) {
				undo();
				if (bluetooth) {
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
		if (!singlePlayer) {
			canUndo = false;
			lines.remove(lines.size() - 1);
			initCanvas();
			changePlayer();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (height == 0) {
			height = surface.getMeasuredHeight();
			width = surface.getMeasuredWidth();
			cellWidth = Math.min(width / (theme.getCol() + 1),
					height / (theme.getRow() + 1));
			height = (theme.getRow() + 1) * cellWidth;
			width = (theme.getCol() + 1) * cellWidth;
			theme.createDrawables(getResources(), height, width, cellWidth);
			total = theme.getCol() * theme.getRow();
			if (!bluetooth || me == 0)
				theme.setBoard(total);
			Log.i("surface height", "surface height: " + total + "  ");

			restart();
		} else {
			initCanvas();
		}
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

				// Paint p = new Paint();

				theme.getBackgroundDrawable().setBounds(0, 0,
						surface.getWidth(), surface.getHeight());
				theme.getBackgroundDrawable().draw(canvas);
				// canvas.drawColor(Color.TRANSPARENT);
				Paint p = new Paint();

				for (Rect rectanlge : rects1) {
					p.setColor(players.get(0).getColor());
					canvas.drawRect(rectanlge, p);
				}
				for (Rect rectanlge : rects2) {
					p.setColor(players.get(1).getColor());
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
							int index = theme.getBoard()[boardIndex];
							boardIndex++;
							theme.getCollectionDrawable(index).setBounds(
									i + cellWidth / 3, j + cellWidth / 3,
									i + cellWidth * 2 / 3,
									j + cellWidth * 2 / 3);
							theme.getCollectionDrawable(index).draw(canvas);
							if (theme.isBonus(index))
								bonuses.add(new Point(i, j));
							if (theme.isPanulty(index))
								panultis.add(new Point(i, j));
						}
					}
				}
				int i = 0;
				for (Line line : lines) {
					Paint pp = new Paint();
					if (i == lines.size() - 1)
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

	private boolean checkForClosedLoop(Canvas canvas, Line line) {
		boolean found = false;
		ArrayList<Rect> rects = player == 0 ? rects1 : rects2;
		if (singlePlayer)
			((Computer) players.get(1)).moveLine(line);
		if (line.isHorizontal()) {
			Line line1 = new Line(new Point(line.getStart().x,
					line.getStart().y - cellWidth), line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x, line.getEnd().y
					- cellWidth), line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x,
					line.getStart().y - cellWidth), new Point(line.getEnd().x,
					line.getEnd().y - cellWidth));
			if (singlePlayer)
				((Computer) players.get(1)).updateScores(line1, line2, line3);

			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x, line.getStart().y
						- cellWidth, line.getStart().x + cellWidth,
						line.getStart().y);
				rects.add(rectanlge);
				found = true;
				mark();
			}

			line1 = new Line(line.getStart(), new Point(line.getStart().x,
					line.getStart().y + cellWidth));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x,
					line.getEnd().y + cellWidth));
			line3 = new Line(new Point(line.getStart().x, line.getStart().y
					+ cellWidth), new Point(line.getEnd().x, line.getEnd().y
					+ cellWidth));
			if (singlePlayer)
				((Computer) players.get(1)).updateScores(line1, line2, line3);
			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x, line.getStart().y,
						line.getStart().x + cellWidth, line.getStart().y
								+ cellWidth);
				rects.add(rectanlge);
				found = true;
				mark();
			}

		} else {
			Line line1 = new Line(new Point(line.getStart().x - cellWidth,
					line.getStart().y), line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x - cellWidth,
					line.getEnd().y), line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x - cellWidth,
					line.getStart().y), new Point(line.getEnd().x - cellWidth,
					line.getEnd().y));
			if (singlePlayer)
				((Computer) players.get(1)).updateScores(line1, line2, line3);
			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x - cellWidth,
						line.getStart().y, line.getStart().x, line.getStart().y
								+ cellWidth);
				rects.add(rectanlge);
				found = true;
				mark();
			}

			line1 = new Line(line.getStart(), new Point(line.getStart().x
					+ cellWidth, line.getStart().y));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x
					+ cellWidth, line.getEnd().y));
			line3 = new Line(new Point(line.getStart().x + cellWidth,
					line.getStart().y), new Point(line.getEnd().x + cellWidth,
					line.getEnd().y));
			if (singlePlayer)
				((Computer) players.get(1)).updateScores(line1, line2, line3);
			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x, line.getStart().y,
						line.getStart().x + cellWidth, line.getStart().y
								+ cellWidth);
				rects.add(rectanlge);
				found = true;
				mark();
			}
		}
		return found;
	}

	private boolean findAllLine(Line line1, Line line2, Line line3) {
		return lines.contains(line1) && lines.contains(line2)
				&& lines.contains(line3);
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

		if (singlePlayer)
			((Computer) players.get(1)).setupMoves(height, width, cellWidth);
		lines.clear();
		rects1.clear();
		rects2.clear();
		score[0] = 0;
		score[1] = 0;
		player = 0;
		tvScore1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tern, 0, 0,
				0);
		tvScore2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		initCanvas();
		// tvPlayer.setText(players.get(player).getName());
		// tvPlayer.setBackgroundColor(players.get(player).getColor());
		marked = 0;
		updateStatus();

	}

	private void mark() {
		ArrayList<Rect> rects = player == 0 ? rects1 : rects2;
		Rect rect = rects.get(rects.size() - 1);
		Point p = new Point(rect.left, rect.top);
		int point = bonuses.contains(p) ? 5 : panultis.contains(p) ? -2 : 1;
		score[player] = score[player] + point;

		marked++;
		updateStatus();
		Log.i("", "marked" + marked);
		if (marked == total) {

			MessageDialog dialog = new MessageDialog(this, 1);
			// dialog.setMessage("Final score " + players.get(0).getName() +
			// ": "
			// + score[0] + " " + players.get(1).getName() + ": "
			// + score[1]);
			dialog.setCallback(this);
			dialog.setScores(players, score,
					new int[] { rects1.size(), rects2.size() }, theme.getGems());
			dialog.show();
		}
	}

	@Override
	public void onDialogAction(int dialogId, Action action) {
		restart();
	}

	private void updateStatus() {
		llPlayers.startAnimation(new ScoreUpdateAnimation(this));
		// int a, b;
		//
		// tvPlayer1.setText(rects1.size() + "");
		// tvPlayer2.setText(rects2.size() + "");
		// if (rects1.size() == 0 && rects2.size() == 0) {
		// a = 1;
		// b = 1;
		// } else {
		// a = rects1.size();
		// b = rects2.size();
		// }
		//
		// LayoutParams lp1 = (LayoutParams) tvPlayer1.getLayoutParams();
		// lp1.weight = a;
		//
		// LayoutParams lp2 = (LayoutParams) tvPlayer2.getLayoutParams();
		// lp2.weight = b;
		//
		// tvPlayer1.setLayoutParams(lp1);
		// tvPlayer2.setLayoutParams(lp2);
		// llPlayers.setWeightSum(a + b);
	}

	@Override
	public void onAnimate(float t) {

		int a, b;
		StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
		String p1 = players.get(0).getName() + " [" + score[0] + "]";

		SpannableString str1 = new SpannableString(p1);
		str1.setSpan(bss, p1.indexOf('[') + 1, p1.indexOf(']'), 0);

		tvScore1.setText(str1);
		if (players.size() == 2) {
			String p2 = players.get(1).getName() + " [" + score[1] + "]";
			SpannableString str2 = new SpannableString(p2);
			str2.setSpan(bss, p2.indexOf('[') + 1, p2.indexOf(']'), 0);
			tvScore2.setText(str2);

		}
		// tvPlayer1.setText(rects1.size() + "");
		// tvPlayer2.setText(rects2.size() + "");
		if (rects1.size() == 0 && rects2.size() == 0) {
			a = 1;
			b = 1;
		} else {
			a = rects1.size();
			b = rects2.size();
		}

		LayoutParams lp1 = (LayoutParams) tvPlayer1.getLayoutParams();
		lp1.weight = lp1.weight + (a - lp1.weight) * t;

		LayoutParams lp2 = (LayoutParams) tvPlayer2.getLayoutParams();
		lp2.weight = lp2.weight + (b - lp2.weight) * t;
		tvPlayer1.setLayoutParams(lp1);
		tvPlayer2.setLayoutParams(lp2);
		llPlayers.setWeightSum(llPlayers.getWeightSum()
				+ (a + b - llPlayers.getWeightSum()) * t);
		tvPlayer1.requestLayout();
		tvPlayer2.requestLayout();
		llPlayers.requestLayout();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bluetooth)
			mGameService.stop();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putInt("player", player);
		savedInstanceState.putParcelableArrayList("lines", lines);
		savedInstanceState.putParcelableArrayList("rects1", rects1);
		savedInstanceState.putParcelableArrayList("rects2", rects2);
		// etc.
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		player = savedInstanceState.getInt("player");
		lines = savedInstanceState.getParcelableArrayList("lines");
		rects1 = savedInstanceState.getParcelableArrayList("rects1");
		rects2 = savedInstanceState.getParcelableArrayList("rects2");
	}

}