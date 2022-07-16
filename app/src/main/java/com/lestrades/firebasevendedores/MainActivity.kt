package com.lestrades.firebasevendedores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configAuth()
    }

    private fun configAuth(){
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null){
                supportActionBar?.title = it.currentUser?.displayName
            }else{
                val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val response = IdpResponse.fromResultIntent(it.data)

                    if (it.resultCode == RESULT_OK) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            Toast.makeText(this, "Bienvenido", Toast.LENGTH_LONG).show()
                        }
                    }
                }.launch(
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
        firebaseAuth.addAuthStateListener (authStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener (authStateListener)
    }
}