package com.example.heritage.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/** ImageView that keeps a 16:9 shape at any width, matching the bundled photos. */
public class AspectImageView extends AppCompatImageView {

    public AspectImageView(Context context) {
        super(context);
    }

    public AspectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * 9 / 16;
        setMeasuredDimension(width, height);
    }
}
