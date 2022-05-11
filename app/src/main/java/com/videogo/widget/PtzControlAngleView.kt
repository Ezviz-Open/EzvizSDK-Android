package com.videogo.widget

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View

import com.videogo.util.LogUtil
import com.videogo.util.Utils
import ezviz.ezopensdk.R

/**
 * 角度尺
 * Created by huchenxi on 2017/4/27.
 */

class PtzControlAngleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {


    private val linePaint: Paint = Paint()
    private val ballPaint: Paint = Paint()
    private val lineLength: Float
    private val ballRadius: Float

    private var start = 0
    private var end = 0
    private var current = 0

    private var style: Int
    private var horizontalGradient: LinearGradient? = null
    private var path = Path()

    //水平无尽模式
    private var horizontalInfinityMode = false

    init {
        linePaint.color = Color.WHITE
        linePaint.isAntiAlias = true
        linePaint.strokeWidth = Utils.dip2px(context, 1f).toFloat()
        linePaint.style = Paint.Style.STROKE

        linePaint.isAntiAlias = true
        ballPaint.color = ContextCompat.getColor(context, R.color.color_648FFC)
        ballPaint.style = Paint.Style.FILL

        lineLength = Utils.dip2px(context, 3f).toFloat()
        ballRadius = Utils.dip2px(context, 3f).toFloat()

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.LiveplayPtzControlAngleView)
        style = a.getInt(R.styleable.LiveplayPtzControlAngleView_liveplay_angle_style, 4)
        a.recycle()

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var angle = 180f
        if (style == 1 || style == 2) {
            if(style == 2){
                if (end != start) {
                    angle = (end - current).toFloat() / (end - start) * 360
                }
                val step = measuredHeight / 10
                canvas.drawLine(measuredWidth - 30f, step.toFloat(), measuredWidth - 30f, (measuredHeight - step).toFloat(), linePaint)
                val offset = angle * step * 2 / 90f

                for (i in 0..8) {
                    val y = (i + 1) * step.toFloat()
                    if (i % 2 == 0) {
                        canvas.drawLine(measuredWidth - 50f, y, measuredWidth - 30f, y, linePaint)
                    } else {
                        canvas.drawLine(measuredWidth - 40f, y, measuredWidth - 30f, y, linePaint)
                    }
                }
                path.reset()
                path.moveTo(measuredWidth - 60f, offset + step)
                path.lineTo(measuredWidth - 94f, offset + step - 20)
                path.lineTo(measuredWidth - 94f, offset + step + 20)
                path.close()
                canvas.drawPath(path, ballPaint)
            }else{
                if (end != start) {
                    angle = (end - current).toFloat() / (end - start) * 360
                }
                val step = measuredHeight / 10
                canvas.drawLine(30f, step.toFloat(), 30f, (measuredHeight - step).toFloat(), linePaint)
                val offset = angle * step * 2 / 90f

                for (i in 0..8) {
                    val y = (i + 1) * step.toFloat()
                    if (i % 2 == 0) {
                        canvas.drawLine(30f, y, 50f, y, linePaint)
                    } else {
                        canvas.drawLine(30f, y, 40f, y, linePaint)
                    }
                }
                path.reset()
                path.moveTo(60f, offset + step)
                path.lineTo(94f, offset + step - 20)
                path.lineTo(94f, offset + step + 20)
                path.close()
                canvas.drawPath(path, ballPaint)
            }
        } else {
            if (horizontalGradient == null) {
                horizontalGradient = LinearGradient(0f, measuredHeight - 30f, measuredWidth.toFloat(), measuredHeight - 30f, intArrayOf(Color.parseColor("#00FFFFFF"), Color.parseColor("#FFFFFFFF"), Color.parseColor("#FFFFFFFF"), Color.parseColor("#00FFFFFF")), floatArrayOf(0f, 0.1f, 0.9f, 1.0f), Shader.TileMode.CLAMP)
            }
            linePaint.shader = horizontalGradient
            if (end != start) {
                angle = (current - start).toFloat() / (end - start) * 360
            }
            if (horizontalInfinityMode) {
                canvas.drawLine(0f, measuredHeight - 30f, measuredWidth.toFloat(), measuredHeight - 30f, linePaint)

                val step = measuredWidth / 10
                val offset = (angle % 90) / 90f * step * 2
                for (i in 0..11) {
                    val x = i * step - offset
                    if (i % 2 == 0) {
                        canvas.drawLine(x, measuredHeight - 50f, x, measuredHeight - 30f, linePaint)
                    } else {
                        canvas.drawLine(x, measuredHeight - 40f, x, measuredHeight - 30f, linePaint)
                    }
                }
                path.reset()
                path.moveTo(measuredWidth / 2f, measuredHeight - 60f)
                path.lineTo(measuredWidth / 2f - 20, measuredHeight - 94f)
                path.lineTo(measuredWidth / 2f + 20, measuredHeight - 94f)
                path.close()
                canvas.drawPath(path, ballPaint)
            } else {
                val step = measuredWidth / 10
                canvas.drawLine(step.toFloat(), measuredHeight - 30f, (measuredWidth - step).toFloat(), measuredHeight - 30f, linePaint)
                val offset = angle * step * 2 / 90f
                for (i in 0..8) {
                    val x = (i + 1) * step.toFloat()
                    if (i % 2 == 0) {
                        canvas.drawLine(x, measuredHeight - 50f, x, measuredHeight - 30f, linePaint)
                    } else {
                        canvas.drawLine(x, measuredHeight - 40f, x, measuredHeight - 30f, linePaint)
                    }
                }
                path.reset()
                path.moveTo(offset + step, measuredHeight - 60f)
                path.lineTo(offset + step - 20, measuredHeight - 94f)
                path.lineTo(offset + step + 20, measuredHeight - 94f)
                path.close()
                canvas.drawPath(path, ballPaint)
            }
        }
    }


    /***
     * 设置角度
     * @param start
     * @param end
     * @param current
     */
    fun setAngle(start: Int, end: Int, current: Int) {

        this.start = start
        this.end = end
        this.current = current

        LogUtil.d("PtzControlAngleView", "style = $style , setAngle start = $start, end = $end, current = $current")

        invalidate()

    }

    /***
     * 设置角度尺的模式
     */
    fun setAngleMode(infinity: Boolean) {
        this.horizontalInfinityMode = infinity
    }


}
