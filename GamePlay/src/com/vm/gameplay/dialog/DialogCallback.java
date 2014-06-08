package com.vm.gameplay.dialog;

public interface DialogCallback {
	void onDialogAction(int dialogId, Action action);

	public static enum Action {
		ACTION0, ACTION1, ACTION2
	};

}
