package com.vm.gameplay.model;

public class Game {
	private Player player;
	private Theme theme;

	public Game(Player player, Theme theme) {
		super();
		this.player = player;
		this.theme = theme;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Theme getTheme() {
		return theme;
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

}
