package com.gh4a.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.activities.WebViewerActivity;
import com.gh4a.utils.StringUtils;

public class MarkdownPreviewWebView extends WebView implements NestedScrollingChild2 {
private final NestedScrollingChildHelper mChildHelper;
private final int[] mScrollOffset = new int[2];
private final int[] mScrollConsumed = new int[2];
private int mNestedOffsetY;
private int mLastY;
private final String mCssTheme;

public MarkdownPreviewWebView(final Context context) {
	this(context, null);
}

public MarkdownPreviewWebView(final Context context, final AttributeSet attrs) {
	this(context, attrs, android.R.attr.webViewStyle);
}

public MarkdownPreviewWebView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
	super(context, attrs, defStyleAttr);

	mChildHelper = new NestedScrollingChildHelper(this);
	setNestedScrollingEnabled(true);

	mCssTheme = Gh4Application.THEME == R.style.DarkTheme
	            ? WebViewerActivity.DARK_CSS_THEME : WebViewerActivity.LIGHT_CSS_THEME;

	if (!isInEditMode()) {
		initWebViewSettings(getSettings());
		setContent("");
	}
}

public void setEditText(final EditText editor) {
	editor.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
			}

			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			        setContent(s.toString());
			}

			@Override
			public void afterTextChanged(final Editable s) {
			}
		});
}

@Override
public boolean onTouchEvent(final MotionEvent ev) {
	boolean result;

	MotionEvent event = MotionEvent.obtain(ev);
	final int action = event.getActionMasked();
	if (action == MotionEvent.ACTION_DOWN) {
		mNestedOffsetY = 0;
	}

	int eventY = (int) event.getY();
	event.offsetLocation(0, mNestedOffsetY);

	switch (action) {
	case MotionEvent.ACTION_DOWN:
		mLastY = eventY;
		startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
		result = super.onTouchEvent(event);
		break;
	case MotionEvent.ACTION_MOVE:
		int deltaY = mLastY - eventY;
		if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed,
		                            mScrollOffset, ViewCompat.TYPE_TOUCH)) {
			deltaY -= mScrollConsumed[1];
			mLastY = eventY - mScrollOffset[1];
			event.offsetLocation(0, -mScrollOffset[1]);
			mNestedOffsetY += mScrollOffset[1];
		}

		result = super.onTouchEvent(event);

		if (dispatchNestedScroll(0, mScrollOffset[1], 0, deltaY,
		                         mScrollOffset, ViewCompat.TYPE_TOUCH)) {
			event.offsetLocation(0, mScrollOffset[1]);
			mNestedOffsetY += mScrollOffset[1];
			mLastY -= mScrollOffset[1];
		}
		break;
	default:
		stopNestedScroll(ViewCompat.TYPE_TOUCH);
		result = super.onTouchEvent(event);
		break;
	}

	return result;
}

@Override
public void setNestedScrollingEnabled(final boolean enabled) {
	mChildHelper.setNestedScrollingEnabled(enabled);
}

@Override
public boolean isNestedScrollingEnabled() {
	return mChildHelper.isNestedScrollingEnabled();
}

@Override
public boolean startNestedScroll(final int axes, final int type) {
	return mChildHelper.startNestedScroll(axes, type);
}

@Override
public void stopNestedScroll(final int type) {
	mChildHelper.stopNestedScroll(type);
}

@Override
public boolean hasNestedScrollingParent(final int type) {
	return mChildHelper.hasNestedScrollingParent(type);
}

@Override
public boolean dispatchNestedScroll(final int dxConsumed, final int dyConsumed,
                                    final int dxUnconsumed, final int dyUnconsumed,  final int[] offsetInWindow, final int type) {
	return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
	                                         dxUnconsumed, dyUnconsumed, offsetInWindow, type);
}

@Override
public boolean dispatchNestedPreScroll(final int dx, final int dy, final int[] consumed,
                                       final int[] offsetInWindow, final int type) {
	return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
}

@Override
public boolean dispatchNestedFling(final float velocityX, final float velocityY, final boolean consumed) {
	return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
}

@Override
public boolean dispatchNestedPreFling(final float velocityX, final float velocityY) {
	return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
}

@SuppressLint("SetJavaScriptEnabled")
private void initWebViewSettings(final WebSettings s) {
	s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
	s.setLoadsImagesAutomatically(true);
	s.setJavaScriptEnabled(true);
	s.setUseWideViewPort(false);
}

private void setContent(final String content) {
	String html = generateMarkdownHtml(StringUtils.toBase64(content), mCssTheme);
	loadDataWithBaseURL("file:///android_asset/", html, null, "utf-8", null);
}

private String generateMarkdownHtml(final String base64Data, final String cssTheme) {
	StringBuilder content = new StringBuilder();
	content.append("<html><head>");
	writeScriptInclude(content, "showdown");
	writeScriptInclude(content, "base64");
	writeCssInclude(content, "markdown", cssTheme);
	writeCssInclude(content, "mdpreview", cssTheme);
	content.append("</head>");

	content.append("<body>");
	content.append("<div id='content'></div>");

	content.append("<script>");
	content.append("var text = Base64.decode('");
	content.append(base64Data);
	content.append("');\n");
	content.append("var converter = new showdown.Converter();\n");
	content.append("converter.setFlavor('github');\n");
	content.append("var html = converter.makeHtml(text);\n");
	content.append("document.getElementById('content').innerHTML = html;");
	content.append("</script>");

	content.append("</body></html>");

	return content.toString();
}

private static void writeScriptInclude(final StringBuilder builder, final String scriptName) {
	builder.append("<script src='file:///android_asset/");
	builder.append(scriptName);
	builder.append(".js' type='text/javascript'></script>");
}

private static void writeCssInclude(final StringBuilder builder, final String cssType, final String cssTheme) {
	builder.append("<link href='file:///android_asset/");
	builder.append(cssType);
	builder.append("-");
	builder.append(cssTheme);
	builder.append(".css' rel='stylesheet' type='text/css'/>");
}
}
