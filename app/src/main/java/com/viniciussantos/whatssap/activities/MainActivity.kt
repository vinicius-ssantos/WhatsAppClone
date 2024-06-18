package com.viniciussantos.whatssap.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.viniciussantos.whatssap.R
import com.viniciussantos.whatssap.adapters.ViewPagerAdapter
import com.viniciussantos.whatssap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        inicializarNavegacaoAbas()
    }

    private fun inicializarNavegacaoAbas() {
        val tabLayout = binding.tabLayoutPrincipal
        val viewPager = binding.viewPagerPrincipal

        // Adapter
        val abas = listOf("CONVERSAS", "CONTATOS")
        viewPager.adapter = ViewPagerAdapter(
            abas,
            supportFragmentManager,
            lifecycle
        )

        tabLayout.isTabIndicatorFullWidth = true
        TabLayoutMediator(tabLayout, viewPager) { aba, posicao ->
            aba.text = abas[posicao]
        }.attach()
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeMainToolbar.tbPrincipal
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

    private fun deslogarUsuario() {
        AlertDialog.Builder(this)
            .setTitle("Deslogar")
            .setMessage("Deseja realmente sair?")
            .setPositiveButton("Sim") { dialog, _ ->
                firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Encerra a activity atual
                dialog.dismiss() // Fecha o dialog
            }
            .setNegativeButton("Não") { dialog, posicao ->
                dialog.dismiss()
            }.create().show()

    }
}