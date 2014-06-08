package com.vm.gameplay.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ScoreUpdateAnimation extends Animation {

	private AnimationCallback callback;

	public ScoreUpdateAnimation(AnimationCallback callback) {
		this.callback = callback;
		setDuration(300);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		callback.onAnimate(interpolatedTime);
	}
}
