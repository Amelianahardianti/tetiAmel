package com.example.amelteti

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.amelteti.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var  auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Auth
        auth =FirebaseAuth.getInstance()

        //username Validation
        val usernameStream = RxTextView.textChanges((binding.etEmail))
            .skipInitialValue()
            .map { username->
                username.isEmpty()
            }
        usernameStream.subscribe{
            showTextMinimalAlert(it,"Email/username")
        }
        //Password Validation
        val passwordStream = RxTextView.textChanges((binding.etPassword))
            .skipInitialValue()
            .map { password->
                password.isEmpty()
            }
        passwordStream.subscribe{
            showTextMinimalAlert(it,"passsword")
        }
     //Button Enable True or false
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
            { usernameInvalid:Boolean, passwordInvalid:Boolean->
                 !usernameInvalid && !passwordInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            if(isValid) {
                binding.butLogin.isEnabled = true
                binding.butLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary)
            } else {
                binding.butLogin.isEnabled = false
                binding.butLogin.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            }
        }

        //Click
        binding.butLogin.setOnClickListener{
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            loginUser(email,password)
        }
        binding.tvHaventAccount.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }
        binding.tvForgotPw.setOnClickListener {
            startActivity(Intent(this,ResetPasswordActivty::class.java))
        }
    }
    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if (text == "Email/Username")
            binding.etEmail.error = if (isNotValid) "$text tidak boleh kosong" else null
        else if (text == "Password")
            binding.etPassword.error = if (isNotValid) "$text tidak boleh kosong" else null
    }
    private fun loginUser(email:String,password:String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){login ->
                if(login.isSuccessful){
                    Intent(this,HomeActivity::class.java).also{
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                        Toast.makeText(this,"Login berhasil!",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this,login.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }
}