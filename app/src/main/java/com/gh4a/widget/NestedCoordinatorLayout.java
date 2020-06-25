package com.gh4a.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

// From: http://stackoverflow.com/a/34954120
public class NestedCoordinatorLayout extends CoordinatorLayout implements NestedScrollingChild2 {
private final NestedScrollingChildHelper mNestedScrollingChildHelper;

private final int[] mParentOffsetInWindow = new int[2];
private final int[] mParentScrollConsumed = new int[2];

public NestedCoordinatorLayout(final Context context) {
	this(context, null, 0);
}

public NestedCoordinatorLayout(final Context context, final AttributeSet attrs) {
	this(context, attrs, 0);
}

public NestedCoordinatorLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
	super(context, attrs, defStyleAttr);
	mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
	setNestedScrollingEnabled(true);
}

@Override
public void setNestedScrollingEnabled(final boolean enabled) {
	mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
}

@Override
public boolean isNestedScrollingEnabled() {
	return mNestedScrollingChildHelper.isNestedScrollingEnabled();
}

@Override
public boolean startNestedScroll(final int axes, final int type) {
	return mNestedScrollingChildHelper.startNestedScroll(axes, type);
}

@Override
public void stopNestedScroll(final int type) {
	mNestedScrollingChildHelper.stopNestedScroll(type);
}

@Override
public boolean hasNestedScrollingParent(final int type) {
	return mNestedScrollingChildHelper.hasNestedScrollingParent(type);
}

@Override
public boolean dispatchNestedScroll(final int dxConsumed, final int dyConsumed, final int dxUnconsumed,
                                    final int dyUnconsumed, final int[] offsetInWindow, final int type) {
	return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
	                                                        dxUnconsumed, dyUnconsumed, offsetInWindow, type);
}

@Override
public boolean dispatchNestedPreScroll(final int dx, final int dy, final int[] consumed,
                                       final int[] offsetInWindow, final int type) {
	return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed,
	                                                           offsetInWindow, type);
}

@Override
public boolean dispatchNestedFling(final float velocityX, final float velocityY, final boolean consumed) {
	return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
}

@Override
public boolean dispatchNestedPreFling(final float velocityX, final float velocityY) {
	return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
}

@Override
public void onNestedScrollAccepted(final View child, final View target, final int axes, final int type) {
	super.onNestedScrollAccepted(child, target, axes, type);

	// Dispatch up to the nested parent
	startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL, type);
}

@Override
public void onStopNestedScroll(final View target, final int type) {
	super.onStopNestedScroll(target, type);
	stopNestedScroll(type);
}

@Override
public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed, final int dxUnconsumed,
                           final int dyUnconsumed, final int type) {
	super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
	dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
	                     mParentOffsetInWindow, type);
}

@Override
public void onNestedPreScroll(final View target, final int dx, final int dy, final int[] consumed, final int type) {
	super.onNestedPreScroll(target, dx, dy, consumed, type);

	final int[] parentConsumed = mParentScrollConsumed;
	if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null, type)) {
		consumed[0] += parentConsumed[0];
		consumed[1] += parentConsumed[1];
	}
}
}
