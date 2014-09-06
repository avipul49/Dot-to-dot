package com.vm.gameplay.model;

public class BoardDimensions {
	private int width;
	private int height;
	private int cellWidth;
	private int xOffset;
	private int yOffset;

	public boolean checkFrame(float x, float y) {
		return x >= 0 && y >= 0 && x < this.getWidth() && y < this.getHeight();
		// return true;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getCellWidth() {
		return cellWidth;
	}

	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	public int getxOffset() {
		return xOffset;
	}

	public void setxOffset(int row) {
		this.xOffset = (width % cellWidth + cellWidth) / 2;
	}

	public int getyOffset() {
		return yOffset;
	}

	public void setyOffset(int col) {
		this.yOffset = (height % cellWidth + cellWidth) / 2;
	}

	public int getxUpperLimit() {
		return width - xOffset;
	}

	public int getyUpperLimit() {
		return height - yOffset;
	}

	public void drawInCell(DrawInCellInterface drawInCellInterface) {
		int index = 0;
		for (int i = xOffset; i <= getxUpperLimit(); i = i + cellWidth) {
			for (int j = yOffset; j <= getyUpperLimit(); j = j + cellWidth) {
				drawInCellInterface.draw(this, i, j, index);
				if (i < width - cellWidth && j < height - cellWidth) {
					index++;
				}
			}
		}
	}

	public static interface DrawInCellInterface {
		public void draw(BoardDimensions boardDimensions, int i, int j,
				int index);

	}
}