package com.example.ta.eyecontrolledtransitionsystem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by ta on 2015/09/28.
 */
public class MyWebView extends WebView {

    private OnTouchEventCallback onTouchEventCallback;

    public MyWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(Context context) {
        super(context);
    }

    public void setOnTouchEventCallback(final OnTouchEventCallback onTouchEventCallback){
        this.onTouchEventCallback = onTouchEventCallback;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(onTouchEventCallback != null) onTouchEventCallback.onTouchStateChanged(event.getAction());
        return super.onTouchEvent(event);
    }


    public static interface OnTouchEventCallback
    {
        public void onTouchStateChanged(int state);
    }
}

