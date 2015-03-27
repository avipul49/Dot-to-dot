package com.vm.gameplay;

import java.util.HashMap;

import android.app.Application;
import android.graphics.Color;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
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

	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
		ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
							// company.
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	public synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = analytics.newTracker(R.xml.analytics);
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}

	public Theme getTheme(int theme) {
		return themes[theme];
	}

	public Theme[] getThemes() {
		themes[0].setBoard(null);
		return themes;
	}

}
