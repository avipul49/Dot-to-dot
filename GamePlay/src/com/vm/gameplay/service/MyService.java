package com.vm.gameplay.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
	private CountDownTimer countDownTimer;
	private int timerCounter;
	private int totalTimerTime = 15000;
	public static final String LAST_TIMEOUT = "last_timeout";
	public static final String TIMEOUT = "timeout";
	public static final String TICK = "tick";
	public static final String STOP = "stop";
	public Receiver receiver = new Receiver();
	public static final String START = "start";

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(STOP);
		intentFilter.addAction(START);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// resetTimer(totalTimerTime);
		// timerCounter = 0;
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class PlayerTimer extends CountDownTimer {

		public PlayerTimer(long timeRemaining2) {
			super(timeRemaining2, 1000);
		}

		public void onTick(long millisUntilFinished) {
			long sec = (long) Math.floor(millisUntilFinished / 1000);
			Log.i("", "===" + sec);
			Intent intent = new Intent();
			intent.setAction(TICK);
			intent.putExtra("millisUntilFinished", millisUntilFinished);
			sendBroadcast(intent);
		}

		public void onFinish() {
			timerCounter++;
			Intent intent = new Intent();
			intent.setAction(TIMEOUT);
			sendBroadcast(intent);
			if (timerCounter == 3) {
				intent = new Intent();
				intent.setAction(LAST_TIMEOUT);
				sendStickyBroadcast(intent);
			} else
				resetTimer(totalTimerTime);
		}
	};

	private void resetTimer(long timeRemaining2) {
		if (countDownTimer != null)
			countDownTimer.cancel();
		countDownTimer = new PlayerTimer(timeRemaining2);
		countDownTimer.start();
	}

	class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(STOP)) {
				if (countDownTimer != null)
					countDownTimer.cancel();
			} else if (action.equals(START)) {
				resetTimer(totalTimerTime);
				timerCounter = 0;
			}
		}
	}

}
