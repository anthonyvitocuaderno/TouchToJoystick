package com.vito.touchtojoystick.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.cardview.widget.CardView
import com.vito.touchtojoystick.R
import com.vito.touchtojoystick.TouchOverlayFrameLayout
import kotlin.math.roundToInt

@SuppressLint("ClickableViewAccessibility")
class Window(
    // declaring required variables
    private val context: Context,
) {
    private val mView: View
    private val clickArea: TouchOverlayFrameLayout
    private var mParams: WindowManager.LayoutParams? = null
    private val mWindowManager: WindowManager
    private val layoutInflater: LayoutInflater
    var lastDirection: TouchDirection = TouchDirection.c
    var str = 100
    val handler = Handler()
    var tapX: Float? = null
    var tapY: Float? = null

    val debounceTime = 300
    init {
        // set the layout parameters of the window
        mParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT, // Display it on top of other application windows
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Don't let it grab the input focus
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Make the underlying application window visible
            // through any transparent parts
            PixelFormat.TRANSLUCENT,
        )

        // getting a LayoutInflater
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        clickArea = layoutInflater.inflate(R.layout.click_area, null) as TouchOverlayFrameLayout
        var lastClickTime: Long = 0
        clickArea.setOnTouchListener { v, event ->
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return@setOnTouchListener true
            val coordinates = IntArray(2)
            clickArea.getLocationOnScreen(coordinates)
            val x = coordinates[0] + event.x
            val y = coordinates[1] + event.y

            Log.d("TEST", "Auto-clicking $x,$y")
            tapX?.toInt()
                ?.let {
                    tapY?.toInt()
                        ?.let { it1 -> AutoClickService.instance?.click(x.toInt(), y.toInt(), it, it1) }
                }
            lastClickTime = SystemClock.elapsedRealtime()
            true
        }

        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.popup_window, null)
        val bl = mView.findViewById<CardView>(R.id.bl)
        val b = mView.findViewById<CardView>(R.id.b)
        val br = mView.findViewById<CardView>(R.id.br)
        val tl = mView.findViewById<CardView>(R.id.tl)
        val t = mView.findViewById<CardView>(R.id.t)
        val tr = mView.findViewById<CardView>(R.id.tr)
        val l = mView.findViewById<CardView>(R.id.l)
        val r = mView.findViewById<CardView>(R.id.r)
        val buttons = listOf<CardView>(
            bl,
            b,
            br,
            tl,
            t,
            tr,
            l,
            r,
        )
        mView.setOnTouchListener { v, event ->
            tapX = event.rawX
            tapY = event.rawY
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val x = event.x
                val y = v.height - event.y
                val lowerLimitX = v.width * 0.087
                val lowerLimitY = v.height * 0.087
                val xSide = v.width * 0.824 / 3
                val ySide = v.height * 0.824 / 3
                str = (event.pressure * 100).roundToInt()

                if (x > lowerLimitX && x < lowerLimitX + xSide) {
                    if (y > lowerLimitY && y < lowerLimitY + ySide) {
                        lastDirection = TouchDirection.bl
                    } else if (y > lowerLimitY + ySide && y < lowerLimitY + ySide * 2) {
                        lastDirection = TouchDirection.l
                    } else if (y > lowerLimitY + ySide * 2 && y < lowerLimitY + ySide * 3) {
                        lastDirection = TouchDirection.tl
                    }
                } else if (x > lowerLimitX + xSide && x < lowerLimitX + xSide * 2) {
                    if (y > lowerLimitY && y < lowerLimitY + ySide) {
                        lastDirection = TouchDirection.b
                    } else if (y > lowerLimitY + ySide && y < lowerLimitY + ySide * 2) {
                        lastDirection = TouchDirection.c
                    } else if (y > lowerLimitY + ySide * 2 && y < lowerLimitY + ySide * 3) {
                        lastDirection = TouchDirection.t
                    }
                } else if (x > lowerLimitX + xSide * 2 && x < lowerLimitX + xSide * 3) {
                    if (y > lowerLimitY && y < lowerLimitY + ySide) {
                        lastDirection = TouchDirection.br
                    } else if (y > lowerLimitY + ySide && y < lowerLimitY + ySide * 2) {
                        lastDirection = TouchDirection.r
                    } else if (y > lowerLimitY + ySide * 2 && y < lowerLimitY + ySide * 3) {
                        lastDirection = TouchDirection.tr
                    }
                } else {
                    lastDirection = TouchDirection.c
                }
            }
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                Log.d("TEST", "Touch ACTION_CANCEL or action_up ended ${event.action}")
            }
            buttons.forEach {
                it.alpha = 0.5f
            }
            when (lastDirection) {
                TouchDirection.t -> t.alpha = 1f
                TouchDirection.tr -> tr.alpha = 1f
                TouchDirection.r -> r.alpha = 1f
                TouchDirection.br -> br.alpha = 1f
                TouchDirection.b -> b.alpha = 1f
                TouchDirection.bl -> bl.alpha = 1f
                TouchDirection.l -> l.alpha = 1f
                TouchDirection.tl -> tl.alpha = 1f
                else -> {
                }
            }
            Log.d("TEST", "Direction $lastDirection")
            true
        }

        mParams!!.gravity = Gravity.LEFT or Gravity.BOTTOM
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun t(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX
        val y = parent.pivotY + parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    private fun tr(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX + parent.width / 2 * 0.9F * str / 100F
        val y = parent.pivotY + parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    private fun r(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX + parent.width / 2 * 0.9F * str / 100F
        val y = parent.pivotY // + parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    private fun br(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX + parent.width / 2 * 0.9F * str / 100F
        val y = parent.pivotY - parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    private fun b(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX // + parent.width / 2 * 0.9F * str / 100F
        val y = parent.pivotY - parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    private fun bl(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX - parent.width / 2 * 0.9F * str / 100F
        val y = parent.pivotY - parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    private fun l(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX - parent.width / 2 * 0.9F * str / 100F
        val y = parent.pivotY // + parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    private fun tl(str: Int) {
        val parent = clickArea
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = parent.pivotX - parent.width / 2 * 0.9F * str / 100F
        val y = parent.pivotY + parent.height / 2 * 0.9F * str / 100F
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState,
        )

        parent.dispatchTouchEvent(motionEvent)
    }

    fun open() {
        try {
            // check if the view is already
            // inflated or present in the window
            if (mView.windowToken == null) {
                if (mView.parent == null) {
                    Log.d("TEST", "height: ${mWindowManager.currentWindowMetrics.bounds.height()}")
                    Log.d("TEST", "width: ${mWindowManager.currentWindowMetrics.bounds.width()}")

                    mWindowManager.addView(mView, mParams)
                    val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT, // Display it on top of other application windows
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Don't let it grab the input focus
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Make the underlying application window visible
                        // through any transparent parts
                        PixelFormat.TRANSLUCENT,
                    )
                    params.gravity = Gravity.CENTER
                    val w = mWindowManager.maximumWindowMetrics.bounds.width()
                    val h = mWindowManager.maximumWindowMetrics.bounds.height()
                    val r = (if (w > h) h else w) / 2
                    params.width = r
                    params.height = r
                    mWindowManager.addView(clickArea, params)

                    autoClick()
                }
            }
        } catch (e: Exception) {
            Log.d("Error1", e.toString())
        }
    }

    private fun autoClick() {
        if (lastDirection != TouchDirection.c) {
            Log.d("TEST", "Auto-clicking...$lastDirection")
        }
        handler.postDelayed({
            when (lastDirection) {
                TouchDirection.t -> t(str)
                TouchDirection.tr -> tr(str)
                TouchDirection.r -> r(str)
                TouchDirection.br -> br(str)
                TouchDirection.b -> b(str)
                TouchDirection.bl -> bl(str)
                TouchDirection.l -> l(str)
                TouchDirection.tl -> tl(str)
                else -> {
                    // nothing to do. center
                }
            }
            autoClick()
        }, debounceTime.toLong())
    }

    fun close() {
        try {
            // remove the view from the window
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(mView)
            // invalidate the view
            mView.invalidate()
            // remove all views
            (mView.parent as ViewGroup).removeAllViews()
            // remove the view from the window
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(clickArea)
            // invalidate the view
            clickArea.invalidate()
            // remove all views
            (clickArea.parent as ViewGroup).removeAllViews()

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (e: Exception) {
            Log.d("Error2", e.toString())
        }
    }
}

enum class TouchDirection {
    tl, t, tr,
    l, c, r,
    bl, b, br,
}
