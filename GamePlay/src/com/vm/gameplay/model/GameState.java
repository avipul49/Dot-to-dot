package com.vm.gameplay.model;

import java.util.ArrayList;

import android.annotation.SuppressLint;

@SuppressLint("NewApi")
public class GameState {
	private boolean canUndo = false;
	private ArrayList<Player> players;
	private boolean isOnline = false, start = false;
	private boolean singlePlayer = false;
	private Theme[] themes;
	private int themeIndex = 1;
	private int color;
	private int player = 0, me = 0;

	public GameState(Theme[] themes) {
		setTheme(themes);
	}

	public void configureTheme(int rows, int cols, int[] board) {
		themes[themeIndex].setRow(rows);
		themes[themeIndex].setCol(cols);
		if (board != null) {
			themes[themeIndex].setBoard(board);
		}
	}

	public void configureTheme(Theme theme) {
		themes[themeIndex].setRow(theme.getRow());
		themes[themeIndex].setCol(theme.getCol());
		if (theme.getBoard() != null) {
			themes[themeIndex].setBoard(theme.getBoard());
		}
	}

	public boolean isGameOver(int marked) {
		return marked == this.getTotal();
	}

	public String getStartGameMessage() {
		return String.format("0::%s::%s::%s::%s", themes[themeIndex],
				themes[themeIndex].getRow(), themes[themeIndex].getCol(),
				themes[themeIndex].getId());
	}

	public byte[] getStartGameInstructionBytes() {
		return getStartGameMessage().getBytes();
	}

	public int getTotal() {
		return themes[themeIndex].getTotal();
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public Player getPlayer(int index) {
		return players.get(index);
	}

	public void startGame(String[] message) {
		String s = "";
		if (message.length == 3) {
			s = message[2];
		}
		Player p = new Player(s, Integer.parseInt(message[1]));
		if (p.getName().isEmpty()) {
			p.setName("Player 2");
		}
		addPlayer(p);
		start = true;
	};

	public void configurePlayers(boolean singlePlayer, boolean isOnline) {

		this.singlePlayer = singlePlayer;
		this.isOnline = isOnline;
		if (players.get(0).getName().isEmpty()) {
			players.get(0).setName("Player 1");
		}
		me = 0;
		if (players.size() == 2) {
			if (players.get(1).getName().isEmpty()) {
				players.get(1).setName("Player 2");
			}
			start = true;
			me = 1;
		} else {
			if (singlePlayer) {
				Player com = new Computer();
				players.add(com);
				start = true;
				me = 0;
			}
		}
	}

	public boolean isMyTurn() {
		return ((!isOnline && !singlePlayer) || me == player);
	}

	public Player getCurrentPlayer() {
		return players.get(player);
	}

	public boolean isComputersTern() {
		return singlePlayer && me != player;
	}

	public void changePlayer() {
		player = player == 0 ? 1 : 0;
	}

	public boolean canUndo() {
		return !singlePlayer && canUndo && (!isOnline || me != player);
	}

	public boolean isBoardCreated() {
		// return isOnline && me == 1;
		return themes[themeIndex].getBoard() != null;
	}

	public boolean isCanUndo() {
		return canUndo;
	}

	public void setCanUndo(boolean canUndo) {
		this.canUndo = canUndo;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

	public boolean isBluetooth() {
		return isOnline;
	}

	public void setBluetooth(boolean bluetooth) {
		this.isOnline = bluetooth;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public Theme getTheme() {
		return themes[themeIndex];
	}

	public void setTheme(Theme[] themes) {
		this.themes = themes;
	}

	public int getThemeIndex() {
		return themeIndex;
	}

	public void setThemeIndex(int themeIndex) {
		this.themeIndex = themeIndex;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isSinglePlayer() {
		return singlePlayer;
	}

	public void setSinglePlayer(boolean singlePlayer) {
		this.singlePlayer = singlePlayer;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	}

	public int getMe() {
		return me;
	}

	public void setMe(int me) {
		this.me = me;
	}

	public void createBoard() {
		this.getTheme().createBoard();
	}

	public void createQuickBoard() {
		this.getTheme().createQuickBoard();
	}

}
