package com.lestrades.firebasevendedores

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.lestrades.firebasevendedores.databinding.ItemProductBinding

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val listener: OnProductLisener
) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemProductBinding.bind(view)

        fun setListener(product: Product) {
            binding.root.setOnClickListener {
                listener.onClick(product)
            }
            binding.root.setOnLongClickListener {
                listener.onLongClick(product)
                true
            }
        }
    }
}