package com.gh4a.widget;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ToggleableBottomSheetBehavior<V extends View>
    extends BottomSheetBehavior<V> {
  private boolean mEnabled = true;

  public ToggleableBottomSheetBehavior() {}

  public ToggleableBottomSheetBehavior(final Context context,
                                       final AttributeSet attrs) {
    super(context, attrs);
  }

  public void setEnabled(final boolean enabled) { mEnabled = enabled; }

  @Override
  public boolean onInterceptTouchEvent(final CoordinatorLayout parent,
                                       final V child, final MotionEvent event) {
    return mEnabled && super.onInterceptTouchEvent(parent, child, event);
  }

  @Override
  public boolean onTouchEvent(final CoordinatorLayout parent, final V child,
                              final MotionEvent event) {
    return mEnabled && super.onTouchEvent(parent, child, event);
  }

  @Override
  public boolean
  onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final V child,
                      final View directTargetChild, final View target,
                      final int nestedScrollAxes) {
    return mEnabled &&
        super.onStartNestedScroll(coordinatorLayout, child, directTargetChild,
                                  target, nestedScrollAxes);
  }

  @Override
  public void onNestedPreScroll(final CoordinatorLayout coordinatorLayout,
                                final V child, final View target, final int dx,
                                final int dy, final int[] consumed) {
    if (mEnabled) {
      super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy,
                              consumed);
    }
  }

  @Override
  public void onStopNestedScroll(final CoordinatorLayout coordinatorLayout,
                                 final V child, final View target) {
    if (mEnabled) {
      super.onStopNestedScroll(coordinatorLayout, child, target);
    }
  }

  @Override
  public boolean onNestedPreFling(final CoordinatorLayout coordinatorLayout,
                                  final V child, final View target,
                                  final float velocityX,
                                  final float velocityY) {
    return mEnabled && super.onNestedPreFling(coordinatorLayout, child, target,
                                              velocityX, velocityY);
  }
}
