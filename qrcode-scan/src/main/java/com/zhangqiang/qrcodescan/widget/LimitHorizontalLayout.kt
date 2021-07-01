package com.zhangqiang.qrcodescan.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.max

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-25
 */
class LimitHorizontalLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var mAdapter: LimitAdapter? = null
    private var mRecyclePool: RecyclePool = RecyclePool()
    private var mNeedBlockLayoutRequest: Boolean = false
    private var mWidthMeasureSpec: Int = 0
    private var mHeightMeasureSpec: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInEditMode) {

            setAdapter(object : LimitAdapter() {
                override fun getCount(): Int {
                    return 100
                }

                override fun getViewType(position: Int): Int {
                    return position % 2
                }

                override fun getView(container: ViewGroup, position: Int, cacheView: View?): View {

                    val viewType = getViewType(position)
                    if (viewType == 0) {
                        val textView = TextView(container.context)
                        textView.textSize = 15f
                        val sb = StringBuilder()
                        for (i in 0..4) {
                            sb.append(position)
                        }
                        textView.text = sb.toString()
                        return textView
                    } else {
                        val imageView = ImageView(container.context)
                        val colorDrawable = ColorDrawable(Color.RED)
                        imageView.setImageDrawable(colorDrawable)
                        imageView.layoutParams = LayoutParams(50, 50)
                        return imageView
                    }
                }
            })
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mWidthMeasureSpec = widthMeasureSpec
        mHeightMeasureSpec = heightMeasureSpec
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var maxHeight = 0
        mAdapter?.apply {

            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            if (mAdapter != null && (heightMode == MeasureSpec.UNSPECIFIED
                        || heightMode == MeasureSpec.AT_MOST)
            ) {
                val count = getCount()
                var left = paddingLeft
                for (index in 0 until count) {
                    val view = obtainView(index)
                    val layoutParams = view.layoutParams as LayoutParams
                    measureView(view, widthMeasureSpec, heightMeasureSpec)
                    left += layoutParams.leftMargin
                    if (left + view.measuredWidth > widthSize - paddingRight) {
                        mRecyclePool.add(layoutParams.viewType,view)
                        break
                    }
                    left += view.measuredWidth + layoutParams.rightMargin
                    maxHeight = max(
                        maxHeight,
                        view.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                    )
                    mRecyclePool.add(layoutParams.viewType,view)
                }
            }
        }
        setMeasuredDimension(
            resolveSize(widthSize, widthMeasureSpec),
            resolveSize(maxHeight + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutChild()
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    private fun obtainView(position: Int): View {
        return mAdapter!!.let {
            val viewType = it.getViewType(position)
            val cacheView = mRecyclePool.pick(viewType)
            val view = it.getView(this@LimitHorizontalLayout, position, cacheView)
            if (view != cacheView && cacheView != null) {
                mRecyclePool.add(viewType, cacheView)
            }
            var layoutParams: LayoutParams? = view.layoutParams as? LayoutParams
            if (layoutParams == null) {
                layoutParams = generateDefaultLayoutParams()
            }
            layoutParams.viewType = viewType
            view.layoutParams = layoutParams
            view
        }
    }

    private fun availableWidth(): Int {
        return width - paddingLeft - paddingRight
    }

    private fun availableHeight(): Int {
        return height - paddingTop - paddingBottom
    }

    fun setAdapter(adapter: LimitAdapter) {
        if (mAdapter != null) {
            mAdapter?.unRegisterObserver(observer)
            mAdapter = null
            mRecyclePool.clear()
        }
        mAdapter = adapter
        mAdapter?.registerObserver(observer)
        requestLayout()
    }

    fun getAdapter(): LimitAdapter? {
        return mAdapter
    }

    override fun requestLayout() {
        if (!mNeedBlockLayoutRequest) {
            super.requestLayout()
        }
    }

    private val observer: Observer = object : Observer {
        override fun onChanged() {
            requestLayout()
        }
    }

    private fun layoutChild() {
        mNeedBlockLayoutRequest = true

        for (childIndex in 0 until childCount) {
            val view = getChildAt(childIndex)
            val lp = view.layoutParams as LayoutParams
            if (lp.viewType < 0) {
                throw IllegalArgumentException("illegal view type : ${lp.viewType}")
            }
            mRecyclePool.add(lp.viewType, view)
        }
        removeViewsInLayout(0, childCount)
        mAdapter?.apply {

            val count = getCount()
            val availableHeight = availableHeight()
            var left: Int = paddingLeft
            for (index in 0 until count) {

                val view = obtainView(index)
                val layoutParams = view.layoutParams as LayoutParams
                measureView(view, mWidthMeasureSpec, mHeightMeasureSpec)
                val l = left + layoutParams.leftMargin
                val t =
                    paddingTop + (availableHeight - view.measuredHeight) / 2 + layoutParams.topMargin
                val r = l + view.measuredWidth
                val b = t + view.measuredHeight
                if (r > width - paddingRight) {
                    mRecyclePool.add(layoutParams.viewType,view)
                    break
                }
                view.layout(l, t, r, b)
                addViewInLayout(view, childCount, layoutParams, true)
                left = r + layoutParams.rightMargin
            }
        }
        mNeedBlockLayoutRequest = false
    }

    class LayoutParams(width: Int, height: Int) :
        ViewGroup.MarginLayoutParams(width, height) {
        var viewType: Int = -1
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun measureView(view: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutParams = view.layoutParams as LayoutParams

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var childWidthMeasureSpec = 0
        val availableWidth =
            widthSize - paddingLeft - paddingRight - layoutParams.leftMargin - layoutParams.rightMargin
        if (widthMode == MeasureSpec.EXACTLY) {
            if (layoutParams.width > 0) {
                childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY)
            } else if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY)
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            }
        } else if (widthMode == MeasureSpec.AT_MOST) {
            if (layoutParams.width > 0) {
                childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY)
            } else if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST)
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            }
        } else if (widthMode == MeasureSpec.UNSPECIFIED) {
            if (layoutParams.width > 0) {
                childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY)
            } else if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            }
        }
        view.measure(
            childWidthMeasureSpec,
            getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom + layoutParams.topMargin + layoutParams.bottomMargin,
                layoutParams.height
            )
        )
    }
}

abstract class LimitAdapter {

    private val observerList: MutableList<Observer> = mutableListOf()

    abstract fun getCount(): Int

    open fun getViewType(position: Int): Int {
        return 0
    }

    abstract fun getView(container: ViewGroup, position: Int, cacheView: View?): View

    fun registerObserver(observer: Observer) {
        if (observerList.contains(observer)) {
            return
        }
        observerList.add(observer)
    }

    fun unRegisterObserver(observer: Observer) {
        observerList.remove(observer)
    }

    fun notifyChanged() {
        for (observer in observerList) {
            observer.onChanged()
        }
    }
}

interface Observer {
    fun onChanged()
}

class RecyclePool {

    private val scrapViews: SparseArray<MutableList<View>> = SparseArray()
    fun add(type: Int, view: View) {
        var views = scrapViews.get(type)
        if (views == null) {
            views = LinkedList()
            scrapViews.put(type,views)
        }
        views.add(view)
    }

    fun pick(type: Int): View? {
        if (scrapViews.size() <= 0) {
            return null
        }
        val views = scrapViews.get(type)
        if (views == null || views.isEmpty()) {
            return null
        }
        return views.removeAt(0)
    }

    fun clear() {
        scrapViews.clear()
    }
}

