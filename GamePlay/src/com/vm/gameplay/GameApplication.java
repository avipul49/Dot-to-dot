package com.vm.gameplay;

import android.app.Application;

import com.vm.gameplay.model.Theme;

public class GameApplication extends Application {

	private Theme[] themes;

	@Override
	public void onCreate() {
		super.onCreate();
		themes = new Theme[3];
		themes[0] = new Theme(0, "Basic", R.drawable.d_background, new int[] {
				R.drawable.red, R.drawable.green, R.drawable.yellow }, 1, 0, 2);
		themes[0].setName("Basic");
		themes[0].setGems("Flags");
		themes[1] = new Theme(1, "Go green", R.drawable.backgroumd, new int[] {
				R.drawable.tree, R.drawable.tree1, R.drawable.cactusicon }, 1,
				2, 0);
		themes[1].setName("Go green");
		themes[1].setGems("Trees");

		themes[2] = new Theme(1, "Crazzy cars", R.drawable.cars_bg_1,
				new int[] { R.drawable.red_car, R.drawable.blue_car,
						R.drawable.yello_car }, 1, 0, 2);
		themes[2].setName("Crazzy cars");
		themes[2].setGems("Cars");

	}

	public Theme getTheme(int theme) {
		return themes[theme];
	}

	public Theme[] getThemes() {
		return themes;
	}

}
