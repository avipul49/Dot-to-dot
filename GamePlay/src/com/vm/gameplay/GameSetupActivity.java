package com.vm.gameplay;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.vm.gameplay.adapter.ThemePagerAdapter;
import com.vm.gameplay.custom_view.CirclePageIndicator;
import com.vm.gameplay.logging.LogginUtil;
import com.vm.gameplay.model.Player;

@SuppressLint("NewApi")
public class GameSetupActivity extends BaseActivity implements
		OnCheckedChangeListener {
	private int[] colors = { Color.parseColor("#AFE90C"), Color.BLUE,
			Color.CYAN, Color.GRAY, Color.GREEN, Color.RED, Color.YELLOW };
	private TextView tvPlayer1Color;
	private TextView tvPlayer2Color;
	private EditText etPlayer1Name;
	private EditText etPlayer2Name;
	private int player1ColorIndex = 0, player2ColorIndex = 1;
	private RadioGroup gameSize;
	private ViewPager viewPager;
	private int row, col;
	private ThemePagerAdapter themePagerAdapter;
	private boolean bluetooth, singlePLayer = true;

	public void init() {
		setContentView(R.layout.setup_game_view);
		bluetooth = getIntent().getBooleanExtra("bluetooth", false);
		gameSize = (RadioGroup) findViewById(R.id.game_size);
		singlePLayer = getIntent().getBooleanExtra("singlePlayer", false);
		gameSize.setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.big)).setChecked(true);
		tvPlayer1Color = (TextView) findViewById(R.id.player1_color);
		tvPlayer2Color = (TextView) findViewById(R.id.player2_color);
		etPlayer1Name = (EditText) findViewById(R.id.player1_name);
		etPlayer2Name = (EditText) findViewById(R.id.player2_name);
		tvPlayer1Color.setBackgroundColor(colors[player1ColorIndex]);
		tvPlayer2Color.setBackgroundColor(colors[player2ColorIndex]);
		CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circle_page_indicator);
		viewPager = (ViewPager) findViewById(R.id.pager);
		themePagerAdapter = new ThemePagerAdapter(this,
				((GameApplication) getApplicationContext()).getThemes());
		viewPager.setAdapter(themePagerAdapter);

		circlePageIndicator.setViewPager(viewPager);
		tvPlayer1Color.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				player1ColorIndex = (player1ColorIndex + 1) % colors.length;
				tvPlayer1Color.setBackgroundColor(colors[player1ColorIndex]);
			}
		});
		tvPlayer2Color.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				player2ColorIndex = (player2ColorIndex + 1) % colors.length;
				tvPlayer2Color.setBackgroundColor(colors[player2ColorIndex]);
			}
		});
		findViewById(R.id.start_game).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				String p1n = etPlayer1Name.getText().toString();
				p1n = p1n.isEmpty() ? "Player 1" : p1n;
				String p2n = etPlayer2Name.getText().toString();
				p2n = p2n.isEmpty() ? "Player 2" : p2n;
				ArrayList<Player> pp = new ArrayList<Player>();
				pp.add(new Player(p1n, colors[player1ColorIndex]));
				if (!bluetooth && !singlePLayer)
					pp.add(new Player(p2n, colors[player2ColorIndex]));
				Intent intent = new Intent(GameSetupActivity.this,
						SurfaceViewActivity.class);
				intent.putExtra("players", pp);
				intent.putExtra("row", row);
				intent.putExtra("col", col);
				intent.putExtra("theme", viewPager.getCurrentItem());
				intent.putExtra("bluetooth", bluetooth);
				intent.putExtra("singlePlayer", singlePLayer);
				GameSetupActivity.this.startActivity(intent);
				if (singlePLayer)
					LogginUtil.logEvent(GameSetupActivity.this, "User action",
							"Single player game created", p1n, 0);
				else
					LogginUtil.logEvent(GameSetupActivity.this, "User action",
							"Multiplayer game created", p1n + " vs " + p2n, 0);

				LogginUtil.logEvent(
						GameSetupActivity.this,
						"User action",
						"Theme selcted",
						((GameApplication) getApplicationContext()).getTheme(
								viewPager.getCurrentItem()).getName(), 0);
			}
		});
		if (bluetooth || singlePLayer) {
			findViewById(R.id.ll_second_player).setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.surface_view, menu);
		return true;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		String size = "";
		switch (group.getId()) {

		case R.id.game_size:
			switch (checkedId) {
			case R.id.small:
				size = "Small";
				row = 5;
				col = 4;
				break;
			case R.id.big:
				size = "Big";
				row = 7;
				col = 5;
				break;
			case R.id.medium:
				size = "Medium";
				row = 6;
				col = 4;
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}
		LogginUtil.logEvent(GameSetupActivity.this, "User action",
				"Game size selected", size, 0);
	}

}
