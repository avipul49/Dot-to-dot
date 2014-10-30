package com.vm.gameplay;

public interface GamePlayInterface {
	void sendMove(String message);
	void leaveRoom();
	void gameOver(int i);
}
