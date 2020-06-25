package com.gh4a.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gh4a.R;
import com.gh4a.utils.TypefaceCache;
import com.gh4a.utils.UiUtils;

public class StyleableTextView extends AppCompatTextView {
private static final int[] TEXT_APPEARANCE_ATTRS = new int[] {
	android.R.attr.textAppearance
};

private int mTypefaceValue = TypefaceCache.TF_REGULAR;

public StyleableTextView(final Context context) {
	super(context, null);
}

public StyleableTextView(final Context context, final AttributeSet attrs) {
	super(context, attrs);
	initAttributes(context, attrs, 0);
}

public StyleableTextView(final Context context, final AttributeSet attrs, final int defStyle) {
	super(context, attrs, defStyle);
	initAttributes(context, attrs, defStyle);
}

public int getTypefaceValue() {
	return mTypefaceValue;
}

private void initAttributes(final Context context, final AttributeSet attrs, final int defStyle) {
	Resources.Theme theme = context.getTheme();
	TypedArray appearance = null;

	if (attrs != null) {
		TypedArray a = theme.obtainStyledAttributes(attrs, TEXT_APPEARANCE_ATTRS, defStyle, 0);
		int ap = a.getResourceId(0, -1);
		if (ap != -1) {
			appearance = theme.obtainStyledAttributes(ap, R.styleable.StyleableTextView);
		}
		a.recycle();
	}

	if (appearance != null) {
		int n = appearance.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = appearance.getIndex(i);

			switch (attr) {
			case R.styleable.StyleableTextView_ghFont:
				mTypefaceValue = appearance.getInt(attr, -1);
				break;
			}
		}
	}

	TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.StyleableTextView, defStyle, 0);
	int n = a.getIndexCount();

	for (int i = 0; i < n; i++) {
		int attr = a.getIndex(i);

		switch (attr) {
		case R.styleable.StyleableTextView_ghFont:
			mTypefaceValue = a.getInt(attr, -1);
			break;
		}
	}

	a.recycle();

	if (!isInEditMode()) {
		setTypeface(TypefaceCache.getTypeface(mTypefaceValue));
	}
}

@Override
public void setTypeface(final Typeface tf, final int style) {
	if (tf == getTypeface()) {
		setTypeface(TypefaceCache.getTypeface(mTypefaceValue, style));
	} else {
		super.setTypeface(tf, style);
	}
}

// workaround for https://code.google.com/p/android/issues/detail?id=208169
@Override
protected void onAttachedToWindow() {
	super.onAttachedToWindow();
	if (isTextSelectable() && isEnabled()) {
		setEnabled(false);
		setEnabled(true);
	}
}

@Override
public void setText(final CharSequence text, final BufferType type) {
	super.setText(text, type);
	if (getMovementMethod() == LinkMovementMethod.getInstance()) {
		setMovementMethod(UiUtils.CHECKING_LINK_METHOD);
	}
}

// workaround for https://code.google.com/p/android/issues/detail?id=191430
@Override
public boolean dispatchTouchEvent(final MotionEvent event) {
	int startSelection = getSelectionStart();
	int endSelection = getSelectionEnd();
	if (startSelection != endSelection && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
		final CharSequence text = getText();
		setText(null);
		setText(text);
	}
	return super.dispatchTouchEvent(event);
}
}
