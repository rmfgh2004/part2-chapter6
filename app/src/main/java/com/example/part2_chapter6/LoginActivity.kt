package com.example.part2_chapter6

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.part2_chapter6.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.signUpButton.setOnClickListener { v ->
            val auth = Firebase.auth
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(this, "email or password isEmpty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "SignUp Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.wtf("MainActivity", task.exception.toString())
                        Toast.makeText(this, "SignUp Error", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.signInButton.setOnClickListener { v ->
            val auth = Firebase.auth
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(this, "email or password isEmpty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    val currentUser = Firebase.auth.currentUser
                    if (task.isSuccessful && currentUser != null) {
                        val userId = currentUser.uid

                        Firebase.messaging.token.addOnCompleteListener {
                            val token = it.result

                            val user = mutableMapOf<String, Any>()
                            user["userId"] = userId
                            user["username"] = email
                            user["fcmToken"] = token

                            Firebase.database.reference.child(Key.DB_USERS).child(userId).updateChildren(user)

                            Toast.makeText(this, "SignIn Success", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.wtf("MainActivity", task.exception.toString())
                        Toast.makeText(this, "SignIn Error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}