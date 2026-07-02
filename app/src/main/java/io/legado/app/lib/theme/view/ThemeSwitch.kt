package io.legado.app.lib.theme.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.SwitchCompat
import io.legado.app.lib.theme.accentColor
import io.legado.app.utils.applyTint

/**
 * @author Aidan Follestad (afollestad)
 */
class ThemeSwitch(context: Context, attrs: AttributeSet) : SwitchCompat(context, attrs) {

    private var isUserAction = false

    init {
        if (!isInEditMode) {
            applyTint(context.accentColor)
        }
    }

    override fun performClick(): Boolean {
        isUserAction = true
        val result = super.performClick()
        isUserAction = false
        return result
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            isUserAction = true
        }
        val result = super.onTouchEvent(ev)
        if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            isUserAction = false
        }
        return result
    }

    fun setOnUserCheckedChangeListener(listener: ((Boolean) -> Unit)?) {
        if (listener == null) {
            return super.setOnCheckedChangeListener(null)
        }
        super.setOnCheckedChangeListener { _, isChecked ->
            if (isUserAction) {
                listener.invoke(isChecked)
            }
        }
    }

}
