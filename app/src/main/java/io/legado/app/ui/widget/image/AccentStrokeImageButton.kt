package io.legado.app.ui.widget.image

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.ColorUtils
import io.legado.app.R
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor

class AccentStrokeImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    init {
        upStyle()
    }

    private fun upStyle() {
        val accentColor = if (isInEditMode) {
            context.getCompatColor(R.color.accent)
        } else {
            ThemeStore.accentColor(context)
        }
        imageTintList = ColorStateList.valueOf(accentColor)
        background = Selector.shapeBuild()
            .setCornerRadius(12.dpToPx())
            .setStrokeWidth(1.dpToPx())
            .setDefaultStrokeColor(accentColor)
            .setPressedBgColor(ColorUtils.setAlphaComponent(accentColor, 24))
            .create()
    }
}
