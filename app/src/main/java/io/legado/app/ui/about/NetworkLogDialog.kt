package io.legado.app.ui.about

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.databinding.DialogRecyclerViewBinding
import io.legado.app.databinding.ItemNetworkLogBinding
import io.legado.app.help.http.NetworkLog
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.widget.dialog.TextDialog
import io.legado.app.utils.dpToPx
import io.legado.app.utils.setLayout
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.viewbindingdelegate.viewBinding
import splitties.views.onClick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NetworkLogDialog : BaseDialogFragment(R.layout.dialog_recycler_view),
    Toolbar.OnMenuItemClickListener {

    private val binding by viewBinding(DialogRecyclerViewBinding::bind)
    private val adapter by lazy { NetworkLogAdapter(requireContext()) }
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    override fun onStart() {
        super.onStart()
        setLayout(0.92f, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.run {
            toolBar.setBackgroundColor(primaryColor)
            toolBar.setTitle(R.string.network_request_log)
            toolBar.inflateMenu(R.menu.app_log)
            toolBar.setOnMenuItemClickListener(this@NetworkLogDialog)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.clipToPadding = false
            recyclerView.setPadding(0, 2.dpToPx(), 0, 2.dpToPx())
            recyclerView.adapter = adapter
        }
        adapter.setItems(NetworkLog.logs)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_clear -> {
                NetworkLog.clear()
                adapter.clearItems()
            }
        }
        return true
    }

    inner class NetworkLogAdapter(context: Context) :
        RecyclerAdapter<NetworkLog.Entry, ItemNetworkLogBinding>(context) {

        override fun getViewBinding(parent: ViewGroup): ItemNetworkLogBinding {
            return ItemNetworkLogBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemNetworkLogBinding,
            item: NetworkLog.Entry,
            payloads: MutableList<Any>
        ) {
            binding.textType.text = item.type
            binding.textType.background = tagBackground(item.type)
            binding.textTime.text = timeFormat.format(Date(item.time))
            binding.textMethod.text = item.method
            bindStatus(binding, item)
            binding.textTook.text = item.tookMs?.let { "${it}ms" }.orEmpty()
            binding.textTook.visibility = if (item.tookMs == null) View.GONE else View.VISIBLE
            binding.textUrl.text = item.url
            binding.textSource.text = item.source
            binding.textSource.visibility = if (item.source.isBlank()) View.GONE else View.VISIBLE
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemNetworkLogBinding) {
            binding.root.onClick {
                getItem(holder.layoutPosition)?.let { item ->
                    showDialogFragment(TextDialog("Network", item.detail))
                }
            }
        }

        private fun bindStatus(binding: ItemNetworkLogBinding, item: NetworkLog.Entry) {
            val statusCode = item.statusCode
            val error = item.error != null
            if (statusCode == null && !error) {
                binding.layoutStatus.visibility = View.GONE
                return
            }
            val color = when {
                error -> Color.parseColor("#D93025")
                statusCode in 200..399 -> Color.parseColor("#34A853")
                statusCode != null -> Color.parseColor("#D93025")
                else -> Color.parseColor("#D93025")
            }
            binding.layoutStatus.visibility = View.VISIBLE
            binding.viewStatusDot.background = dotBackground(color)
            binding.textStatus.setTextColor(color)
            binding.textStatus.text = statusCode?.toString() ?: "ERR"
        }

        private fun tagBackground(type: String): GradientDrawable {
            val color = when (type) {
                "OkHttp" -> Color.parseColor("#2E7D32")
                "WebView" -> Color.parseColor("#1565C0")
                "JS" -> Color.parseColor("#6A1B9A")
                else -> Color.parseColor("#5F6368")
            }
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 5.dpToPx().toFloat()
                setColor(color)
            }
        }

        private fun dotBackground(color: Int): GradientDrawable {
            return GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
        }
    }
}
