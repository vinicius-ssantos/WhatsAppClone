package com.viniciussantos.whatssap.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.viniciussantos.whatssap.R
import com.viniciussantos.whatssap.databinding.ActivityMensagensBinding
import com.viniciussantos.whatssap.model.Usuario
import com.viniciussantos.whatssap.utlis.Constantes

class MensagensActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMensagensBinding.inflate(layoutInflater) }
    private var dadosDestinatario: Usuario? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        recuperarDadosUsuarioDestinatario()
        inicializarToolbar()
    }

    private fun inicializarToolbar() {
        val toolbar = binding.materialToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "WhatsApp"
        }

        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_principal, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.item_perfil -> {
                            startActivity(Intent(applicationContext, PerfilActivity::class.java))
                        }

                        R.id.item_sair -> {
                            deslogarUsuario()
                        }
                    }
                    return true
                }
            }
        )
    }

    private fun recuperarDadosUsuarioDestinatario() {
        val extras = intent.extras
        if (extras != null) {
            val origem = extras.getString("origem")
            if (origem.equals(Constantes.ORIGEM_CONTATO)) {
                dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable("dadosDestinatario", Usuario::class.java)
                } else {
                    extras.getParcelable("dadosDestinatario")

                }
            } else if (origem.equals(Constantes.ORIGEM_CONVERSA)) {

            }
        }
    }


}