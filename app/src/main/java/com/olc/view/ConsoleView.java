package com.olc.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.olc.reader.R;

public class ConsoleView extends ScrollView {
    public static final String START_SUFF = "";

    private static final int MAX_CHARACTERS = 1024*1024;
    private static final long TOUCH_TIMEOUT = 4000L;
    private boolean mIsIdle = true;
    private long mLastTouchEventTime;
    private TextView mTextView;

    public ConsoleView(Context context) {
        this(context, null);
    }

    public ConsoleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTextView = new TextView(context);
        this.mTextView.setHorizontallyScrolling(true);
        HorizontalScrollView hsView = new HorizontalScrollView(context);
        hsView.addView(this.mTextView);

        hsView.setHorizontalScrollBarEnabled(false);
        setScrollbarFadingEnabled(false);
        super.addView(hsView);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        this.mTextView.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_small) / metrics.scaledDensity);
        setTextColor(getResources().getColor(R.color.black));
        setBackgroundColor(getResources().getColor(R.color.white));
        clear();
    }

    public void append(CharSequence charSequence) {
        if (!(charSequence.toString().endsWith("\r") | charSequence.toString().endsWith("\n"))) {
            charSequence = charSequence + "\r\n";
        }
        CharSequence text = this.mTextView.getText();
        int localLength = text.length();
        int k;

        if (localLength <= MAX_CHARACTERS) {
            if (this.mIsIdle) {
                this.mTextView.setText(charSequence);
                this.mIsIdle = false;
            } else {
                this.mTextView.append(charSequence);
            }
        } else {

            StringBuilder localStringBuilder = new StringBuilder();
            localStringBuilder.append(text);
            k = localLength - 3686;
            while (k < localLength) {
                if (text.charAt(k) == '\n' || text.charAt(k) == '\r') {
                    int l = k + 1;
                    if (text.charAt(l) == '\n' || text.charAt(l) == '\r') {
                        l = l + 1;
                    }

                    localStringBuilder.replace(0, l, START_SUFF);
                    localStringBuilder.append(charSequence.toString());
                    this.mTextView.setText(localStringBuilder);
                    break;
                }
                k++;
            }
        }


        if (System.currentTimeMillis() - this.mLastTouchEventTime > TOUCH_TIMEOUT) {
            post(new Runnable() {
                public void run() {
                    ConsoleView.this.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    public CharSequence getText() {
        return mTextView.getText();
    }

    public void setText(CharSequence charSequence) {
        this.mTextView.setText(charSequence);
    }

    public void clear() {
        this.mTextView.setText("");
        this.mIsIdle = true;
        this.mLastTouchEventTime = System.currentTimeMillis() - TOUCH_TIMEOUT;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        this.mLastTouchEventTime = System.currentTimeMillis();
        Context localContext = getContext();
        return ((localContext instanceof Activity)) && (((Activity) localContext).onTouchEvent(event)) || super.onTouchEvent(event);
    }

    public void setBackgroundColor(int paramInt) {
        super.setBackgroundColor(paramInt);
        this.mTextView.setBackgroundColor(paramInt);
    }

    public void setTextColor(int color) {
        this.mTextView.setTextColor(color);
    }


}
