package com.vm.gameplay.logging;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class LogginUtil {

	public static void logEvent(Context context, String category, String event,
			String label, long value) {

		EasyTracker easyTracker = EasyTracker.getInstance(context);

		// MapBuilder.createEvent().build() returns a Map of event fields and
		// values
		// that are set and sent with the hit.
		easyTracker.send(MapBuilder.createEvent(category, // Event category
															// (required)
				event, // Event action (required)
				label, // Event label
				value) // Event value
				.build());
	}
}
