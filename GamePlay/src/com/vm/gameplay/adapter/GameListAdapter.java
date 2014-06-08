package com.vm.gameplay.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.vm.gameplay.custom_view.GameCustomView;
import com.vm.gameplay.model.Game;

public class GameListAdapter extends ArrayAdapter<Game> {

	private ArrayList<Game> gameList;

	public GameListAdapter(Context context, ArrayList<Game> gameList) {
		super(context, 0, gameList);
		this.gameList = gameList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GameCustomView view = (GameCustomView) convertView;
		if (view == null)
			view = new GameCustomView(getContext());
		view.setGame(gameList.get(position));
		return view;
	}

}
