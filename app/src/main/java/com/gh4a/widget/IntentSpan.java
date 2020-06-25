package com.gh4a.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.gh4a.utils.UiUtils;

public class IntentSpan extends ClickableSpan {
    private final Context mContext;
    private final IntentCallback mCallback;

    public interface IntentCallback {
        Intent getIntent(Context context);
    }

    public IntentSpan(final @NonNull Context context, final @NonNull IntentCallback cb) {
        mContext = context;
        mCallback = cb;
    }

    @Override
    public void onClick(final View view) {
        Intent intent = mCallback.getIntent(mContext);
        if (intent != null) {
            mContext.startActivity(intent);
        }
    }

    @Override
    public void updateDrawState(final @NonNull TextPaint ds) {
        ds.setUnderlineText(false);
        ds.setColor(UiUtils.resolveColor(mContext, android.R.attr.textColorLink));
    }
}
