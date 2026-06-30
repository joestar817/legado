package io.legado.app.ui.main.rss

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.request.RequestOptions
import io.legado.app.R
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.RssSource
import io.legado.app.databinding.ItemRssBinding
import io.legado.app.help.glide.ImageLoader
import io.legado.app.help.glide.OkHttpModelLoader
import io.legado.app.ui.widget.NgActionPopup
import io.legado.app.ui.widget.NgActionPopupItem
import splitties.views.onLongClick

class RssAdapter(
    context: Context,
    private val fragment: Fragment,
    private val callBack: CallBack,
    private val lifecycle: Lifecycle
) : RecyclerAdapter<RssSource, ItemRssBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemRssBinding {
        return ItemRssBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemRssBinding,
        item: RssSource,
        payloads: MutableList<Any>
    ) {
        binding.apply {
            tvName.text = item.sourceName
            val options = RequestOptions()
                .set(OkHttpModelLoader.sourceOriginOption, item.sourceUrl)
            ImageLoader.load(fragment, lifecycle, item.sourceIcon)
                .apply(options)
                .centerCrop()
                .placeholder(R.drawable.image_rss)
                .error(R.drawable.image_rss)
                .into(ivIcon)
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemRssBinding) {
        binding.apply {
            root.setOnClickListener {
                getItemByLayoutPosition(holder.layoutPosition)?.let {
                    callBack.openRss(it)
                }
            }
            root.onLongClick {
                getItemByLayoutPosition(holder.layoutPosition)?.let {
                    showMenu(ivIcon, it)
                }
            }
        }
    }

    private fun showMenu(view: View, rssSource: RssSource) {
        NgActionPopup(context, buildRssMenuItems(rssSource)) {
            when (it.itemId) {
                R.id.menu_edit -> callBack.edit(rssSource)
                R.id.menu_top -> callBack.toTop(rssSource)
                R.id.menu_login -> callBack.login(rssSource)
                R.id.menu_del -> callBack.del(rssSource)
                R.id.menu_disable -> callBack.disable(rssSource)
            }
        }.show(view)
    }

    private fun buildRssMenuItems(rssSource: RssSource): List<NgActionPopupItem> {
        return buildList {
            add(NgActionPopupItem(R.id.menu_edit, R.string.edit, R.drawable.ic_edit))
            add(NgActionPopupItem(R.id.menu_top, R.string.to_top, R.drawable.ic_arrow_drop_up))
            if (!rssSource.loginUrl.isNullOrBlank()) {
                add(NgActionPopupItem(R.id.menu_login, R.string.login, R.drawable.ic_lock_outline))
            }
            add(NgActionPopupItem(R.id.menu_disable, R.string.disable_source, R.drawable.ic_baseline_close, dividerBefore = true))
            add(NgActionPopupItem(R.id.menu_del, R.string.delete, R.drawable.ic_outline_delete))
        }
    }

    interface CallBack {
        fun openRss(rssSource: RssSource)
        fun edit(rssSource: RssSource)
        fun toTop(rssSource: RssSource)
        fun login(rssSource: RssSource)
        fun del(rssSource: RssSource)
        fun disable(rssSource: RssSource)
    }
}
