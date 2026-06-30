package io.legado.app.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import io.legado.app.R
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor

object NgMenuPopup {

    private const val DEFAULT_WIDTH_DP = 0

    fun bindToolbarMenu(
        context: Context,
        toolbar: Toolbar?,
        menu: Menu,
        prepareMenu: () -> Unit = {},
        onItemClick: (MenuItem) -> Unit
    ) {
        if (toolbar == null) return
        bindActionSubMenus(context, menu, prepareMenu, onItemClick)
        bindOverflowMenu(context, menu, prepareMenu, onItemClick)
    }

    fun show(
        anchor: View,
        menu: Menu,
        widthDp: Int = DEFAULT_WIDTH_DP,
        itemIds: List<Int>? = null,
        includeInvisible: Boolean = false,
        onItemClick: (MenuItem) -> Unit
    ) {
        val items = menu.toPopupItems(
            itemIds = itemIds,
            includeInvisible = includeInvisible
        )
        if (items.isEmpty()) return
        NgActionPopup(anchor.context, items, widthDp) { item ->
            (item.payload as? MenuItem)?.let(onItemClick)
        }.show(anchor)
    }

    fun Menu.toPopupItems(
        itemIds: List<Int>? = null,
        includeInvisible: Boolean = false
    ): List<NgActionPopupItem> {
        val orderedItems = if (itemIds == null) {
            (0 until size()).map { getItem(it) }
        } else {
            itemIds.mapNotNull { id -> findItem(id) }
        }
        var previousGroup = Menu.NONE
        return orderedItems
            .filter { includeInvisible || it.isVisible }
            .mapIndexed { index, item ->
                val groupChanged = index > 0 &&
                    item.groupId != previousGroup &&
                    item.groupId != Menu.NONE
                previousGroup = item.groupId
                NgActionPopupItem(
                    itemId = item.itemId,
                    title = item.title,
                    iconRes = item.defaultIconRes(),
                    iconDrawable = item.icon,
                    checked = item.isChecked,
                    dividerBefore = groupChanged,
                    payload = item
                )
            }
    }

    private fun bindActionSubMenus(
        context: Context,
        menu: Menu,
        prepareMenu: () -> Unit,
        onItemClick: (MenuItem) -> Unit
    ) {
        for (index in 0 until menu.size()) {
            val item = menu.getItem(index)
            val subMenu = item.subMenu ?: continue
            if (!item.isVisible || !item.wantsActionButton()) continue
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            item.actionView = createToolbarActionView(
                context = context,
                icon = item.icon,
                iconRes = item.defaultIconRes().takeIf { it != 0 },
                contentDescription = item.title
            ) { anchor ->
                prepareMenu()
                show(anchor, subMenu, onItemClick = onItemClick)
            }
        }
    }

    private fun bindOverflowMenu(
        context: Context,
        menu: Menu,
        prepareMenu: () -> Unit,
        onItemClick: (MenuItem) -> Unit
    ) {
        val candidateIds = overflowCandidates(menu).map { it.itemId }
        if (candidateIds.isEmpty()) return
        hideItems(menu, candidateIds)
        val moreItem = menu.findItem(R.id.menu_more)
            ?: menu.add(Menu.NONE, R.id.menu_more, menu.size() + 1000, R.string.more)
        moreItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        moreItem.icon = ContextCompat.getDrawable(context, R.drawable.ic_more_vert)
        moreItem.actionView = createToolbarActionView(
            context = context,
            icon = moreItem.icon,
            iconRes = R.drawable.ic_more_vert,
            contentDescription = moreItem.title
        ) { anchor ->
            candidateIds.forEach { id ->
                menu.findItem(id)?.isVisible = true
            }
            prepareMenu()
            val visibleIds = candidateIds.filter { id -> menu.findItem(id)?.isVisible == true }
            if (visibleIds.isEmpty()) return@createToolbarActionView
            hideItems(menu, visibleIds)
            show(
                anchor = anchor,
                menu = menu,
                itemIds = visibleIds,
                includeInvisible = true,
                onItemClick = onItemClick
            )
        }
    }

    private fun overflowCandidates(menu: Menu): List<MenuItem> {
        return (0 until menu.size())
            .map { menu.getItem(it) }
            .filter { item ->
                item.itemId != R.id.menu_more &&
                    item.isVisible &&
                    item.actionView == null &&
                    item.subMenu == null &&
                    !item.wantsActionButton()
            }
    }

    private fun hideItems(menu: Menu, itemIds: List<Int>) {
        itemIds.forEach { id ->
            menu.findItem(id)?.isVisible = false
        }
    }

    private fun createToolbarActionView(
        context: Context,
        icon: Drawable?,
        @DrawableRes iconRes: Int?,
        contentDescription: CharSequence?,
        onClick: (View) -> Unit
    ): View {
        return ImageButton(context).apply {
            val drawable = icon ?: iconRes?.let { ContextCompat.getDrawable(context, it) }
            setImageDrawable(drawable?.mutate())
            setColorFilter(context.getCompatColor(R.color.primaryText))
            this.contentDescription = contentDescription
            background = toolbarItemBackground(context)
            scaleType = ImageView.ScaleType.CENTER
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick(it) }
            layoutParams = ViewGroup.LayoutParams(48.dpToPx(), 48.dpToPx())
        }
    }

    private fun toolbarItemBackground(context: Context): Drawable? {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true)
        return ContextCompat.getDrawable(context, outValue.resourceId)
    }

    private fun MenuItem.wantsActionButton(): Boolean {
        return reflectBoolean("requiresActionButton") || reflectBoolean("requestsActionButton")
    }

    @SuppressLint("PrivateApi")
    private fun MenuItem.reflectBoolean(methodName: String): Boolean {
        return runCatching {
            javaClass.getDeclaredMethod(methodName).apply { isAccessible = true }
                .invoke(this) as? Boolean
        }.getOrNull() == true
    }

    @DrawableRes
    private fun MenuItem.defaultIconRes(): Int {
        if (icon != null) return 0
        return when (itemId) {
            R.id.menu_add,
            R.id.menu_add_book_source,
            R.id.menu_add_replace_rule -> R.drawable.ic_add

            R.id.menu_import_local,
            R.id.menu_import_onLine,
            R.id.menu_import_qr,
            R.id.menu_import_default -> R.drawable.ic_import

            R.id.menu_help -> R.drawable.ic_help
            R.id.menu_group,
            R.id.menu_group_manage,
            R.id.menu_add_group,
            R.id.menu_remove_group,
            R.id.menu_clear_group,
            R.id.menu_auto_group -> R.drawable.ic_groups

            R.id.action_sort,
            R.id.menu_sort_desc,
            R.id.menu_sort_manual,
            R.id.menu_sort_auto,
            R.id.menu_sort_name,
            R.id.menu_sort_url,
            R.id.menu_sort_time,
            R.id.menu_sort_respondTime,
            R.id.menu_sort_enable -> R.drawable.ic_sort

            R.id.menu_enabled_group,
            R.id.menu_enable_selection,
            R.id.menu_enable_explore -> R.drawable.ic_check

            R.id.menu_disabled_group,
            R.id.menu_disable_selection,
            R.id.menu_disable_explore -> R.drawable.ic_baseline_close

            R.id.menu_group_login -> R.drawable.ic_lock_outline
            R.id.menu_export_selection -> R.drawable.ic_export
            R.id.menu_share_source -> R.drawable.ic_share
            R.id.menu_check_source,
            R.id.menu_check_selected_interval -> R.drawable.ic_check_source

            R.id.menu_top_sel -> R.drawable.ic_expand_more
            R.id.menu_bottom_sel -> R.drawable.ic_expand_more
            R.id.menu_del_selection -> R.drawable.ic_outline_delete
            else -> titleFallbackIconRes()
        }
    }

    @DrawableRes
    private fun MenuItem.titleFallbackIconRes(): Int {
        val text = title?.toString().orEmpty()
        return when {
            text.contains("网络日志") -> R.drawable.ic_network_check
            text.contains("调试日志") || text.contains("日志") -> R.drawable.ic_help
            text.contains("刷新") || text.contains("校验") -> R.drawable.ic_refresh_black_24dp
            text.contains("复制") || text.contains("拷贝") -> R.drawable.ic_copy
            text.contains("删除") || text.contains("清空") -> R.drawable.ic_outline_delete
            text.contains("分享") -> R.drawable.ic_share
            text.contains("导出") -> R.drawable.ic_export
            text.contains("登录") -> R.drawable.ic_lock_outline
            text.contains("置顶") -> R.drawable.ic_expand_more
            text.contains("启用") || text.contains("允许") -> R.drawable.ic_check
            text.contains("禁用") || text.contains("关闭") -> R.drawable.ic_baseline_close
            else -> 0
        }
    }
}
