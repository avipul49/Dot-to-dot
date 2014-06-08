package com.vm.gameplay.model;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

public class Line implements Parcelable {
	private Point start, end;

	public Line() {
	}

	public Line(Point start, Point end) {
		this.end = end;
		this.start = start;
	}

	public Point getStart() {
		return start;
	}

	public void setStart(Point start) {
		this.start = start;
	}

	public Point getEnd() {
		return end;
	}

	public void setEnd(Point end) {
		this.end = end;
	}

	@Override
	public int hashCode() {
		return start.x;
	}

	public Line(Parcel in) {
		super();
		start = in.readParcelable(Point.class.getClassLoader());
		end = in.readParcelable(Point.class.getClassLoader());
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		else {
			Line line = (Line) o;
			return this.start.x == line.start.x && this.start.y == line.start.y
					&& this.end.x == line.end.x && this.end.y == line.end.y;
		}
	}

	public boolean isHorizontal() {
		return start.y == end.y;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeParcelable(start, flags);
		dest.writeParcelable(end, flags);

	}

	public static final Parcelable.Creator CREATOR = new Creator<Line>() {

		@Override
		public Line createFromParcel(Parcel source) {
			return new Line(source);
		}

		@Override
		public Line[] newArray(int size) {

			return new Line[size];
		}
	};

	@Override
	public String toString() {
		return "Start: " + start.toString() + "   End: " + end.toString();
	}

}
