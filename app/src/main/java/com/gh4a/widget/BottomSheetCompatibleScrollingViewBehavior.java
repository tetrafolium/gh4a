package com.gh4a.widget;

import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

public class BottomSheetCompatibleScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {

@Override
public boolean onRequestChildRectangleOnScreen(final CoordinatorLayout parent, final View child,
                                               final Rect rectangle, final boolean immediate) {
	return true;
}
}
