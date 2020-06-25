package com.gh4a.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.gh4a.BaseActivity;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.widget.IssueLabelSpan;
import com.meisolsson.githubsdk.model.Label;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class UiUtils {
    public static final LinkMovementMethod CHECKING_LINK_METHOD = new LinkMovementMethod() {
        @Override
        public boolean onTouchEvent(final @NonNull TextView widget,
                                    final @NonNull Spannable buffer, final @NonNull MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(widget.getContext(), R.string.link_not_openable, Toast.LENGTH_LONG)
                .show();
                return true;
            } catch (SecurityException e) {
                // some apps have intent filters set for the VIEW action for
                // internal, non-exported activities
                // -> ignore
                return true;
            }
        }
    };

    public static void hideImeForView(final View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager)
                                 view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showImeForView(final View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager)
                                 view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static int textColorForBackground(final Context context, final int backgroundColor) {
        int red = Color.red(backgroundColor);
        int green = Color.green(backgroundColor);
        int blue = Color.blue(backgroundColor);
        int luminance = Math.round(0.213F * red + 0.715F * green + 0.072F * blue);

        if (luminance >= 128) {
            return ContextCompat.getColor(context, R.color.label_fg_dark);
        }
        return ContextCompat.getColor(context, R.color.label_fg_light);
    }

    public static void trySetListOverscrollColor(final RecyclerView view, final int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RecyclerViewEdgeColorHelper helper =
                (RecyclerViewEdgeColorHelper) view.getTag(R.id.EdgeColorHelper);
            if (helper == null) {
                helper = new RecyclerViewEdgeColorHelper(view);
                view.setTag(R.id.EdgeColorHelper, helper);
            }
            helper.setColor(color);
        }
    }

    @TargetApi(21)
    private static class RecyclerViewEdgeColorHelper implements ViewTreeObserver.OnGlobalLayoutListener {
        private final RecyclerView mView;
        private int mColor;
        private EdgeEffect mTopEffect, mBottomEffect;

        private static Method sTopEnsureMethod, sBottomEnsureMethod;
        private static Field sTopEffectField, sBottomEffectField;

        public RecyclerViewEdgeColorHelper(final RecyclerView view) {
            mView = view;
            mColor = 0;
            mView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
        public void setColor(final int color) {
            mColor = color;
            applyIfPossible();
        }
        @Override
        public void onGlobalLayout() {
            applyIfPossible();
        }

        private void applyIfPossible() {
            if (!ensureStaticMethodsAndFields()) {
                return;
            }
            try {
                Object topEffect = sTopEffectField.get(mView);
                Object bottomEffect = sBottomEffectField.get(mView);
                if (topEffect == null || bottomEffect == null) {
                    sTopEnsureMethod.invoke(mView);
                    sBottomEnsureMethod.invoke(mView);

                    mTopEffect = (EdgeEffect) sTopEffectField.get(mView);
                    mBottomEffect = (EdgeEffect) sBottomEffectField.get(mView);
                } else {
                    mTopEffect = (EdgeEffect) topEffect;
                    mBottomEffect = (EdgeEffect) bottomEffect;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                mTopEffect = mBottomEffect = null;
            }
            applyColor(mTopEffect);
            applyColor(mBottomEffect);
        }

        private void applyColor(final EdgeEffect effect) {
            if (effect != null) {
                final int alpha = Color.alpha(effect.getColor());
                effect.setColor(Color.argb(alpha, Color.red(mColor),
                                           Color.green(mColor), Color.blue(mColor)));
            }
        }

        private boolean ensureStaticMethodsAndFields() {
            if (sTopEnsureMethod != null && sBottomEnsureMethod != null) {
                return true;
            }
            try {
                sTopEnsureMethod = RecyclerView.class.getDeclaredMethod("ensureTopGlow");
                sTopEnsureMethod.setAccessible(true);
                sBottomEnsureMethod = RecyclerView.class.getDeclaredMethod("ensureBottomGlow");
                sBottomEnsureMethod.setAccessible(true);
                sTopEffectField = RecyclerView.class.getDeclaredField("mTopGlow");
                sTopEffectField.setAccessible(true);
                sBottomEffectField = RecyclerView.class.getDeclaredField("mBottomGlow");
                sBottomEffectField.setAccessible(true);
                return true;
            } catch (NoSuchMethodException | NoSuchFieldException e) {
                // ignored
            }
            return false;
        }
    }

    public static int mixColors(final int startColor, final int endColor, final float fraction) {
        // taken from ArgbEvaluator.evaluate
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24)
               | ((startR + (int) (fraction * (endR - startR))) << 16)
               | ((startG + (int) (fraction * (endG - startG))) << 8)
               | ((startB + (int) (fraction * (endB - startB))));
    }

    public static boolean canViewScrollUp(final View view) {
        if (view == null) {
            return false;
        }
        return view.canScrollVertically(-1);
    }

    public static Context makeHeaderThemedContext(final Context context) {
        @StyleRes int themeResId = resolveDrawable(context, R.attr.headerTheme);
        if (themeResId != 0) {
            return new ContextThemeWrapper(context, themeResId);
        }
        return context;
    }

    public static Dialog createProgressDialog(final Context context, final @StringRes int messageResId) {
        View content = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        TextView message = content.findViewById(R.id.message);

        message.setText(messageResId);
        return new AlertDialog.Builder(context)
               .setView(content)
               .create();
    }

    public static int resolveDrawable(final Context context, final @AttrRes int styledAttributeId) {
        TypedArray a = context.obtainStyledAttributes(new int[] {
                           styledAttributeId
                       });
        int resource = a.getResourceId(0, 0);
        a.recycle();
        return resource;
    }

    public static @ColorInt int resolveColor(final Context context, final @AttrRes int styledAttributeId) {
        TypedArray a = context.obtainStyledAttributes(new int[] {
                           styledAttributeId
                       });
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    private static void enqueueDownload(final Context context, final Uri uri, final String fileName,
                                        final String description, final String mimeType, final String mediaType, final boolean wifiOnly) {
        final DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        .setDescription(description)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverRoaming(false);

        if (mediaType != null) {
            request.addRequestHeader("Accept", mediaType);
        }
        if (mimeType != null) {
            request.setMimeType(mimeType);
        }
        if (wifiOnly) {
            request.setAllowedOverMetered(false);
        }

        dm.enqueue(request);
    }

    public static void enqueueDownloadWithPermissionCheck(final BaseActivity activity,
            final String url, final String mimeType, final String fileName,
            final String description, final String mediaType) {
        final ActivityCompat.OnRequestPermissionsResultCallback cb =
        (requestCode, permissions, grantResults) -> {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enqueueDownload(activity, url, mimeType, fileName, description, mediaType);
            }
        };
        activity.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, cb,
                                   R.string.download_permission_rationale);
    }

    private static void enqueueDownload(final Context context, final String url, final String mimeType,
                                        final String fileName, final String description, final String mediaType) {
        if (url == null) {
            return;
        }

        final Uri uri = Uri.parse(url).buildUpon()
                        .appendQueryParameter("access_token", Gh4Application.get().getAuthToken())
                        .build();

        if (!downloadNeedsWarning(context)) {
            enqueueDownload(context, uri, fileName, description, mimeType, mediaType, false);
            return;
        }

        DialogInterface.OnClickListener buttonListener = (dialog, which) -> {
            boolean wifiOnly = which == DialogInterface.BUTTON_NEUTRAL;
            enqueueDownload(context, uri, fileName, description,
                            mimeType, mediaType, wifiOnly);
        };

        new AlertDialog.Builder(context)
        .setTitle(R.string.download_mobile_warning_title)
        .setMessage(R.string.download_mobile_warning_message)
        .setPositiveButton(R.string.download_now_button, buttonListener)
        .setNeutralButton(R.string.download_wifi_button, buttonListener)
        .setNegativeButton(R.string.cancel, null)
        .show();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean downloadNeedsWarning(final Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.isActiveNetworkMetered();
    }

    public static abstract class EmptinessWatchingTextWatcher implements TextWatcher {
        public EmptinessWatchingTextWatcher(final EditText editor) {
            afterTextChanged(editor.getText());
        }
        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        }
        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        }
        @Override
        public void afterTextChanged(final Editable s) {
            onIsEmpty(s == null || s.length() == 0);
        }
        public abstract void onIsEmpty(boolean isEmpty);
    }

    public static class ButtonEnableTextWatcher extends EmptinessWatchingTextWatcher {
        private final View mView;

        public ButtonEnableTextWatcher(final EditText editor, final View view) {
            super(editor);
            mView = view;
            afterTextChanged(editor.getText());
        }

        @Override
        public void onIsEmpty(final boolean isEmpty) {
            if (mView != null) {
                mView.setEnabled(!isEmpty);
            }
        }
    }

    public static CharSequence getSelectedText(final TextView view) {
        int min = 0;
        int max = view.length();

        if (view.isFocused()) {
            int selectionStart = view.getSelectionStart();
            int selectionEnd = view.getSelectionEnd();

            min = Math.max(0, Math.min(selectionStart, selectionEnd));
            max = Math.max(0, Math.max(selectionStart, selectionEnd));
        }

        return view.getText().subSequence(min, max);
    }

    public static abstract class QuoteActionModeCallback implements ActionMode.Callback {
        private final TextView mView;

        public QuoteActionModeCallback(final TextView view) {
            mView = view;
        }

        @Override
        public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
            mode.getMenuInflater().inflate(R.menu.comment_selection_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            if (item.getItemId() != R.id.quote) {
                return false;
            }

            onTextQuoted(UiUtils.getSelectedText(mView));
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(final ActionMode mode) {
        }

        public abstract void onTextQuoted(CharSequence text);
    }

    public static class WhitespaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        @Override
        public int findTokenStart(final CharSequence text, final int cursor) {
            while (cursor > 0 && !Character.isWhitespace(text.charAt(cursor - 1))) {
                cursor--;
            }

            return cursor;
        }

        @Override
        public int findTokenEnd(final CharSequence text, final int cursor) {
            int len = text.length();

            while (cursor < len) {
                if (Character.isWhitespace(text.charAt(cursor))) {
                    return cursor;
                } else {
                    cursor++;
                }
            }

            return len;
        }

        @Override
        public CharSequence terminateToken(final CharSequence text) {
            return text;
        }
    }

    @NonNull
    public static SpannableStringBuilder formatLabelList(final Context context, final List<Label> labels) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (Label label : labels) {
            int pos = builder.length();
            IssueLabelSpan span = new IssueLabelSpan(context, label, true);
            builder.append(label.name());
            builder.setSpan(span, pos, pos + label.name().length(), 0);
        }
        return builder;
    }

    public static int limitViewHeight(final int heightMeasureSpec, final int maxHeight) {
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        switch (heightMode) {
        case View.MeasureSpec.AT_MOST:
        case View.MeasureSpec.EXACTLY:
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                                    Math.min(heightSize, maxHeight), heightMode);
            break;
        case View.MeasureSpec.UNSPECIFIED:
            heightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST);
            break;
        }

        return heightMeasureSpec;
    }

    public static void setMenuItemText(final Context context, final MenuItem item, final String title,
                                       final String subtitle) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(title).append("\n");

        int start = builder.length();
        builder.append(subtitle);

        int secondaryTextColor = UiUtils.resolveColor(context, android.R.attr.textColorSecondary);
        builder.setSpan(new ForegroundColorSpan(secondaryTextColor), start, builder.length(), 0);

        item.setTitle(builder);
    }
}
