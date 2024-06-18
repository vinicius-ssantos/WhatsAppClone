package com.viniciussantos.whatssap.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.viniciussantos.whatssap.databinding.ActivityCadastroBinding
import com.viniciussantos.whatssap.model.Usuario
import com.viniciussantos.whatssap.utlis.exibirMensagem

class CadastroActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCadastroBinding.inflate(layoutInflater) }
    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        inicializarEventosClick()

    }
    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça seu cadastro"
            // Só realiza a açaõ de voltar se a toolbar estiver habilitada
            //
            // e o parentActivityName estiver definido no AndroidManifest
            setDisplayHomeAsUpEnabled(true)
        }
    }
    private fun inicializarEventosClick() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                // criar Usuarios
                cadastrarUsuario(nome, email, senha)
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { resultado ->
                if (resultado.isSuccessful) {
                    // Salvar os dados no firestore
                    val idusuario = resultado.result.user?.uid
                    if (idusuario != null) {
                        val usuario = Usuario(idusuario, nome, email)
                        salvarUsuaiorFirestore(usuario)
                    }
                }
            }.addOnFailureListener { erro ->
                try {
                    throw erro
                } catch (erroSenhaFraca: FirebaseAuthWeakPasswordException) {
                    erroSenhaFraca.printStackTrace()
                    exibirMensagem("Senha fraca, digite outra com numeros e letras")
                } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException) {
                    erroUsuarioExistente.printStackTrace()
                    exibirMensagem("E-mail já cadastrado, digite outro email")
                } catch (erroCredenciasInvalidas: FirebaseAuthInvalidCredentialsException) {
                    erroCredenciasInvalidas.printStackTrace()
                    exibirMensagem("E-mail inválido, digite outro email")
                }
            }
    }

    private fun salvarUsuaiorFirestore(usuario: Usuario) {
        firestore.collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
                exibirMensagem("Sucesso ao salvar usuario")
            }.addOnFailureListener {
                exibirMensagem("Erro ao salvar usuario")
            }
    }

    private fun validarCampos(): Boolean {
        nome = binding.editTextNome.text.toString()
        email = binding.editTextEmail.text.toString()
        senha = binding.editTextSenha.text.toString()
        if (nome.isNotEmpty()) {
            binding.textInputLayoutNome.error = null

            if (email.isNotEmpty()) {
                if (senha.isNotEmpty()) {
                    binding.textInputLayoutSenha.error = null
                    return true
                } else {
                    binding.textInputLayoutSenha.error = "Preencha o campo senha"
                    return false
                }
            } else {
                binding.textInputLayoutEmail.error = "Preencha o campo email"
                return false
            }
        } else {
            binding.textInputLayoutNome.error = "Preencha o campo nome"
            return false
        }
    }


}