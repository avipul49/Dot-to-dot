package com.vm.gameplay.logging;

import android.content.Context;

import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.analytics.Tracker;
import com.vm.gameplay.GameApplication;
import com.vm.gameplay.GameApplication.TrackerName;

public class LogginUtil {

	public static void logEvent(Context context, String category, String event,
			String label, long value) {
		Tracker t = ((GameApplication) context.getApplicationContext())
				.getTracker(TrackerName.APP_TRACKER);
		t.enableAdvertisingIdCollection(true);

		t.send(MapBuilder.createEvent(category, // Event category
												// (required)
				event, // Event action (required)
				label, // Event label
				value) // Event value
				.build());
	}
}
