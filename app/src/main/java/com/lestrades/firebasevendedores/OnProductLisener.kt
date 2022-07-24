package com.lestrades.firebasevendedores

interface OnProductLisener {
    fun onClick(product: Product)
    fun onLongClick(product: Product)
}