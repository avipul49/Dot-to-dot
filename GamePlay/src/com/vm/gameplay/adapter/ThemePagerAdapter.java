package com.vm.gameplay.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vm.gameplay.R;
import com.vm.gameplay.model.Theme;

public class ThemePagerAdapter extends PagerAdapter {

	private Theme[] themes;
	private LayoutInflater inflater;
	private int currentTheme;

	public ThemePagerAdapter(Context context, Theme[] themes) {
		this.themes = themes;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return themes.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		View layout = inflater.inflate(R.layout.theme_detail_view, null);

		ImageView defaultIcon = (ImageView) layout
				.findViewById(R.id.default_icon);

		ImageView bonudIcon = (ImageView) layout.findViewById(R.id.bonus_icon);

		ImageView panultytIcon = (ImageView) layout
				.findViewById(R.id.panulty_icon);
		defaultIcon.setImageResource(themes[position].getDefaultIcon());
		bonudIcon.setImageResource(themes[position].getBonusIcon());
		panultytIcon.setImageResource(themes[position].getPanultyIcon());
		layout.findViewById(R.id.theme_backgorund).setBackgroundColor(
				themes[position].getBoardColor());
		((ViewPager) container).addView(layout);

		return layout;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return themes[position].getName();
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		currentTheme = position;
	}

	public int getCurrentTheme() {
		return currentTheme;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}
}
