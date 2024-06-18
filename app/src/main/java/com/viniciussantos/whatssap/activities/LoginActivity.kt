package com.viniciussantos.whatssap.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.viniciussantos.whatssap.databinding.ActivityLoginBinding
import com.viniciussantos.whatssap.utlis.exibirMensagem

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var email: String
    private lateinit var senha: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarEventosClick()

    }

    override fun onStart() {
        super.onStart()
       verificarUsuarioLogado()
    }




    private fun inicializarEventosClick() {
        binding.textCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
        binding.btnLogar.setOnClickListener {
            if (validarCampo()){
                logarUsuario()
            }
        }
    }



    private fun validarCampo(): Boolean {
        email = binding.editLoginEmail.text.toString()
        senha = binding.editLoginSenha.text.toString()
        if (email.isNotEmpty()) {
            binding.textInputLayoutLoginEmail.error = null
            if (senha.isNotEmpty()) {
                binding.textInputLayoutLoginSenha.error = null
                return true
            } else {
                binding.textInputLayoutLoginSenha.error = "Preencha o campo senha"
                return false
            }

        } else {
            binding.textInputLayoutLoginEmail.error = "Preencha o campo e-mail"
            return false
        }
        return true
    }

    private fun logarUsuario() {
        firebaseAuth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener {
                exibirMensagem("Usuario logado com sucesso")
                startActivity(Intent(this, MainActivity::class.java))
            }.addOnFailureListener(){error ->
                try{
                    throw error
                }catch (erroUsuarioInvalido: FirebaseAuthInvalidUserException){
                    erroUsuarioInvalido.printStackTrace()
                    exibirMensagem("E-mail não cadastrado")
                }catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException){
                    erroCredenciaisInvalidas.printStackTrace()
                    exibirMensagem("E-mail ou senha estão incorretos")
                }
            }
    }

    private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser
        if (usuarioAtual != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}