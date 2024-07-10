package com.vito.touchtojoystick.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.vito.touchtojoystick.MainActivity

class AutoClickService : AccessibilityService() {
    companion object {
        var instance: AutoClickService? = null
    }
    internal val events = mutableListOf<Event>()

    override fun onInterrupt() {
        // NO-OP
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // NO-OP
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun click(x: Int, y: Int, originX: Int, originY: Int) {
        dispatch(x, y)
//        dispatch(originX, originY)
    }

    fun dispatch(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        val builder = GestureDescription.Builder()
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(path, 10, 10))
            .build()
        dispatchGesture(gestureDescription, null, null)
    }

    fun run(newEvents: MutableList<Event>) {
        events.clear()
        events.addAll(newEvents)
        val builder = GestureDescription.Builder()
        events.forEach { builder.addStroke(it.onEvent()) }
        dispatchGesture(builder.build(), null, null)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
