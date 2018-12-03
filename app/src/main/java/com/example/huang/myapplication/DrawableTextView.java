package com.example.huang.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 可以设置右边图片点击事件的TextView
 * Created by huang on 2017/10/21.
 */

public class DrawableTextView extends android.support.v7.widget.AppCompatTextView {
    public DrawableLeftClickListener drawableLeftClickListener;

    public DrawableTextView(Context context) {
        super(context);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    //图片和文字居中
//    @Override
//    protected void onDraw(Canvas canvas) {
//        Drawable[] drawables = getCompoundDrawables();
//        if (null != drawables) {
//            Drawable drawableLeft = drawables[0];
//            Drawable drawableRight = drawables[2];
//            float textWidth = getPaint().measureText(getText().toString());
//            if (null != drawableLeft) {
//                setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
//                float contentWidth = textWidth + getCompoundDrawablePadding() + drawableLeft.getIntrinsicWidth();
//                if (getWidth() - contentWidth > 0) {
//                    canvas.translate((getWidth() - contentWidth - getPaddingRight() - getPaddingLeft()) / 2, 0);
//                }
//            }
//            if (null != drawableRight) {
//                setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
//                float contentWidth = textWidth + getCompoundDrawablePadding() + drawableRight.getIntrinsicWidth();
//                if (getWidth() - contentWidth > 0) {
//                    canvas.translate(-(getWidth() - contentWidth - getPaddingRight() - getPaddingLeft()) / 2, 0);
//                }
//            }
//            if (null == drawableRight && null == drawableLeft) {
//                setGravity(Gravity.CENTER);
//            }
//        }
//        super.onDraw(canvas);
//    }

    public void setDrawableLeftClickListener(DrawableLeftClickListener drawableRightClickListener) {
        this.drawableLeftClickListener = drawableRightClickListener;
    }

    //为了方便,直接写了一个内部类的接口
    public interface DrawableLeftClickListener {
        void onDrawableLeftClickListener(View view);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (drawableLeftClickListener != null) {
                    // getCompoundDrawables获取是一个数组，数组0,1,2,3,对应着左，上，右，下 这4个位置的图片，如果没有就为null
                    Drawable leftDrawable = getCompoundDrawables()[0];
                    //判断的依据是获取点击区域相对于屏幕的x值比我(获取Drawable的边界宽度)小就可以判断点击在Drawable上
                    if (leftDrawable != null && event.getRawX() <= (leftDrawable.getBounds().width())) {
                        drawableLeftClickListener.onDrawableLeftClickListener(this);
                    }
                    //此处不能设置成false,否则drawable不会触发点击事件,如果设置,TextView会处理事件
                    return false;
                }

                break;

        }
        return super.onTouchEvent(event);

    }
}
