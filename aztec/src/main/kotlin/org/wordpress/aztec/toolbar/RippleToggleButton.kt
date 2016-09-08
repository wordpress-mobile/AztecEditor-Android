package org.wordpress.aztec.toolbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.Toast
import android.widget.ToggleButton
import org.wordpress.aztec.R

class RippleToggleButton : ToggleButton, OnLongClickListener {
    private var mHalfWidth: Float = 0.toFloat()
    private var mAnimationIsRunning = false
    private var mTimer = 0
    private var mFillPaint: Paint? = null
    private var mStrokePaint: Paint? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        if (isInEditMode) {
            return
        }

        setOnLongClickListener(this)

        val rippleColor = resources.getColor(R.color.format_bar_ripple_animation)

        mFillPaint = Paint()
        mFillPaint!!.isAntiAlias = true
        mFillPaint!!.color = rippleColor
        mFillPaint!!.style = Paint.Style.FILL
        mFillPaint!!.alpha = FILL_INITIAL_OPACITY

        mStrokePaint = Paint()
        mStrokePaint!!.isAntiAlias = true
        mStrokePaint!!.color = rippleColor
        mStrokePaint!!.style = Paint.Style.STROKE
        mStrokePaint!!.strokeWidth = 2f
        mStrokePaint!!.alpha = STROKE_INITIAL_OPACITY

        setWillNotDraw(false)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (mAnimationIsRunning) {
            if (DURATION <= mTimer * FRAME_RATE) {
                mAnimationIsRunning = false
                mTimer = 0
            } else {
                val progressFraction = mTimer.toFloat() * FRAME_RATE / DURATION

                mFillPaint!!.alpha = (FILL_INITIAL_OPACITY * (1 - progressFraction)).toInt()
                mStrokePaint!!.alpha = (STROKE_INITIAL_OPACITY * (1 - progressFraction)).toInt()

                canvas.drawCircle(mHalfWidth, mHalfWidth, mHalfWidth * progressFraction, mFillPaint!!)
                canvas.drawCircle(mHalfWidth, mHalfWidth, mHalfWidth * progressFraction, mStrokePaint!!)

                mTimer++
            }

            invalidate()
        }
    }

    override fun onLongClick(view: View?): Boolean {
        if (contentDescription == null ||
            contentDescription.toString().equals("", ignoreCase = true)) {
            return false
        } else {
            Toast.makeText(context, contentDescription, Toast.LENGTH_SHORT).show()
            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        startRippleAnimation()
        return super.onTouchEvent(event)
    }

    private fun startRippleAnimation() {
        if (this.isEnabled && !mAnimationIsRunning) {
            mHalfWidth = (measuredWidth / 2).toFloat()
            mAnimationIsRunning = true
            invalidate()
        }
    }

    companion object {
        private val FRAME_RATE = 10
        private val DURATION = 250
        private val FILL_INITIAL_OPACITY = 200
        private val STROKE_INITIAL_OPACITY = 255
    }
}
