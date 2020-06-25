package com.gh4a.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.gh4a.R;

@SuppressWarnings("unused")
public class ScrollAwareFloatingActionButtonBehavior extends FloatingActionButton.Behavior {
private final FloatingActionButton.OnVisibilityChangedListener mVisibilityChangedListener =
	new FloatingActionButton.OnVisibilityChangedListener() {
	@Override
	public void onHidden(final FloatingActionButton fab) {
		super.onHidden(fab);
		fab.setVisibility(View.INVISIBLE);
	}
};

public ScrollAwareFloatingActionButtonBehavior(final Context context, final AttributeSet attrs) {
	super(context, attrs);
}

@Override
public boolean onStartNestedScroll(final @NonNull CoordinatorLayout coordinatorLayout,
                                   final @NonNull FloatingActionButton child, final @NonNull View directTargetChild,
                                   final @NonNull View target, final int axes, final int type) {
	if (target.getTag(R.id.FloatingActionButtonScrollEnabled) == null) {
		return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
		                                 axes, type);
	}
	return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
}

@Override
public void onNestedScroll(final @NonNull CoordinatorLayout coordinatorLayout,
                           final @NonNull FloatingActionButton child, final @NonNull View target, final int dxConsumed,
                           final int dyConsumed, final int dxUnconsumed, final int dyUnconsumed, final int type) {
	super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
	                     dyUnconsumed, type);

	if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
		child.hide(mVisibilityChangedListener);
	} else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
		child.show();
	}
}
}
