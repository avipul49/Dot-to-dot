/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vm.gameplay;

import java.util.ArrayList;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.vm.gameplay.custom_view.PlayerCustomView;
import com.vm.gameplay.logging.LogginUtil;
import com.vm.gameplay.model.Player;
import com.vm.gameplay.model.Theme;
import com.vm.gameplay.service.BluetoothGameService;

/**
 * This is the main Activity that displays the current chat session.
 */
@SuppressLint("NewApi")
public class FindGameActivity extends BaseActivity implements OnClickListener {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String STR_MESSAGE_STATE_CHANGE = "1";
	public static final String STR_MESSAGE_READ = "2";
	public static final String STR_MESSAGE_WRITE = "3";
	public static final String STR_MESSAGE_DEVICE_NAME = "4";
	public static final String STR_MESSAGE_TOAST = "5";
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private ListView lvGames;
	// private GameListAdapter adapter;
	private ArrayAdapter<String> deviceAdapter;
	// private ArrayList<Game> games;
	private PlayerCustomView pcvPlayer;
	private int devicesTried = 0;
	private Set<BluetoothDevice> pairedDevices;
	// Layout Views
	// Array adapter for the conversation thread
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothGameService mGameService = null;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			String action = intent.getAction();
			switch (Integer.parseInt(action)) {
			case MESSAGE_STATE_CHANGE:

				switch (intent.getIntExtra("state",
						BluetoothGameService.STATE_NONE)) {
				case BluetoothGameService.STATE_CONNECTED:
					// mChatService.write(("0::" + pcvPlayer.getPlayer())
					// .getBytes());

					// setStatus(getString(R.string.title_connected_to,
					// mConnectedDeviceName));
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothGameService.STATE_CONNECTING:

					setStatus(R.string.title_connecting);
					break;
				case BluetoothGameService.STATE_LISTEN:
				case BluetoothGameService.STATE_NONE:
					devicesTried++;
					if (devicesTried == mBluetoothAdapter.getBondedDevices()
							.size())
						setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// // construct a string from the buffer
				// String writeMessage = new String(writeBuf);
				// Toast.makeText(BluetoothGameSetupActivity.this, writeMessage,
				// Toast.LENGTH_SHORT).show();
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) intent.getByteArrayExtra("data");
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, intent.getIntExtra(
						"length", 0));
				Log.i("ReadMsg", readMessage);
				String[] message = readMessage.split("::");
				switch (Integer.parseInt(message[0])) {
				case 0:
					String s = "";
					if (message.length >= 3) {
						s = message[2];
					}
					Player p = new Player(s, Integer.parseInt(message[1]));
					Theme theme = new Theme("");
					if (message.length >= 7) {
						theme.setup(message[3]);
						theme.setRow(Integer.parseInt(message[4]));
						theme.setCol(Integer.parseInt(message[5]));
						theme.setId(Integer.parseInt(message[6]));
					}
					// Intent intent1 = new Intent(
					// BluetoothGameSetupActivity.this,
					// SurfaceViewActivity.class);
					// intent1.putExtra("players", pp);
					// intent1.putExtra("bluetooth", true);
					// BluetoothGameSetupActivity.this.startActivity(intent1);
					Intent intent1 = new Intent(FindGameActivity.this,
							SurfaceViewActivity.class);
					ArrayList<Player> pp = new ArrayList<Player>();
					mGameService.write(("0::" + pcvPlayer.getPlayer())
							.getBytes());
					pp.add(p);
					pp.add(pcvPlayer.getPlayer());
					intent1.putExtra("players", pp);
					intent1.putExtra("bluetooth", true);
					intent1.putExtra("theme", theme.getId());
					intent1.putExtra("board", theme.getBoard());
					intent1.putExtra("row", theme.getRow());
					intent1.putExtra("col", theme.getCol());
					FindGameActivity.this.startActivity(intent1);
					LogginUtil.logEvent(FindGameActivity.this, "User action",
							"Bluetooth game joined", p.getName(), 0);

					// games.add(new Game(p, theme));
					// adapter.notifyDataSetChanged();
					break;

				default:
					break;
				}
				// Toast.makeText(FindGameActivity.this, readMessage,
				// Toast.LENGTH_SHORT).show();
				// mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
				// + readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				// mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				// Toast.makeText(getApplicationContext(),
				// "Connected to " + mConnectedDeviceName,
				// Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				// Toast.makeText(getApplicationContext(),
				// msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
				// .show();
				break;
			}
		};
	};

	public void init() {
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.find_game_view);
		lvGames = (ListView) findViewById(R.id.game_list);
		pcvPlayer = (PlayerCustomView) findViewById(R.id.player_data);
		// games = new ArrayList<Game>();
		// adapter = new GameListAdapter(this, games);
		lvGames.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int i = 0;
				for (BluetoothDevice device : pairedDevices) {

					if (i == position) {
						BluetoothGameService.getInstance(FindGameActivity.this)
								.connect(
										mBluetoothAdapter
												.getRemoteDevice(device
														.getAddress()), false);
						break;
					}
					i++;
				}
			}
		});
		deviceAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		lvGames.setAdapter(deviceAdapter);
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		// ensureDiscoverable();
		findViewById(R.id.find_game).setOnClickListener(this);
		findDevices();
	}

	private void findDevices() {
		deviceAdapter.clear();
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				deviceAdapter.add(device.getName());
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (mGameService == null)
				setupGame();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(STR_MESSAGE_DEVICE_NAME);
		intentFilter.addAction(STR_MESSAGE_READ);
		intentFilter.addAction(STR_MESSAGE_STATE_CHANGE);
		intentFilter.addAction(STR_MESSAGE_WRITE);

		this.registerReceiver(receiver, intentFilter);
	}

	private void setupGame() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mGameService = BluetoothGameService.getInstance(this);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
		unregisterReceiver(receiver);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupGame();
			} else {
				Log.d(TAG, "BT not enabled");
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mGameService.connect(device, secure);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.find_game, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.start:
			if (mGameService != null) {
				Intent intent = new Intent(this, GameSetupActivity.class);
				intent.putExtra("bluetooth", true);
				this.startActivity(intent);
			}
			break;

		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return false;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.start_game:
			if (mGameService != null) {
				Intent intent = new Intent(this, GameSetupActivity.class);
				intent.putExtra("bluetooth", true);
				this.startActivity(intent);
			}
			break;
		case R.id.find_game:
			findDevices();
			break;
		default:
			break;
		}
	}

	public void setClickable(View view) {
		if (view != null) {
			view.setClickable(false);
			if (view instanceof ViewGroup) {
				ViewGroup vg = ((ViewGroup) view);
				for (int i = 0; i < vg.getChildCount(); i++) {
					setClickable(vg.getChildAt(i));
				}
			}
		}
	}
}
