package com.zhangqiang.qrcodescan.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-20
 */
class ScanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint = Paint()
    private val mTmpRect = Rect()
    private var horizontalBarLength: Int = 100
    private var verticalBarLength: Int = 60
    private var barWidth: Int = 10
    private var scanDrawable: Drawable? = null
    private var scanRegionHeight: Int = 50
    private val scanInterval = 3000
    private var scanOffset: Int = 0
    private var scanAnimator: Animator? = null
    private var mMainColor:Int = Color.BLACK

    init {
        mMainColor = Color.WHITE
        scanDrawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, IntArray(2) {
            if (it == 0) {
                mMainColor
            } else {
                Color.TRANSPARENT
            }
        })
        barWidth = 2.dpToPx()
        scanRegionHeight = 10.dpToPx()
        horizontalBarLength = 50.dpToPx()
        verticalBarLength = 30.dpToPx()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = mMainColor
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            //draw left top
            mTmpRect.set(
                paddingLeft, paddingTop, paddingLeft + horizontalBarLength,
                paddingTop + barWidth
            )
            canvas.drawRect(mTmpRect, mPaint)

            mTmpRect.set(
                paddingLeft,
                paddingTop + barWidth,
                paddingLeft + barWidth,
                paddingTop + barWidth + verticalBarLength
            )
            canvas.drawRect(mTmpRect, mPaint)

            //draw top right
            mTmpRect.set(
                width - paddingRight - horizontalBarLength,
                paddingTop,
                width - paddingRight,
                paddingTop + barWidth
            )
            canvas.drawRect(mTmpRect, mPaint)
            mTmpRect.set(
                width - paddingRight - barWidth,
                paddingTop + barWidth,
                width - paddingRight,
                paddingTop + barWidth + verticalBarLength
            )
            canvas.drawRect(mTmpRect, mPaint)
            //draw left bottom
            mTmpRect.set(
                paddingLeft,
                height - paddingBottom - barWidth,
                paddingLeft + horizontalBarLength,
                height - paddingBottom
            )
            canvas.drawRect(mTmpRect, mPaint)
            mTmpRect.set(
                paddingLeft,
                height - paddingBottom - barWidth - verticalBarLength,
                paddingLeft + barWidth,
                height - paddingBottom - barWidth
            )
            canvas.drawRect(mTmpRect, mPaint)
            //draw right bottom
            mTmpRect.set(
                width - paddingRight - horizontalBarLength,
                height - paddingBottom - barWidth,
                width - paddingRight,
                height - paddingBottom
            )
            canvas.drawRect(mTmpRect, mPaint)
            mTmpRect.set(
                width - paddingRight - barWidth,
                height - paddingBottom - barWidth - verticalBarLength,
                width - paddingRight,
                height - paddingBottom - barWidth
            )
            canvas.drawRect(mTmpRect, mPaint)
            //draw scanning view
            val scanRegionTop = paddingTop + barWidth - scanRegionHeight + scanOffset
            val scanRegionBottom = scanRegionTop + scanRegionHeight
            scanDrawable?.setBounds(
                paddingLeft + barWidth,
                scanRegionTop,
                width - paddingRight - barWidth,
                scanRegionBottom
            )
            canvas.clipRect(
                paddingLeft + barWidth,
                paddingTop + barWidth,
                width - paddingRight - barWidth,
                height - paddingBottom - barWidth
            )
            scanDrawable?.draw(canvas)
            invalidate()
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateAnimator()
    }

    private fun updateAnimator() {
        if (visibility == VISIBLE && ViewCompat.isAttachedToWindow(this)) {
            if (scanAnimator == null) {
                val animator =
                    ValueAnimator.ofFloat(1F)
                animator.duration = scanInterval.toLong()
                animator.interpolator = LinearInterpolator()
                animator.addUpdateListener {
                    val factor = it.animatedValue as Float
                    val scanRange = height - paddingTop - paddingBottom - barWidth * 2 + scanRegionHeight
                    scanOffset = (scanRange * factor).toInt()
                    invalidate()
                }
                animator.repeatCount = ValueAnimator.INFINITE
                scanAnimator = animator

            }
            scanAnimator?.start()
        } else {
            scanAnimator?.cancel()
            scanAnimator = null
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateAnimator()
    }

    private fun Int.dpToPx():Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),resources.displayMetrics)
            .toInt()
    }

    fun Float.dpToPx():Int{
       return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this,resources.displayMetrics)
           .toInt()
    }
}