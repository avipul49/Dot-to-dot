package com.vm.gameplay.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.TextView;

import com.vm.gameplay.R;
import com.vm.gameplay.dialog.DialogCallback.Action;
import com.vm.gameplay.logging.LogginUtil;
import com.vm.gameplay.model.Computer;
import com.vm.gameplay.model.Player;

public class MessageDialog extends Dialog implements
		android.view.View.OnClickListener {

	private TextView tvCollectionText;
	private TextView tvPlayer1Name;
	private TextView tvPlayer2Name;
	private TextView tvPlayer1Collections;
	private TextView tvPlayer2Collections;
	private TextView tvPlayer1Score;
	private TextView tvPlayer2Score;
	private TextView tvResult;

	private DialogCallback callback;

	public MessageDialog(Context context, int dialogId) {
		super(context, R.style.CustomDialogTheme);
		init(R.layout.message_dialog);
	}

	public MessageDialog(Context context, int dialogId, int resourceId) {
		super(context, R.style.CustomDialogTheme);
		init(resourceId);
	}

	private void init(int resourceId) {
		this.setContentView(resourceId);
		this.setCancelable(false);
		tvCollectionText = (TextView) findViewById(R.id.tv_collections);
		tvPlayer1Name = (TextView) findViewById(R.id.player_1_name);
		tvPlayer2Name = (TextView) findViewById(R.id.player_2_name);
		tvPlayer1Collections = (TextView) findViewById(R.id.player_1_collection);
		tvPlayer2Collections = (TextView) findViewById(R.id.player_2_collection);
		tvPlayer1Score = (TextView) findViewById(R.id.player_1_score);
		tvPlayer2Score = (TextView) findViewById(R.id.player_2_score);
		tvResult = (TextView) findViewById(R.id.result);
		findViewById(R.id.leave).setOnClickListener(this);
		findViewById(R.id.restart).setOnClickListener(this);

		// findViewById(R.id.root).setAlpha(200);

	}

	public void setCallback(DialogCallback callback) {
		this.callback = callback;
	}

	public void setScores(boolean timeout, int curentPlayer,
			ArrayList<Player> players, int[] scores, int[] collections,
			String gem) {
		String gameOverMessage = "";
		if (timeout) {
			gameOverMessage = players.get(curentPlayer == 0 ? 1 : 0).getName()
					+ "\nWins";
		} else if (scores[0] > scores[1])
			gameOverMessage = players.get(0).getName() + "\nWins";
		else
			gameOverMessage = players.get(1).getName() + "\nWins";
		if (players.get(1) instanceof Computer) {
			if (scores[0] > scores[1]) {
				LogginUtil.logEvent(getContext(), "Game play",
						"Game against AI", "Player wins", 0);
			} else {
				LogginUtil.logEvent(getContext(), "Game play",
						"Game against AI", "Computer wins", 0);
			}
		}

		tvCollectionText.setText(String.format(getContext().getResources()
				.getString(R.string.collection_text), gem));
		tvPlayer1Name.setText(players.get(0).getName());
		tvPlayer2Name.setText(players.get(1).getName());
		Bitmap image = BitmapFactory.decodeResource(
				getContext().getResources(), R.drawable.reward);
		Bitmap.createScaledBitmap(image, 30, 30, false);
		BitmapDrawable crown = new BitmapDrawable(getContext().getResources(),
				image);
		crown.setBounds(30, 30, 30, 30);

		tvResult.setText(gameOverMessage);

		LogginUtil.logEvent(getContext(), "Game play", "Game ended",
				gameOverMessage, 0);

		tvPlayer1Collections.setText(collections[0] + "");
		tvPlayer2Collections.setText(collections[1] + "");

		tvPlayer1Score.setText(scores[0] + "");
		tvPlayer2Score.setText(scores[1] + "");
	}

	@Override
	public void onClick(View v) {
		dismiss();
		switch (v.getId()) {
		case R.id.leave:
			callback.onDialogAction(1, Action.LEAVE);
			break;
		case R.id.restart:
			callback.onDialogAction(1, Action.RESTART);
			break;
		default:
			break;
		}
	}

}
