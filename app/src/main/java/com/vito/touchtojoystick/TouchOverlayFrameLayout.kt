package com.vito.touchtojoystick

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout

class TouchOverlayFrameLayout(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        super.onInterceptTouchEvent(ev)
        Log.d("TEST", "Intercept test")
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("TEST", "Intercept test 2")
//        return super.onTouchEvent(event)
        return false
    }
}
