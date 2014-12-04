package com.vm.gameplay;

import android.app.Application;
import android.graphics.Color;

import com.vm.gameplay.model.Theme;

public class GameApplication extends Application {

	private Theme[] themes;

	@Override
	public void onCreate() {
		super.onCreate();
		themes = new Theme[1];
		themes[0] = new Theme(0, "Basic", getResources().getColor(
				R.color.background), new int[] { R.drawable.heartbreak,
				R.drawable.cupcake, R.drawable.heart }, 1, 0, 2);
		themes[0].setName("Basic");
		themes[0].setGems("Flags");
		themes[0].setLineColor(getResources().getColor(R.color.blur_lines));
		themes[0].setSelectedLineColor(Color.WHITE);
	}

	public Theme getTheme(int theme) {
		return themes[theme];
	}

	public Theme[] getThemes() {
		themes[0].setBoard(null);
		return themes;
	}

}
