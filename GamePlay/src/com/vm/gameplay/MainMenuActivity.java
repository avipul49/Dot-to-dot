package com.vm.gameplay;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.example.games.tq.MainActivity;
import com.vm.gameplay.logging.LogginUtil;

public class MainMenuActivity extends BaseActivity implements OnClickListener {
	private BluetoothAdapter mBluetoothAdapter = null;
	private Intent intent;

	public void init() {

		setContentView(R.layout.main_menu_view);
		findViewById(R.id.local).setOnClickListener(this);
		findViewById(R.id.join_bluetooth).setOnClickListener(this);
		findViewById(R.id.start_bluetooth).setOnClickListener(this);
		findViewById(R.id.single_player).setOnClickListener(this);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		intent = null;

		switch (v.getId()) {
		case R.id.local:
			// MessageDialog md = new MessageDialog(this, 0);
			// md.show();
			intent = new Intent(this, GameSetupActivity.class);
			startActivity(intent);
			LogginUtil.logEvent(this, "User action", "Game selected",
					"Multiplayer local game", 0);
			break;
		case R.id.join_bluetooth:
			// intent = new Intent(this, FindGameActivity.class);
			// LogginUtil.logEvent(this, "User action", "Game selected",
			// "Join bluetooth game", 0);
			// if (mBluetoothAdapter.isEnabled()) {
			// // ensureDiscoverable();
			// startActivity(intent);
			// } else {
			// Intent enableIntent = new Intent(
			// BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// startActivityForResult(enableIntent, 0);
			// }
			intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			break;
		case R.id.start_bluetooth:
			intent = new Intent(this, GameSetupActivity.class);
			intent.putExtra("bluetooth", true);
			LogginUtil.logEvent(this, "User action", "Game selected",
					"Bluetooth game started", 0);
			if (mBluetoothAdapter.isEnabled()) {
				// ensureDiscoverable();
				startActivity(intent);
			} else {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, 0);
			}
			break;

		case R.id.single_player:
			LogginUtil.logEvent(this, "User action", "Game selected",
					"Single player game", 0);
			intent = new Intent(this, GameSetupActivity.class);
			intent.putExtra("singlePlayer", true);
			startActivity(intent);
			break;
		default:
			break;
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (intent != null)
				startActivity(intent);
		} else {
			// finish();
		}
	}
}
