package com.lestrades.firebasevendedores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject


import com.lestrades.firebasevendedores.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), OnProductLisener, MainAux {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var adapter: ProductAdapter
    private lateinit var firestoreListener: ListenerRegistration
    private var productSelect: Product? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_LONG).show()
                }
            } else {
                //Con esto se manda respuesta en caso de que se pulse hacia atras y finaliza la app
                if (response == null) {
                    Toast.makeText(this, "Hasta pronto", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    response.error?.let {
                        if (it.errorCode == ErrorCodes.NO_NETWORK) {
                            Toast.makeText(
                                this,
                                "Código de error : ${it.errorCode}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()
        configRecyclerView()
        //configFirestore() //Carga el listado pero solo una vez, no actualiza
        //configFirestoreRealtime()// añadido en el onResume
        configButtons()
    }


    private fun configAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null) {
                supportActionBar?.title = it.currentUser?.displayName
                binding.llProgress.visibility = View.GONE
                binding.nsvProducts.visibility = View.VISIBLE
                binding.efab.show()

            } else {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )
                resultLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        configFirestoreRealtime()
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        firestoreListener.remove()
    }

    private fun configRecyclerView() {
        adapter = ProductAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(
                this@MainActivity,
                3,
                GridLayoutManager.HORIZONTAL, false
            )
            adapter = this@MainActivity.adapter
        }
//        (1..20).forEach {
//            val product = Product(
//                it.toString(), "Producto $it",
//                "Este producto es el $it", "",
//                it, it * 1.1
//            )
//            adapter.add(product)
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sing_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesión terminada.", Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            binding.llProgress.visibility = View.VISIBLE
                            binding.nsvProducts.visibility = View.GONE
                            binding.efab.hide()
                        } else {
                            Toast.makeText(this, "No se pudo cerrar la sesión", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val product = document.toObject(Product::class.java)
                    product.id = document.id
                    adapter.add(product)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar datos.", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun configFirestoreRealtime() {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection("products")

        firestoreListener = productRef.addSnapshotListener { values, error ->
            if (error != null) {
                Toast.makeText(this, "Error al consultar datos.", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            for (value in values!!.documentChanges) {
                val product = value.document.toObject(Product::class.java)
                product.id = value.document.id
                when (value.type) {
                    DocumentChange.Type.ADDED -> adapter.add(product)
                    DocumentChange.Type.MODIFIED -> adapter.update(product)
                    DocumentChange.Type.REMOVED -> adapter.delete(product)
                }
            }

        }
    }

    private fun configButtons() {
        binding.efab.setOnClickListener {
            productSelect = null
            AddDialogFragment().show(
                supportFragmentManager,
                AddDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onClick(product: Product) {
        productSelect = product
        AddDialogFragment().show(
            supportFragmentManager,
            AddDialogFragment::class.java.simpleName
        )
    }

    override fun onLongClick(product: Product) {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection("products")
        product.id?.let {
            productRef.document(it)
                .delete()
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar el producto.", Toast.LENGTH_LONG).show()
                }
        }
    }
    override fun getProductSelected(): Product? = productSelect
}

