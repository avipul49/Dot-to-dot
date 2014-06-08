package com.vm.gameplay.custom_view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vm.gameplay.R;
import com.vm.gameplay.model.Player;

public class PlayerCustomView extends LinearLayout {
	private Player player;
	private int[] colors = { Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN,
			Color.RED, Color.YELLOW };
	private int colorIndex;
	private boolean enabled = true;

	public void setPlayer(Player player) {
		this.player = player;
		tvColor.setBackgroundColor(player.getColor());
		etName.setText(player.getName());
	}

	private TextView tvColor;
	private EditText etName;

	public PlayerCustomView(Context context) {
		this(context, null);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		etName.setEnabled(enabled);
	}

	public PlayerCustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.player_view, this, true);

		tvColor = (TextView) findViewById(R.id.color);
		tvColor.setBackgroundColor(colors[0]);
		tvColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (enabled) {
					colorIndex = (colorIndex + 1) % colors.length;
					tvColor.setBackgroundColor(colors[colorIndex]);
				}
			}
		});
		etName = (EditText) findViewById(R.id.name);
		player = new Player("", colors[0]);
		tvColor.setText("Tap");
	}

	public Player getPlayer() {
		player.setColor(colors[colorIndex]);
		String name = etName.getText().toString();
		if (name.isEmpty())
			name = android.os.Build.MODEL;
		player.setName(etName.getText().toString());
		return player;
	}
}
