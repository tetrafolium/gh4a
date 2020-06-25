package com.gh4a.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.style.LineBackgroundSpan;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;

public class EllipsizeLineSpan
    extends ReplacementSpan implements LineBackgroundSpan, LineHeightSpan {
  private final Rect mClipRect = new Rect();
  private final int mBottomMargin;

  public EllipsizeLineSpan(final int bottomMargin) {
    mBottomMargin = bottomMargin;
  }

  @Override
  public void drawBackground(final Canvas c, final Paint p, final int left,
                             final int right, final int top, final int baseline,
                             final int bottom, final CharSequence text,
                             final int start, final int end, final int lnum) {
    c.getClipBounds(mClipRect);
  }

  @Override
  public int getSize(final @NonNull Paint paint, final CharSequence text,
                     final int start, final int end,
                     final Paint.FontMetricsInt fm) {
    if (fm != null) {
      paint.getFontMetricsInt(fm);
    }
    int textWidth = (int)Math.ceil(paint.measureText(text, start, end));
    return Math.min(textWidth, mClipRect.width());
  }

  @Override
  public void draw(final @NonNull Canvas canvas, final CharSequence text,
                   final int start, final int end, final float x, final int top,
                   final int y, final int bottom, final @NonNull Paint paint) {
    float textWidth = paint.measureText(text, start, end);

    if (x + (int)Math.ceil(textWidth) < mClipRect.right) {
      // text fits
      canvas.drawText(text, start, end, x, y, paint);
    } else {
      float ellipsisWidth = paint.measureText("\u2026");
      // move 'end' to the ellipsis point
      end = start + paint.breakText(text, start, end, true,
                                    mClipRect.right - x - ellipsisWidth, null);
      canvas.drawText(text, start, end, x, y, paint);
      canvas.drawText("\u2026", x + paint.measureText(text, start, end), y,
                      paint);
    }
  }

  @Override
  public void chooseHeight(final CharSequence text, final int start,
                           final int end, final int spanstartv, final int v,
                           final Paint.FontMetricsInt fm) {
    fm.descent += mBottomMargin;
    fm.bottom += mBottomMargin;
  }
}
