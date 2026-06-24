package io.legado.app.ui.config

import android.graphics.Color
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceGroup
import io.legado.app.R
import io.legado.app.lib.prefs.fragment.PreferenceFragment

object ConfigPreferenceStyle {

    fun applyTo(preferenceGroup: PreferenceGroup?) {
        preferenceGroup ?: return
        for (index in 0 until preferenceGroup.preferenceCount) {
            val preference = preferenceGroup.getPreference(index)
            preference.layoutResource = if (preference is PreferenceCategory) {
                R.layout.view_my_preference_category
            } else {
                R.layout.view_my_preference
            }
            if (preference is PreferenceGroup) {
                applyTo(preference)
            }
        }
    }

    fun applyListStyle(fragment: PreferenceFragment) {
        fragment.view?.setBackgroundColor(Color.TRANSPARENT)
        fragment.listView.setBackgroundColor(Color.TRANSPARENT)
        fragment.listView.setPadding(
            0,
            fragment.resources.getDimensionPixelSize(R.dimen.ng_space_l),
            0,
            fragment.listView.paddingBottom
        )
    }

}
