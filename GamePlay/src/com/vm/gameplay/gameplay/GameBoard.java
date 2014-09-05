package com.vm.gameplay.gameplay;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.vm.gameplay.model.BoardDimensions;
import com.vm.gameplay.model.BoardDimensions.DrawInCellInterface;
import com.vm.gameplay.model.GameState;
import com.vm.gameplay.model.Line;
import com.vm.gameplay.model.Theme;

public class GameBoard {

	private SurfaceView surfaceView;
	private GameState gameState;
	private GamePlay gamePlay;
	private Context context;
	private Theme theme;
	private BoardDimensions boardDimensions;
	private int color;
	private Canvas canvas = null;

	public GameBoard(Context context, SurfaceView surfaceView,
			GamePlay gamePlay, GameState gameState,
			BoardDimensions boardDimenstions) {
		this.context = context;
		this.surfaceView = surfaceView;
		this.gameState = gameState;
		this.gamePlay = gamePlay;
		this.theme = gameState.getTheme();
		this.boardDimensions = boardDimenstions;
	}

	public void setLastMoveColor(int color) {
		this.color = color;
	}

	public boolean isValidMove(Line line) {
		return !gamePlay.getLines().contains(line)
				&& boardDimensions.checkFrame(line.getEnd().x, line.getEnd().y)
				&& boardDimensions.checkFrame(line.getStart().x,
						line.getStart().y);
	}

	public void drawBoard() {

		SurfaceHolder holder = surfaceView.getHolder();
		try {
			canvas = holder.lockCanvas();
			synchronized (holder) {
				canvas.drawColor(theme.getBoardColor());
				if (gameState.isStart()) {
					drawBoxes(canvas, gamePlay.getRects1(), gameState
							.getPlayers().get(0).getColor());
					drawBoxes(canvas, gamePlay.getRects2(), gameState
							.getPlayers().get(1).getColor());
				}
				final Paint p = new Paint();
				p.setColor(Color.WHITE);
				final float strokeWidth = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources()
								.getDisplayMetrics());
				final float radius = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources()
								.getDisplayMetrics());

				boardDimensions.drawInCell(new DrawInCellInterface() {

					@Override
					public void draw(BoardDimensions boardDimensions, int i,
							int j, int boardIndex) {
						if (i < boardDimensions.getWidth()
								- boardDimensions.getCellWidth()
								&& j < boardDimensions.getHeight()
										- boardDimensions.getCellWidth()) {
							int index = theme.getBoard()[boardIndex];
							gameState
									.getTheme()
									.getCollectionDrawable(index)
									.setBounds(
											i + boardDimensions.getCellWidth()
													/ 5,
											j + boardDimensions.getCellWidth()
													/ 5,
											i + boardDimensions.getCellWidth()
													* 4 / 5,
											j + boardDimensions.getCellWidth()
													* 4 / 5);
							theme.getCollectionDrawable(index).draw(canvas);
							if (theme.isBonus(index))
								gamePlay.getBonuses().add(new Point(i, j));
							if (theme.isPanulty(index))
								gamePlay.getPanultis().add(new Point(i, j));
						}
						Paint blurLinePaint = new Paint();
						blurLinePaint.setColor(theme.getLineColor());

						blurLinePaint.setStrokeWidth(strokeWidth);
						if (i < boardDimensions.getxUpperLimit()
								- boardDimensions.getxOffset())
							canvas.drawLine(i, j,
									i + boardDimensions.getCellWidth(), j,
									blurLinePaint);
						if (j < boardDimensions.getyUpperLimit()
								- boardDimensions.getyOffset())
							canvas.drawLine(i, j, i,
									j + boardDimensions.getCellWidth(),
									blurLinePaint);
					}
				});
				int lineIndex = 0;
				for (Line line : gamePlay.getLines()) {
					Paint pp = new Paint();
					if (lineIndex == gamePlay.getLines().size() - 1)
						pp.setColor(color);
					else
						pp.setColor(Color.WHITE);
					pp.setStrokeWidth(strokeWidth);
					canvas.drawLine(line.getStart().x, line.getStart().y,
							line.getEnd().x, line.getEnd().y, pp);
					lineIndex++;
				}

				boardDimensions.drawInCell(new DrawInCellInterface() {
					@Override
					public void draw(BoardDimensions boardDimensions, int i,
							int j, int index) {
						canvas.drawCircle(i, j, radius, p);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
				holder.unlockCanvasAndPost(canvas);
			}
		}
	}

	private void drawBoxes(Canvas canvas, ArrayList<Rect> rects, int color) {
		for (Rect rectanlge : rects) {
			Paint p = new Paint();
			p.setColor(color);
			canvas.drawRect(rectanlge, p);
		}
	}
}
