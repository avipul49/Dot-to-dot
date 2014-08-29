package com.vm.gameplay.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vm.gameplay.R;
import com.vm.gameplay.model.Game;
import com.vm.gameplay.model.Player;

public class GameCustomView extends LinearLayout {

	public void setPlayer(Player player) {
		tvColor.setBackgroundColor(player.getColor());
		tvName.setText(player.getName());
	}

	private TextView tvColor;
	private TextView tvName;

	public GameCustomView(Context context) {
		this(context, null);
	}

	public void setGame(Game game) {
		this.findViewById(R.id.root).setBackgroundResource(
				game.getTheme().getBoardColor());
		setPlayer(game.getPlayer());

	}

	public GameCustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.game_view, this, true);
		tvColor = (TextView) findViewById(R.id.color);
		tvName = (TextView) findViewById(R.id.name);
	}

}
