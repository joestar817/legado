package io.legado.app.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.legado.app.ui.widget.text.AccentBgTextView
import io.legado.app.utils.dpToPx

@Suppress("unused", "MemberVisibilityCanBePrivate")
class WrapLabelsBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private val unUsedViews = arrayListOf<TextView>()
    private val usedViews = arrayListOf<TextView>()
    var textSize = 11f
    var maxRows = 2
    private val rowGap = 4.dpToPx()

    fun setLabels(
        labels: List<String>,
        onClick: ((String) -> Unit)? = null,
        onLongClick: ((String) -> Boolean)? = null
    ) {
        clear()
        labels.forEach {
            addLabel(it, onClick, onLongClick)
        }
    }

    fun clear() {
        unUsedViews.addAll(usedViews)
        usedViews.clear()
        removeAllViews()
    }

    fun addLabel(label: String, onClick: ((String) -> Unit)?, onLongClick: ((String) -> Boolean)?) {
        val tv = if (unUsedViews.isEmpty()) {
            AccentBgTextView(context, null).apply {
                setPadding(3.dpToPx(), 0, 3.dpToPx(), 0)
                setRadius(2)
                layoutParams = MarginLayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = 2.dpToPx()
                    bottomMargin = rowGap
                }
                maxLines = 1
                usedViews.add(this)
            }
        } else {
            unUsedViews.last().apply {
                usedViews.add(this)
                unUsedViews.removeAt(unUsedViews.lastIndex)
            }
        }
        tv.textSize = textSize
        tv.text = label
        tv.setOnClickListener(if (onClick != null) View.OnClickListener { onClick(label) } else null)
        tv.setOnLongClickListener(
            if (onLongClick != null) View.OnLongClickListener { onLongClick(label) } else null
        )
        addView(tv)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val maxWidth = if (widthMode == MeasureSpec.UNSPECIFIED) Int.MAX_VALUE else widthSize
        val availableWidth = (maxWidth - paddingLeft - paddingRight).coerceAtLeast(0)
        var rowWidth = 0
        var rowHeight = 0
        var maxRowWidth = 0
        var rows = 1
        var contentHeight = paddingTop + paddingBottom

        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == GONE) continue
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
            if (rowWidth > 0 && rowWidth + childWidth > availableWidth) {
                maxRowWidth = maxOf(maxRowWidth, rowWidth)
                contentHeight += rowHeight
                rows += 1
                rowWidth = 0
                rowHeight = 0
                if (rows > maxRows) break
            }
            rowWidth += childWidth
            rowHeight = maxOf(rowHeight, childHeight)
        }
        if (rows <= maxRows) {
            maxRowWidth = maxOf(maxRowWidth, rowWidth)
            contentHeight += rowHeight
        }
        val contentWidth = maxRowWidth + paddingLeft + paddingRight
        val measuredWidth = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            contentWidth.coerceAtMost(maxWidth)
        }
        setMeasuredDimension(
            resolveSize(measuredWidth, widthMeasureSpec),
            resolveSize(contentHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val availableWidth = (r - l - paddingLeft - paddingRight).coerceAtLeast(0)
        var x = paddingLeft
        var y = paddingTop
        var rowHeight = 0
        var rows = 1
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == GONE) continue
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
            if (x > paddingLeft && x + childWidth > paddingLeft + availableWidth) {
                rows += 1
                if (rows > maxRows) return
                x = paddingLeft
                y += rowHeight
                rowHeight = 0
            }
            val left = x + lp.leftMargin
            val top = y + lp.topMargin
            child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
            x += childWidth
            rowHeight = maxOf(rowHeight, childHeight)
        }
    }
}
