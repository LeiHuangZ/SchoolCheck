package com.example.huang.myapplication.certificate;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * 自定义ImageView
 *
 * @author huang
 * @date 2017/11/3
 */

public class MyView extends android.support.v7.widget.AppCompatImageView {
    public MyView(Context context) {
        super(context);
    }
    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
