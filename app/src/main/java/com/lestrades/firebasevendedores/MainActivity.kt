package com.lestrades.firebasevendedores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.lestrades.firebasevendedores.databinding.ActivityMainBinding
import java.lang.Error


class MainActivity : AppCompatActivity(), OnProductLisener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var adapter: ProductAdapter

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
                }else{
                    response.error?.let{
                        if(it.errorCode == ErrorCodes.NO_NETWORK){
                            Toast.makeText(this,"Código de error : ${it.errorCode}",Toast.LENGTH_LONG).show()
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
    }



    private fun configAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null) {
                supportActionBar?.title = it.currentUser?.displayName
                binding.llProgress.visibility = View.GONE
                binding.nsvProducts.visibility = View.VISIBLE

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
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    private fun configRecyclerView() {
        adapter = ProductAdapter(mutableListOf(),this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity,
                3,
                GridLayoutManager.HORIZONTAL,false)
            adapter = this@MainActivity.adapter
        }
        (1..20).forEach {
            val product = Product(it.toString(),"Producto $it",
            "Este producto es el $it","",
            it,it * 1.1)
            adapter.add(product)
        }
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
                        } else {
                            Toast.makeText(this, "No se pudo cerrar la sesión", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(product: Product) {
        TODO("Not yet implemented")
    }

    override fun onLongClick(product: Product) {
        TODO("Not yet implemented")
    }
}