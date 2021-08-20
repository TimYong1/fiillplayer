package com.fiill.fiillplayer.streamplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.fiill.fiillplayer.widgets.MeasureHelper;

/**
 * class to set the video view's size after scale the aspect ratio
 */

public class ScalableTextureView extends TextureView implements IScalableDisplay{
    private MeasureHelper measureHelper;

    public ScalableTextureView(Context context) {
        super(context);
        init();
    }

    public ScalableTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void setAspectRatio(int aspectRatio) {
        measureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            measureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    public ScalableTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        measureHelper = new MeasureHelper(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureHelper.doMeasure(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
    }
}
