package com.videogo.ui.realplay

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.hypot

/**
 * 鱼眼播放器触摸事件监听处理（支持拖动、两指缩放）
 */
abstract class FecPlayTouchListener(context: Context) : View.OnTouchListener {

    private val vc = ViewConfiguration.get(context)
    private val touchSlop = vc.scaledTouchSlop

    private var lastMotionX: Float = 0f
    private var lastMotionY: Float = 0f
    private var baseDistance: Float = 0f
    private var activePointerId: Int = 0
    private var isDragging: Boolean = false
    private var isScaling: Boolean = false

    private var isDragConfirm: Boolean = false
    private var isScaleConfirm: Boolean = false

    /**
     * @return 只在事件被放大和拖动事件消耗才返回true，其他时候返回false，方便Touch事件嵌套处理
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                lastMotionX = event.x
                lastMotionY = event.y
                onDown(event.x, event.y)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                lastMotionX = event.x
                lastMotionY = event.y
                activePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = event.findPointerIndex(activePointerId)
                if (activePointerIndex == -1) {
                    return false
                }

                // 多指触摸时，对缩放优先处理
                if (event.pointerCount > 1) {
                    var sumX = 0f
                    var sumY = 0f
                    for (i in 0 until event.pointerCount) {
                        sumX += event.getX(i)
                        sumY += event.getY(i)
                    }
                    val centerX = sumX / event.pointerCount
                    val centerY = sumY / event.pointerCount

                    sumX = 0f
                    sumY = 0f
                    for (i in 0 until event.pointerCount) {
                        sumX += abs(event.getX(i) - centerX)
                        sumY += abs(event.getY(i) - centerY)
                    }
                    val devX = sumX / event.pointerCount
                    val devY = sumY / event.pointerCount

                    val distance = hypot((devX * 2).toDouble(), (devY * 2).toDouble()).toFloat()
                    if (!isScaling && distance > touchSlop) {
                        isScaling = true
                        baseDistance = distance
                        onStartScale(distance)
                    }

                    if (isScaling) {
                        isScaleConfirm = onScale(distance / baseDistance, distance, centerX, centerY)
                        return isScaleConfirm
                    }
                }

                val x = event.getX(activePointerIndex)
                val y = event.getY(activePointerIndex)
                var deltaX = lastMotionX - x
                var deltaY = lastMotionY - y

                if (!isDragging && (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop)) {
                    isDragging = true
                    onStartDrag(lastMotionX, lastMotionY)

                    if (abs(deltaX) > touchSlop) {
                        if (deltaX > 0) {
                            deltaX -= touchSlop
                        } else {
                            deltaX += touchSlop
                        }
                    }

                    if (abs(deltaY) > touchSlop) {
                        if (deltaY > 0) {
                            deltaY -= touchSlop
                        } else {
                            deltaY += touchSlop
                        }
                    }
                }

                if (isDragging) {
                    lastMotionX = x
                    lastMotionY = y
                    isDragConfirm = onDrag(-deltaX, -deltaY, x, y)
                    return isDragConfirm
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                var fromScaleDrag = false

                if (isScaling) {
                    fromScaleDrag = isScaleConfirm
                    isScaleConfirm = false
                    isScaling = false
                }

                if (isDragging) {
                    fromScaleDrag = isDragConfirm
                    isDragConfirm = false
                    isDragging = false
                }

                onUp(event.x, event.y)
                return fromScaleDrag
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onPointerUp(event)
            }
        }

        return true
    }

    private fun onPointerUp(event: MotionEvent) {
        val pointerIndex =
            event.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = event.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            val newPointerIndex = event.pointerCount - 2
            lastMotionX = event.getX(newPointerIndex)
            lastMotionY = event.getY(newPointerIndex)
            activePointerId = event.getPointerId(newPointerIndex)
        } else {
            val activePointerIndex = event.findPointerIndex(activePointerId)
            if (activePointerIndex == -1) {
                return
            }

            lastMotionX = event.getX(activePointerIndex)
            lastMotionY = event.getY(activePointerIndex)
        }
    }

    abstract fun onStartDrag(x: Float, y: Float)

    abstract fun onDrag(deltaX: Float, deltaY: Float, x: Float, y: Float): Boolean

    abstract fun onDown(x: Float, y: Float)

    abstract fun onStartScale(distance: Float)

    abstract fun onScale(scale: Float, distance: Float, centerX: Float, centerY: Float): Boolean

    abstract fun onUp(x: Float, y: Float)
}