package com.vm.gameplay.gameplay;

import java.util.ArrayList;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.vm.gameplay.model.Computer;
import com.vm.gameplay.model.GameState;
import com.vm.gameplay.model.Line;

public class GamePlay {
	private ArrayList<Line> lines = new ArrayList<Line>();
	private ArrayList<Rect> rects1 = new ArrayList<Rect>();
	private ArrayList<Rect> rects2 = new ArrayList<Rect>();
	private ArrayList<Point> bonuses = new ArrayList<Point>();
	private ArrayList<Point> panultis = new ArrayList<Point>();
	// private int width;
	// private int height;
	private int cellWidth = 100;
	private GameState gameState;
	private int score[] = new int[2];
	private int marked;
	private OnScoreListener onScoreListener;

	public GamePlay(int cellWidth2, int height2, int width2,
			GameState gameState, OnScoreListener onScoreListener) {
		// this.width = width2;
		// this.height = height2;
		this.cellWidth = cellWidth2;
		this.gameState = gameState;
		this.onScoreListener = onScoreListener;
	}

	public boolean drawLine(Line line) {
		if (line != null) {
			lines.add(line);

			if (!checkForClosedLoop(line)) {
				gameState.setCanUndo(true);
				return true;
				// changePlayer();
			} else {
				gameState.setCanUndo(false);
			}
		}
		return false;
	}

	private boolean checkForClosedLoop(Line line) {
		boolean found = false;
		boolean singlePlayer = gameState.isSinglePlayer();
		ArrayList<Rect> rects = gameState.getPlayer() == 0 ? rects1 : rects2;
		if (singlePlayer)
			((Computer) gameState.getPlayers().get(1)).moveLine(line);
		if (line.isHorizontal()) {
			Line line1 = new Line(new Point(line.getStart().x,
					line.getStart().y - cellWidth), line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x, line.getEnd().y
					- cellWidth), line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x,
					line.getStart().y - cellWidth), new Point(line.getEnd().x,
					line.getEnd().y - cellWidth));
			if (singlePlayer)
				((Computer) gameState.getPlayers().get(1)).updateScores(line1,
						line2, line3);

			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x, line.getStart().y
						- cellWidth, line.getStart().x + cellWidth,
						line.getStart().y);
				rects.add(rectanlge);
				found = true;
				mark();
			}

			line1 = new Line(line.getStart(), new Point(line.getStart().x,
					line.getStart().y + cellWidth));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x,
					line.getEnd().y + cellWidth));
			line3 = new Line(new Point(line.getStart().x, line.getStart().y
					+ cellWidth), new Point(line.getEnd().x, line.getEnd().y
					+ cellWidth));
			if (singlePlayer)
				((Computer) gameState.getPlayers().get(1)).updateScores(line1,
						line2, line3);
			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x, line.getStart().y,
						line.getStart().x + cellWidth, line.getStart().y
								+ cellWidth);
				rects.add(rectanlge);
				found = true;
				mark();
			}

		} else {
			Line line1 = new Line(new Point(line.getStart().x - cellWidth,
					line.getStart().y), line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x - cellWidth,
					line.getEnd().y), line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x - cellWidth,
					line.getStart().y), new Point(line.getEnd().x - cellWidth,
					line.getEnd().y));
			if (singlePlayer)
				((Computer) gameState.getPlayers().get(1)).updateScores(line1,
						line2, line3);
			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x - cellWidth,
						line.getStart().y, line.getStart().x, line.getStart().y
								+ cellWidth);
				rects.add(rectanlge);
				found = true;
				mark();
			}

			line1 = new Line(line.getStart(), new Point(line.getStart().x
					+ cellWidth, line.getStart().y));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x
					+ cellWidth, line.getEnd().y));
			line3 = new Line(new Point(line.getStart().x + cellWidth,
					line.getStart().y), new Point(line.getEnd().x + cellWidth,
					line.getEnd().y));
			if (singlePlayer)
				((Computer) gameState.getPlayers().get(1)).updateScores(line1,
						line2, line3);
			if (findAllLine(line1, line2, line3)) {
				Rect rectanlge = new Rect(line.getStart().x, line.getStart().y,
						line.getStart().x + cellWidth, line.getStart().y
								+ cellWidth);
				rects.add(rectanlge);
				found = true;
				mark();
			}
		}
		return found;
	}

	private void mark() {
		ArrayList<Rect> rects = gameState.getPlayer() == 0 ? rects1 : rects2;
		Rect rect = rects.get(rects.size() - 1);
		Point p = new Point(rect.left, rect.top);
		int point = bonuses.contains(p) ? 5 : panultis.contains(p) ? -2 : 1;
		score[gameState.getPlayer()] = score[gameState.getPlayer()] + point;
		marked++;
		onScoreListener.onScoreUpdate(score[gameState.getPlayer()], marked);
	}

	private boolean findAllLine(Line line1, Line line2, Line line3) {
		return lines.contains(line1) && lines.contains(line2)
				&& lines.contains(line3);
	}

	public void undo() {
		gameState.setCanUndo(false);
		lines.remove(lines.size() - 1);
	}

	public void clear() {
		lines.clear();
		rects1.clear();
		rects2.clear();
		score[0] = 0;
		score[1] = 0;
		marked = 0;
	}

	public Line getSelectedLine(float eventx, float eventy) {
		int xM = (((int) eventx - (cellWidth / 2)) / cellWidth) * cellWidth
				+ cellWidth / 2;
		int yM = (((int) eventy - cellWidth / 2) / cellWidth) * cellWidth
				+ cellWidth / 2;

		int x = (eventx - xM) > (xM + cellWidth - eventx) ? xM + cellWidth : xM;
		int y = (eventy - yM) > (yM + cellWidth - eventy) ? yM + cellWidth : yM;

		Line line = new Line();
		Point start = new Point(), end = new Point();
		if (Math.abs(x - eventx) < Math.abs(y - eventy)) {
			start.x = x;
			start.y = yM;
			end.x = x;
			end.y = yM + cellWidth;
		} else {
			start.x = xM;
			start.y = y;
			end.x = xM + cellWidth;
			end.y = y;
		}
		line.setEnd(end);
		line.setStart(start);
		return line;
	}

	public ArrayList<Line> getLines() {
		return lines;
	}

	public void setLines(ArrayList<Line> lines) {
		this.lines = lines;
	}

	public ArrayList<Rect> getRects1() {
		return rects1;
	}

	public void setRects1(ArrayList<Rect> rects1) {
		this.rects1 = rects1;
	}

	public ArrayList<Rect> getRects2() {
		return rects2;
	}

	public void setRects2(ArrayList<Rect> rects2) {
		this.rects2 = rects2;
	}

	public ArrayList<Point> getBonuses() {
		return bonuses;
	}

	public void setBonuses(ArrayList<Point> bonuses) {
		this.bonuses = bonuses;
	}

	public ArrayList<Point> getPanultis() {
		return panultis;
	}

	public void setPanultis(ArrayList<Point> panultis) {
		this.panultis = panultis;
	}

}
