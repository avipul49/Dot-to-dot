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
	private BoardDimensions boardDimensions;

	public Computer() {
		super("Computer", Color.parseColor("#FEE401"));
	}

	public void setupMoves(BoardDimensions boardDimensions) {
		this.boardDimensions = boardDimensions;
		lines.clear();
		for (int i = boardDimensions.getxOffset(); i <= boardDimensions
				.getxUpperLimit(); i = i + boardDimensions.getCellWidth()) {
			for (int j = boardDimensions.getyOffset(); j <= boardDimensions
					.getyUpperLimit(); j = j + boardDimensions.getCellWidth()) {
				if (i + boardDimensions.getxOffset() < boardDimensions
						.getxUpperLimit()) {
					Line line = new Line();
					line.setStart(new Point(i, j));
					line.setEnd(new Point(i + boardDimensions.getCellWidth(), j));
					lines.put(line, 0);
				}
				if (j + boardDimensions.getyOffset() < boardDimensions
						.getyUpperLimit()) {
					Line line = new Line();
					line.setStart(new Point(i, j));
					line.setEnd(new Point(i, j + boardDimensions.getCellWidth()));
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
					line.getStart().y - boardDimensions.getCellWidth()),
					line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x, line.getEnd().y
					- boardDimensions.getCellWidth()), line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x,
					line.getStart().y - boardDimensions.getCellWidth()),
					new Point(line.getEnd().x, line.getEnd().y
							- boardDimensions.getCellWidth()));

			if (islineMoved(line1))
				score++;
			if (islineMoved(line2))
				score++;
			if (islineMoved(line3))
				score++;

			line1 = new Line(line.getStart(), new Point(line.getStart().x,
					line.getStart().y + boardDimensions.getCellWidth()));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x,
					line.getEnd().y + boardDimensions.getCellWidth()));
			line3 = new Line(new Point(line.getStart().x, line.getStart().y
					+ boardDimensions.getCellWidth()), new Point(
					line.getEnd().x, line.getEnd().y
							+ boardDimensions.getCellWidth()));

			int temp = 0;
			if (islineMoved(line1))
				temp++;
			if (islineMoved(line2))
				temp++;
			if (islineMoved(line3))
				temp++;
			return score > temp ? score : temp;
		} else {
			Line line1 = new Line(new Point(line.getStart().x
					- boardDimensions.getCellWidth(), line.getStart().y),
					line.getStart());
			Line line2 = new Line(new Point(line.getEnd().x
					- boardDimensions.getCellWidth(), line.getEnd().y),
					line.getEnd());
			Line line3 = new Line(new Point(line.getStart().x
					- boardDimensions.getCellWidth(), line.getStart().y),
					new Point(line.getEnd().x - boardDimensions.getCellWidth(),
							line.getEnd().y));
			if (islineMoved(line1))
				score++;
			if (islineMoved(line2))
				score++;
			if (islineMoved(line3))
				score++;

			line1 = new Line(line.getStart(), new Point(line.getStart().x
					+ boardDimensions.getCellWidth(), line.getStart().y));
			line2 = new Line(line.getEnd(), new Point(line.getEnd().x
					+ boardDimensions.getCellWidth(), line.getEnd().y));
			line3 = new Line(new Point(line.getStart().x
					+ boardDimensions.getCellWidth(), line.getStart().y),
					new Point(line.getEnd().x + boardDimensions.getCellWidth(),
							line.getEnd().y));
			int temp = 0;
			if (islineMoved(line1))
				temp++;
			if (islineMoved(line2))
				temp++;
			if (islineMoved(line3))
				temp++;
			return score > temp ? score : temp;
		}
	}

	private boolean islineMoved(Line line) {
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
