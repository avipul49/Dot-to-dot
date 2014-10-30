package com.vm.gameplay.dialog;

public interface DialogCallback {
	void onDialogAction(int dialogId, Action action);

	public static enum Action {
		LEAVE, RESTART
	};

}
