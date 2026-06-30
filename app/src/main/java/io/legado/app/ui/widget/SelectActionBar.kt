package io.legado.app.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import io.legado.app.R
import io.legado.app.databinding.ViewSelectActionBarBinding
import io.legado.app.lib.theme.TintHelper
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.elevation
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.getSecondaryDisabledTextColor
import io.legado.app.lib.theme.transparentNavBar
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.visible


@Suppress("unused")
class SelectActionBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val bgIsLight = ColorUtils.isColorLight(context.bottomBackground)
    private val primaryTextColor = context.getPrimaryTextColor(bgIsLight)
    private val disabledColor = context.getSecondaryDisabledTextColor(bgIsLight)

    private var callBack: CallBack? = null
    private var selMenu: PopupMenu? = null
    private var menuItemClickListener: PopupMenu.OnMenuItemClickListener? = null
    private val binding = ViewSelectActionBarBinding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        if (!isInEditMode) {
            val transparentNavBar = context.transparentNavBar
            if (transparentNavBar) {
                setBackgroundColor(Color.TRANSPARENT)
            } else {
                setBackgroundColor(context.bottomBackground)
                elevation = context.elevation
            }
            binding.cbSelectedAll.setTextColor(primaryTextColor)
            TintHelper.setTint(binding.cbSelectedAll, context.accentColor, !bgIsLight)
            binding.ivMenuMore.setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN)
            binding.cbSelectedAll.setOnUserCheckedChangeListener { isChecked ->
                callBack?.selectAll(isChecked)
            }
            binding.btnRevertSelection.setOnClickListener { callBack?.revertSelection() }
            binding.btnSelectActionMain.setOnClickListener { callBack?.onClickSelectBarMainAction() }
            binding.ivMenuMore.setOnClickListener { showSelectionMenu() }
            applyNavigationBarPadding()
        }
    }

    fun setMainActionText(text: String) = binding.run {
        btnSelectActionMain.text = text
        btnSelectActionMain.visible()
    }

    fun setMainActionText(@StringRes id: Int) = binding.run {
        btnSelectActionMain.setText(id)
        btnSelectActionMain.visible()
    }

    fun inflateMenu(@MenuRes resId: Int): Menu? {
        selMenu = PopupMenu(context, binding.ivMenuMore)
        selMenu?.inflate(resId)
        binding.ivMenuMore.visible()
        return selMenu?.menu
    }

    fun setCallBack(callBack: CallBack) {
        this.callBack = callBack
    }

    fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        menuItemClickListener = listener
    }

    private fun showSelectionMenu() {
        val menu = selMenu?.menu ?: return
        val items = buildPopupItems(menu)
        if (items.isEmpty()) return
        NgActionPopup(context, items, widthDp = 180) { item ->
            (item.payload as? MenuItem)?.let { menuItem ->
                menuItemClickListener?.onMenuItemClick(menuItem)
            }
        }.show(binding.ivMenuMore)
    }

    private fun buildPopupItems(menu: Menu): List<NgActionPopupItem> {
        val items = arrayListOf<NgActionPopupItem>()
        for (index in 0 until menu.size()) {
            val menuItem = menu[index]
            if (!menuItem.isVisible) continue
            items.add(
                NgActionPopupItem(
                    itemId = menuItem.itemId,
                    title = menuItem.title,
                    iconRes = selectActionIcon(menuItem.itemId),
                    checked = menuItem.isChecked,
                    payload = menuItem
                )
            )
        }
        return items
    }

    private fun selectActionIcon(itemId: Int): Int {
        return when (itemId) {
            R.id.menu_enable_selection,
            R.id.menu_enable_explore,
            R.id.menu_update_enable -> R.drawable.ic_check
            R.id.menu_disable_selection,
            R.id.menu_disable_explore,
            R.id.menu_update_disable -> R.drawable.ic_baseline_close
            R.id.menu_add_group,
            R.id.menu_add_to_group -> R.drawable.ic_add
            R.id.menu_remove_group,
            R.id.menu_remove_to_group,
            R.id.menu_del_selection -> R.drawable.ic_outline_delete
            R.id.menu_clear_group,
            R.id.menu_clear_cache -> R.drawable.ic_clear_all
            R.id.menu_auto_group,
            R.id.menu_check_selected_interval -> R.drawable.ic_refresh_black_24dp
            R.id.menu_top_sel -> R.drawable.ic_arrow_drop_up
            R.id.menu_bottom_sel -> R.drawable.ic_arrow_down
            R.id.menu_export_selection -> R.drawable.ic_export
            R.id.menu_share_source -> R.drawable.ic_share
            R.id.menu_check_source -> R.drawable.ic_check_source
            R.id.menu_change_source -> R.drawable.ic_swap_horiz
            else -> 0
        }
    }

    fun upCountView(selectCount: Int, allCount: Int) = binding.run {
        if (selectCount == 0) {
            cbSelectedAll.isChecked = false
        } else {
            cbSelectedAll.isChecked = selectCount >= allCount
        }

        //重置全选的文字
        if (cbSelectedAll.isChecked) {
            cbSelectedAll.text = context.getString(
                R.string.select_cancel_count,
                selectCount,
                allCount
            )
        } else {
            cbSelectedAll.text = context.getString(
                R.string.select_all_count,
                selectCount,
                allCount
            )
        }
        setMenuClickable(selectCount > 0)
    }

    private fun setMenuClickable(isClickable: Boolean) = binding.run {
        btnRevertSelection.isEnabled = isClickable
        btnRevertSelection.isClickable = isClickable
        btnSelectActionMain.isEnabled = isClickable
        btnSelectActionMain.isClickable = isClickable
        if (isClickable) {
            ivMenuMore.setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_IN)
        } else {
            ivMenuMore.setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN)
        }
        ivMenuMore.isEnabled = isClickable
        ivMenuMore.isClickable = isClickable
    }

    interface CallBack {

        fun selectAll(selectAll: Boolean)

        fun revertSelection()

        fun onClickSelectBarMainAction() {}
    }
}
