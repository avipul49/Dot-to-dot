package com.vm.gameplay.model;

import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {
	/**
	 * 
	 */
	private String name;
	private int color;

	public Player(Parcel in) {
		super();
		this.name = in.readString();
		this.color = in.readInt();
	}

	public Player(String name, int color) {
		super();
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(this.name);
		dest.writeInt(this.color);
	}

	public static final Parcelable.Creator CREATOR = new Creator<Player>() {

		@Override
		public Player createFromParcel(Parcel source) {
			return new Player(source);
		}

		@Override
		public Player[] newArray(int size) {

			return new Player[size];
		}
	};

	public String toString() {
		return color + "::" + name;
	};

	public Date getFirstMonday(Date startDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		int day = c.get(Calendar.DAY_OF_WEEK);
		while (day != 1) {
			c.add(Calendar.DATE, 1);
			day = c.get(Calendar.DAY_OF_WEEK);
		}
		return c.getTime();
	}

}
