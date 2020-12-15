package com.jhworks.library.decoration

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class DividerGridItemDecoration : ItemDecoration {
    private var mDivider: Drawable?

    constructor(context: Context) {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
    }

    constructor(context: Context, resId: Int) {
        mDivider = ContextCompat.getDrawable(context, resId)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        // 列数
        var spanCount = -1
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            spanCount = layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            spanCount = layoutManager.spanCount
        }
        return spanCount
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        mDivider ?: return

        val childCount = parent.childCount
        var child: View
        for (i in 0 until childCount) {
            child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val right = child.right + params.rightMargin + mDivider!!.intrinsicWidth
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider?.setBounds(left, top, right, bottom)
            mDivider?.draw(c)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        mDivider ?: return

//        val spanCount = getSpanCount(parent)
        val childCount = parent.childCount
        var child: View
        for (i in 0 until childCount) {
            child = parent.getChildAt(i)
//            val itemPosition = parent.layoutManager?.getPosition(child) ?: 0
//            if (!isLastColum(parent, itemPosition, spanCount, childCount)) {
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin
            val left = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicWidth
            mDivider?.setBounds(left, top, right, bottom)
            mDivider?.draw(c)
//            }
        }
    }

    private fun isLastColum(parent: RecyclerView, pos: Int, spanCount: Int,
                            childCount: Int): Boolean {
        var childCountTemp = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            if ((pos + 1) % spanCount == 0) {// 如果是最后一列，则不需要绘制右边
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0) { // 如果是最后一列，则不需要绘制右边
                    return true
                }
            } else {
                childCountTemp -= childCountTemp % spanCount
                if (pos >= childCountTemp) // 如果是最后一列，则不需要绘制右边
                    return true
            }
        }
        return false
    }

    private fun isFirstColumn(parent: RecyclerView, pos: Int, spanCount: Int,
                              childCount: Int): Boolean {
        var childCountTemp = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            if (pos % spanCount == 0) {
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if (pos % spanCount == 0) {
                    return true
                }
            } else {
                val firstItemPositions = intArrayOf()
                layoutManager.findFirstVisibleItemPositions(firstItemPositions)

                // todo
                childCountTemp -= childCountTemp % spanCount
                if (pos >= childCountTemp)
                    return true
            }
        }
        return false
    }

    private fun isLastRaw(parent: RecyclerView, pos: Int, spanCount: Int,
                          childCount: Int): Boolean {
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            // 如果是最后一行，则不需要绘制底部
            if (pos + spanCount >= childCount) return true
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager.orientation
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一行，则不需要绘制底部
                if (pos + spanCount >= childCount) return true
            } else  // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true
                }
            }
        }
        return false
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        mDivider ?: return

        val itemPosition = parent.layoutManager?.getPosition(view) ?: 0
        val spanCount = getSpanCount(parent)
        val childCount = parent.adapter?.itemCount ?: 0
        // 如果是最后一行，则不需要绘制底部
        if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
            // 最后一行且为最后一列则不绘制
            if (isLastColum(parent, itemPosition, spanCount, childCount)) {
                outRect[0, 0, 0] = 0
            } else {
                outRect[0, 0, mDivider!!.intrinsicWidth] = 0
            }
//            outRect.set(0, 0, mDivider!!.intrinsicWidth, 0)
        } else if (isLastColum(parent, itemPosition, spanCount, childCount)) {
            // 如果是最后一列，则不需要绘制右边
            outRect[0, 0, 0] = mDivider!!.intrinsicHeight
//            Log.e("xxxxxxxxxxxx", "最后一列>> $itemPosition, $childCount")

        } else {
            outRect[0, 0, mDivider!!.intrinsicWidth] = mDivider!!.intrinsicHeight
        }
    }

    companion object {
        private val ATTRS = intArrayOf(R.attr.listDivider)
    }
}