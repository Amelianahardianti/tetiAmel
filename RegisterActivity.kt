package com.example.amelteti

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.amelteti.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Observable

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

     //Auth
        auth=FirebaseAuth.getInstance()

        //fullname Validation
        val nameStream = RxTextView.textChanges(binding.etfullname)
            .skipInitialValue()
            .map { name ->
                name.isEmpty()
            }

        nameStream.subscribe { isEmpty: Boolean ->
            showNameExistAlert(isEmpty)
        }

        //Email Validation
                val emailStream = RxTextView.textChanges(binding.etEmail)
                    .skipInitialValue()
                    .map { email->
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    }
                emailStream.subscribe {
                    showEmailValidAlert(it)
                }

        //username Validation
        val usernameStream = RxTextView.textChanges((binding.etusername))
            .skipInitialValue()
            .map { username->
                username.length < 6
            }
        usernameStream.subscribe{
            showTextMinimalAlert(it,"username")
        }
        //Password Validation
        val passwordStream = RxTextView.textChanges((binding.etpassword))
            .skipInitialValue()
            .map { password->
                password.length < 6
            }
        passwordStream.subscribe{
            showTextMinimalAlert(it,"password")
        }
        //ConfirmPasswordValidation
        val passwordConfirmStream = Observable.combineLatest(
            RxTextView.textChanges(binding.ettpassword)
                .skipInitialValue()
                .map { password ->
                    password.toString() != binding.etpassword.text.toString()
                },
            RxTextView.textChanges(binding.ettpassword)
                .skipInitialValue()
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.etpassword.text.toString()
                },
            { password, confirmPassword -> password || confirmPassword }
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())

        passwordConfirmStream.subscribe {
            showPasswordConfirmAlert(it)
        }
//Button Enable True or False
        val invalidFieldStream = Observable.combineLatest(
            nameStream,
            emailStream,
            usernameStream,
            passwordStream,
            passwordConfirmStream,
        {nameInvalid : Boolean, emailInvalid:Boolean, usernameInvalid:Boolean, passwordInvalid:Boolean,passwordConfirmInvalid:Boolean->
                !nameInvalid && !emailInvalid && !usernameInvalid && !passwordInvalid && !passwordConfirmInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            if(isValid) {
                binding.btnregister.isEnabled = true
                binding.btnregister.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary)
            } else {
                binding.btnregister.isEnabled = false
                binding.btnregister.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            }
        }

            //click

        binding.btnregister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etpassword.text.toString().trim()
            registerUser(email,password)
        }
        binding.tvHaventAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showNameExistAlert(isNotValid: Boolean) {
        binding.etfullname.error = if (isNotValid) "Nama tidak boleh kosong!" else null
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if (text.equals("Username", ignoreCase = true))
            binding.etusername.error = if (isNotValid) "$text harus lebih dari 6 huruf" else null
        else if (text.equals("Password", ignoreCase = true))
            binding.etpassword.error = if (isNotValid) "$text harus lebih dari 8 huruf" else null
    }

    private fun showEmailValidAlert(isNotValid: Boolean) {
        binding.etEmail.error = if (isNotValid) " email tidak valid" else null
    }

    private fun showPasswordConfirmAlert(isNotValid: Boolean) {
        binding.ettpassword.error = if (isNotValid) "Password tidak sama!" else null
    }

    private fun registerUser(email:String, password:String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this,"Register Berhasil",Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }
}