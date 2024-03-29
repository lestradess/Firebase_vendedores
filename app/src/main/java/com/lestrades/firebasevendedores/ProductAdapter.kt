package com.lestrades.firebasevendedores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.lestrades.firebasevendedores.databinding.ItemProductBinding

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val listener: OnProductLisener
) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private lateinit var context: Context
    //Ser crea la vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    //Se le asigna un valor a cada componente
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        holder.setListener(product)
        holder.binding.tvName.text = product.name
        holder.binding.tvPrice.text = product.price.toString()
        holder.binding.tvQuantity.text = product.price.toString()
        Glide.with(context)
            .load(product.imgUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .into(holder.binding.imgProduct)
    }

    override fun getItemCount(): Int = productList.size

    fun add(product: Product){
        if (!productList.contains(product)){
            productList.add(product)
            notifyItemInserted(productList.size -1)
        } else {
            update(product)
        }
    }
    fun update (product: Product){
        val index = productList.indexOf(product)
        if (index!=-1){
            productList.set(index,product)
            notifyItemChanged(index)
        }
    }
    fun delete(product: Product){
        val index = productList.indexOf(product)
        if (index!=-1){
            productList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

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
