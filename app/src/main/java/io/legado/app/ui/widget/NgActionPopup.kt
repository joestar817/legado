package io.legado.app.ui.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.legado.app.R
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor

data class NgActionPopupItem(
    val itemId: Int,
    val titleRes: Int,
    val iconRes: Int,
    val checked: Boolean = false,
    val dividerBefore: Boolean = false
)

class NgActionPopup(
    context: Context,
    items: List<NgActionPopupItem>,
    private val widthDp: Int = 152,
    onItemClick: (NgActionPopupItem) -> Unit
) : PopupWindow(widthDp.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT) {

    init {
        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 6.dpToPx(), 0, 6.dpToPx())
            background = GradientDrawable().apply {
                setColor(context.getCompatColor(R.color.ng_surface_soft))
                cornerRadius = 18.dpToPx().toFloat()
            }
        }
        items.forEach { item ->
            if (item.dividerBefore) {
                panel.addView(createDivider(context))
            }
            panel.addView(createActionRow(context, item) {
                dismiss()
                onItemClick(item)
            })
        }
        contentView = panel
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(0x00000000))
        elevation = 8.dpToPx().toFloat()
    }

    fun show(anchor: View) {
        val margin = 8.dpToPx()
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val rootWidth = anchor.rootView.width
        val maxX = (rootWidth - width - margin).coerceAtLeast(margin)
        val anchorRight = location[0] + anchor.width
        val right = if (anchorRight > width) anchorRight else rootWidth - margin
        val x = (right - width - margin).coerceIn(margin, maxX)
        val y = location[1] + anchor.height + margin
        showAtLocation(anchor.rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun createActionRow(
        context: Context,
        item: NgActionPopupItem,
        onClick: () -> Unit
    ): View {
        val color = context.getCompatColor(R.color.ng_on_surface)
        return LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(12.dpToPx(), 0, 12.dpToPx(), 0)
            minimumHeight = 44.dpToPx()
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            addView(ImageView(context).apply {
                setImageDrawable(ContextCompat.getDrawable(context, item.iconRes))
                setColorFilter(color)
            }, LinearLayout.LayoutParams(20.dpToPx(), 20.dpToPx()).apply {
                marginEnd = 10.dpToPx()
            })
            addView(TextView(context).apply {
                text = context.getString(item.titleRes)
                setTextColor(color)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                includeFontPadding = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            if (item.checked) {
                addView(ImageView(context).apply {
                    setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check))
                    setColorFilter(color)
                }, LinearLayout.LayoutParams(20.dpToPx(), 20.dpToPx()).apply {
                    marginStart = 10.dpToPx()
                })
            }
        }
    }

    private fun createDivider(context: Context): View {
        return View(context).apply {
            setBackgroundColor(context.getCompatColor(R.color.ng_outline))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                leftMargin = 12.dpToPx()
                rightMargin = 12.dpToPx()
                topMargin = 3.dpToPx()
                bottomMargin = 3.dpToPx()
            }
        }
    }
}
