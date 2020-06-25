package com.gh4a.widget;

import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.MotionEvent;
import android.view.View;

public class ToggleableAppBarLayoutBehavior extends AppBarLayout.Behavior {
    private boolean mEnabled = true;

    public void setEnabled(final boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean onStartNestedScroll(final @NonNull CoordinatorLayout parent,
                                       final @NonNull AppBarLayout child, final @NonNull View directTargetChild,
                                       final @NonNull View target, final int nestedScrollAxes, final int type) {
        return mEnabled && super.onStartNestedScroll(parent, child, directTargetChild, target,
                nestedScrollAxes, type);
    }

    @Override
    public void onNestedPreScroll(final @NonNull CoordinatorLayout coordinatorLayout,
                                  final @NonNull AppBarLayout child, final @NonNull View target,
                                  final int dx, final int dy, final @NonNull int[] consumed, final int type) {
        if (mEnabled) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        }
    }

    @Override
    public void onNestedScroll(final @NonNull CoordinatorLayout coordinatorLayout,
                               final @NonNull AppBarLayout child, final @NonNull View target,
                               final int dxConsumed, final int dyConsumed, final int dxUnconsumed, final int dyUnconsumed, final int type) {
        if (mEnabled) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                                 dxUnconsumed, dyUnconsumed, type);
        }
    }

    @Override
    public void onStopNestedScroll(final @NonNull CoordinatorLayout coordinatorLayout,
                                   final @NonNull AppBarLayout child, final @NonNull View target, final int type) {
        if (mEnabled) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final CoordinatorLayout parent,
                                         final AppBarLayout child, final MotionEvent ev) {
        return mEnabled && super.onInterceptTouchEvent(parent, child, ev);
    }
}
