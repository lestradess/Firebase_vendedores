package com.lestrades.firebasevendedores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.lestrades.firebasevendedores.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_LONG).show()
                }
            }else{
                //Con esto se manda respuesta en caso de que se pulse hacia atras y finaliza la app
                if (response == null){
                    Toast.makeText(this, "Hasta pronto",Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()
    }

    private fun configAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null) {
                supportActionBar?.title = it.currentUser?.displayName
                binding.tvInit.visibility = View.VISIBLE
            } else {
                val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

                resultLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sing_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesi??n terminada.", Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            binding.tvInit.visibility = View.GONE
                        }else{
                            Toast.makeText(this,"No se pudo cerrar la sesi??n",Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}