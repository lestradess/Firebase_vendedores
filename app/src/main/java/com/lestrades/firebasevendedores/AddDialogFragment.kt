package com.lestrades.firebasevendedores

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.lestrades.firebasevendedores.databinding.FragmentDialogAddBinding

class AddDialogFragment : DialogFragment(), DialogInterface.OnShowListener {
    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null
    private var product: Product? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            binding = FragmentDialogAddBinding.inflate(LayoutInflater.from(context))
            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle("Agregar producto")
                    .setPositiveButton("Agregar", null)
                    .setNegativeButton("Cancelar", null)
                    .setView(it.root)
                val dialog = builder.create()
                dialog.setOnShowListener(this)

                return dialog
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onShow(dialogInterface: DialogInterface?) {
        initProduct()

        val dialog = dialog as? AlertDialog
        dialog?.let {
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                binding?.let {
                    val product = Product(
                        name = it.etName.text.toString().trim(),
                        description = it.etDescription.text.toString().trim(),
                        quantity = it.etQuantity.text.toString().toInt(),
                        price = it.etPrice.text.toString().toDouble()
                    )
                    save(product)
                }
            }
            negativeButton?.setOnClickListener {
                dismiss()
            }
        }
    }
    private fun initProduct(){
        product = (activity as? MainAux)?.getProductSelected()
        product?.let { product ->
            binding?.let{
                it.etName.setText(product.name)
                it.etDescription.setText(product.description)
                it.etQuantity.setText(product.quantity.toString())
                it.etPrice.setText(product.price.toString())
            }
        }
    }

    private fun save(product: Product) {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(activity, "Producto añadido", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error al insertar.", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                dismiss()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}