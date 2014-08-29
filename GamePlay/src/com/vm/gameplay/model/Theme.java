package com.vm.gameplay.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class Theme {
	private int id;
	private int boardColor;
	private int lineColor;
	private int selectedLineColor;
	private int[] collectionRes;
	private BitmapDrawable[] collectionDrawables;
	private String name;
	private int bonus, panulty, defaultIcon;
	private int row = 7, col = 5;
	private String gems;
	private int[] board;

	public Theme(String name) {
		this.name = name;
	}

	public int getRow() {
		return row;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public Theme(int id, String name, int backgroundRes, int[] collectionRes,
			int bonus, int panulty, int defaultIcon) {
		super();
		this.id = id;
		this.boardColor = backgroundRes;
		this.collectionRes = collectionRes;
		this.name = name;
		this.bonus = bonus;
		this.panulty = panulty;
		this.defaultIcon = defaultIcon;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isBonus(int position) {
		return position == bonus;
	}

	public boolean isPanulty(int position) {
		return panulty == position;
	}

	public void createBoard(int total) {
		board = new int[total];
		board[(int) (Math.random() * total)] = -1;
		board[(int) (Math.random() * total)] = -1;
		board[(int) (Math.random() * total)] = -1;

		for (int i = 0; i < 3; i++) {
			int num = (int) (Math.random() * total);
			if (board[num] != -1) {
				board[num] = -2;
			}
		}

		for (int i = 0; i < total; i++) {
			if (board[i] != -1 && board[i] != -2) {
				int num = 0;
				do {
					num = (int) (Math.random() * collectionRes.length);
				} while (num == bonus || num == panulty);

				board[i] = num;
			} else {
				if (board[i] == -1)
					board[i] = bonus;
				else
					board[i] = panulty;
			}
		}
	}

	public void setBoard(int[] board) {
		this.board = board;
	}

	public int[] getBoard() {
		return board;
	}

	public void createDrawables(Resources r, int height, int width,
			int cellWidth) {
		if (collectionRes != null) {
			collectionDrawables = new BitmapDrawable[collectionRes.length];
			for (int i = 0; i < collectionRes.length; i++) {
				collectionDrawables[i] = getDrawable(r, collectionRes[i],
						cellWidth, cellWidth);
			}
		}
	}

	private BitmapDrawable getDrawable(Resources r, int rId, int height,
			int width) {
		Bitmap image = BitmapFactory.decodeResource(r, rId);
		Bitmap.createScaledBitmap(image, height, width, false);
		return new BitmapDrawable(r, image);
	}

	public int getBoardColor() {
		return boardColor;
	}

	public void setBoardColor(int backgroundRes) {
		this.boardColor = backgroundRes;
	}

	public int[] getCollectionRes() {
		return collectionRes;
	}

	public BitmapDrawable[] getCollectionDrawables() {
		return collectionDrawables;
	}

	public BitmapDrawable getCollectionDrawable(int index) {
		return collectionDrawables[index];
	}

	public void setCollectiondrawables(BitmapDrawable[] collectiondrawables) {
		this.collectionDrawables = collectiondrawables;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer(id + "");
		for (int i = 0; i < board.length; i++) {
			s.append(";;" + board[i]);
		}

		return s.toString();
	}

	public int getId() {
		return id;
	}

	public void setup(String theme) {
		String[] s = theme.split(";;");
		id = Integer.parseInt(s[0]);
		board = new int[s.length - 1];
		for (int i = 1; i < s.length; i++) {
			board[i - 1] = Integer.parseInt(s[i]);
		}
	}

	public int getDefaultIcon() {
		return collectionRes[defaultIcon];
	}

	public int getBonusIcon() {
		return collectionRes[bonus];
	}

	public int getPanultyIcon() {
		return collectionRes[panulty];
	}

	public void setGems(String gems) {
		this.gems = gems;
	}

	public String getGems() {
		return gems;
	}

	public int getTotal() {
		return row * col;
	}

	public int getLineColor() {
		return lineColor;
	}

	public void setLineColor(int lineColor) {
		this.lineColor = lineColor;
	}

	public int getSelectedLineColor() {
		return selectedLineColor;
	}

	public void setSelectedLineColor(int selectedLineColor) {
		this.selectedLineColor = selectedLineColor;
	}

}
