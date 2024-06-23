package com.viniciussantos.whatssap.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.viniciussantos.whatssap.R
import com.viniciussantos.whatssap.adapters.MensagensAdapter
import com.viniciussantos.whatssap.databinding.ActivityMensagensBinding
import com.viniciussantos.whatssap.model.Conversa
import com.viniciussantos.whatssap.model.Mensagem
import com.viniciussantos.whatssap.model.Usuario
import com.viniciussantos.whatssap.utlis.Constantes
import com.viniciussantos.whatssap.utlis.exibirMensagem

class MensagensActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMensagensBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var listenerRegistration: ListenerRegistration

    private var dadosDestinatario: Usuario? = null
    private var dadosUsuarioRemetente: Usuario? = null
    private lateinit var conversasAdapter: MensagensAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        recuperarDadosUsuarios()
        inicializarToolbar()
        inicializarEventosClick()
        inicializarRecyclerView()
        inicializarListeners()
    }

    private fun inicializarRecyclerView() {
        //Adapter
        with(binding) {
            conversasAdapter = MensagensAdapter()
            rvMensagens.adapter = conversasAdapter
            rvMensagens.layoutManager = LinearLayoutManager(applicationContext)

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListeners() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id
        if (idUsuarioRemetente != null && idUsuarioDestinatario != null) {

            // Listener para mensagens do destinatário para o remetente
            listenerRegistration =
                firestore
                    .collection(Constantes.MENSAGENS)
                    .document(idUsuarioRemetente)
                    .collection(idUsuarioDestinatario)
                    .orderBy("data", Query.Direction.ASCENDING)
                    .addSnapshotListener { querySnapshot, exception ->
                        if (exception != null) {
                            exibirMensagem("Erro ao recuperar mensagens")
                        }

                        val listaMensagens = mutableListOf<Mensagem>()
                        val documentos = querySnapshot?.documents

                        documentos?.forEach { documentSnapshot ->
                            val mensagem = documentSnapshot.toObject(Mensagem::class.java)
                            if (mensagem != null) {
                                listaMensagens.add(mensagem)
                                Log.i("exibicao_mensagens", mensagem.mensagem)
                            }
                        }

                        //Lista
                        if (listaMensagens.isNotEmpty()) {
                            //Carregar os dados Adapter
                            conversasAdapter.adicionarLista(listaMensagens)
                        }
                    }
        }
    }

    private fun inicializarEventosClick() {
        binding.fabEnviar.setOnClickListener() {
            val mensagem = binding.editMensagem.text.toString()
            salvarMensagem(mensagem)
        }
    }

    private fun salvarMensagem(textoMensagem: String) {
        if (textoMensagem.isNotEmpty()) {
            val idUsuarioRemetente = firebaseAuth.currentUser?.uid
            val idUsuarioDestinatario = dadosDestinatario?.id
            if (idUsuarioRemetente != null && idUsuarioDestinatario != null) {
                val mensagem = Mensagem(
                    idUsuarioRemetente, textoMensagem
                )

                // Salvar mensagem para o Remetente
                salvarMensagemFireStore(idUsuarioRemetente, idUsuarioDestinatario, mensagem)

                // criar conversa entre remetente e destinatario
                val conversaRemetente = Conversa(
                    idUsuarioRemetente,
                    idUsuarioDestinatario,
                    dadosDestinatario!!.foto,
                    dadosDestinatario!!.nome,
                    textoMensagem
                )
                // Salvar conversa para o Remetente no Firestore
                salvarConversaFirestore(conversaRemetente)

                // Salvar mensagem para o Destinatario
                salvarMensagemFireStore(idUsuarioDestinatario, idUsuarioRemetente, mensagem)

                // criar conversa entre destinatario e remetente
                val conversaDestinatario = Conversa(
                    idUsuarioDestinatario,
                    idUsuarioRemetente,
                    dadosUsuarioRemetente!!.foto,
                    dadosUsuarioRemetente!!.nome,
                    textoMensagem
                )

                // Salvar conversa para o Destinatario no Firestore
                salvarConversaFirestore(conversaDestinatario)
            }
        }
    }

    private fun salvarConversaFirestore(conversa: Conversa) {
        firestore
            .collection(Constantes.CONVERSAS)
            .document(conversa.idUsuarioRementente)
            .collection(Constantes.ULTIMAS_CONVERSAS)
            .document(conversa.idUsuarioDestinatario)
            .set(conversa).addOnFailureListener {
                exibirMensagem("Erro ao salvar conversa")
            }
    }

    private fun salvarMensagemFireStore(
        idUsuarioRemetente: String, idUsuarioDestinatario: String, mensagem: Mensagem
    ) {

        firestore.collection(Constantes.MENSAGENS).document(idUsuarioRemetente)
            .collection(idUsuarioDestinatario).add(mensagem).addOnFailureListener() {
                exibirMensagem("Erro ao enviar mensagem")
            }
    }


    private fun inicializarToolbar() {
        val toolbar = binding.tbMensagens
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            if (dadosDestinatario != null) {
                setDisplayHomeAsUpEnabled(true)
                binding.textNome.text = dadosDestinatario?.nome
                Picasso.get().load(dadosDestinatario!!.foto).into(binding.imageFotoPerfil)
            }
            setDisplayHomeAsUpEnabled(true)
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_principal, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.item_perfil -> {
                        startActivity(
                            Intent(
                                applicationContext, PerfilActivity::class.java
                            )
                        )
                    }

                    R.id.item_sair -> {

                    }
                }
                return true
            }
        })
    }

    private fun recuperarDadosUsuarios() {
        //recuperando dados usuário logado
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        if (dadosUsuarioRemetente != null) {
            if (idUsuarioRemetente != null) {
                firestore.collection(Constantes.USUARIOS).document(idUsuarioRemetente).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val usuario = documentSnapshot.toObject(Usuario::class.java)
                        if (usuario != null) {
                            dadosUsuarioRemetente = usuario
                        }
                    }.addOnFailureListener {
                        exibirMensagem("Erro ao recuperar dados do usuário")
                    }
            }
        }
        // recuperando dados destinatário
        val extras = intent.extras
        if (extras != null) {

            dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable("dadosDestinatario", Usuario::class.java)
            } else {
                extras.getParcelable("dadosDestinatario")

            }
        }
    }


}