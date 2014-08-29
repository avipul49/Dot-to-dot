package com.vm.gameplay.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.graphics.Color;
import android.graphics.Point;

public class Computer extends Player {

	private HashMap<Line, Integer> lines = new HashMap<Line, Integer>();
	private int cellWidth;

	public Computer() {
		super("Computer", Color.parseColor("#FEE401"));
	}

	public void setupMoves(int height, int width, int cellWidth) {
		this.cellWidth = cellWidth;
		lines.clear();
		for (int i = cellWidth / 2; i <= width - cellWidth / 2; i = i
				+ cellWidth) {
			for (int j = cellWidth / 2; j <= height - cellWidth / 2; j = j
					+ cellWidth) {
				if (i + cellWidth < width) {
					Line line = new Line();
					line.setStart(new Point(i, j));
					line.setEnd(new Point(i + cellWidth, j));
					lines.put(line, 0);
				}
				if (j + cellWidth < height) {
					Line line = new Line();
					line.setStart(new Point(i, j));
					line.setEnd(new Point(i, j + cellWidth));
					lines.put(line, 0);
				}
			}
		}
	}

	public void moveLine(Line line) {
		lines.put(line, -1);
	}

	private int getScore(Line line) {
		int score = 0;
		if (line.isHorizontal()) {
			Line line1 = new Line(new Point(line.getStart().x,
					line.getStart().y - cellWidth), line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x, line.getEnd().y
					- cellWidth), line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x,
					line.getStart().y - cellWidth), new Point(line.getEnd().x,
					line.getEnd().y - cellWidth));

			if (lineMoved(line1))
				score++;
			if (lineMoved(line2))
				score++;
			if (lineMoved(line3))
				score++;

			line1 = new Line(line.getStart(), new Point(line.getStart().x,
					line.getStart().y + cellWidth));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x,
					line.getEnd().y + cellWidth));
			line3 = new Line(new Point(line.getStart().x, line.getStart().y
					+ cellWidth), new Point(line.getEnd().x, line.getEnd().y
					+ cellWidth));

			int temp = 0;
			if (lineMoved(line1))
				temp++;
			if (lineMoved(line2))
				temp++;
			if (lineMoved(line3))
				temp++;
			return score > temp ? score : temp;
		} else {
			Line line1 = new Line(new Point(line.getStart().x - cellWidth,
					line.getStart().y), line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x - cellWidth,
					line.getEnd().y), line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x - cellWidth,
					line.getStart().y), new Point(line.getEnd().x - cellWidth,
					line.getEnd().y));
			if (lineMoved(line1))
				score++;
			if (lineMoved(line2))
				score++;
			if (lineMoved(line3))
				score++;

			line1 = new Line(line.getStart(), new Point(line.getStart().x
					+ cellWidth, line.getStart().y));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x
					+ cellWidth, line.getEnd().y));
			line3 = new Line(new Point(line.getStart().x + cellWidth,
					line.getStart().y), new Point(line.getEnd().x + cellWidth,
					line.getEnd().y));
			int temp = 0;
			if (lineMoved(line1))
				temp++;
			if (lineMoved(line2))
				temp++;
			if (lineMoved(line3))
				temp++;
			return score > temp ? score : temp;
		}
	}

	private boolean lineMoved(Line line) {
		return lines.get(line) != null && lines.get(line) == -1;
	}

	public void setLineScore(Line line) {

		Integer score = lines.get(line);
		if (score != null && score != -1) {
			lines.put(line, getScore(line));
		}
	}

	public void updateScores(Line... lines) {
		for (Line line : lines) {
			setLineScore(line);
		}
	}

	public Line getNextMove() {
		Set<Map.Entry<Line, Integer>> set = lines.entrySet();
		ArrayList<Map.Entry<Line, Integer>> lineSet = new ArrayList<Map.Entry<Line, Integer>>(
				set);
		Collections.shuffle(lineSet);
		for (Map.Entry<Line, Integer> entry : lineSet) {
			if (entry.getValue() == 3) {
				return entry.getKey();
			}
		}
		for (Map.Entry<Line, Integer> entry : lineSet) {
			if (entry.getValue() == 0) {
				return entry.getKey();
			}
		}

		for (Map.Entry<Line, Integer> entry : lineSet) {
			if (entry.getValue() == 1) {
				return entry.getKey();
			}
		}
		for (Map.Entry<Line, Integer> entry : lineSet) {
			if (entry.getValue() == 2) {
				return entry.getKey();
			}
		}
		return null;

	}
}
