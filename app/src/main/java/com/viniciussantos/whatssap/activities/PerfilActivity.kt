package com.viniciussantos.whatssap.activities

import android.content.pm.PackageManager
import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.viniciussantos.whatssap.databinding.ActivityPerfilBinding
import com.viniciussantos.whatssap.utlis.exibirMensagem

class PerfilActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPerfilBinding.inflate( layoutInflater )
    }
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){ uri ->
        if( uri != null ){
            binding.imagePerfil.setImageURI( uri )
            uploadImagemStorage( uri )
        }else{
            exibirMensagem("Nenhuma imagem selecionada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        solicitarPermissao()
        inicializarEventosClick()
    }

    override fun onStart() {
        super.onStart()
        recuperarDadosIniciaisUsuario()
    }

    private fun recuperarDadosIniciaisUsuario() {
        var idUsuario = firebaseAuth.currentUser?.uid // id do usuario logado
        if (idUsuario != null) {

            firestore
                .collection("usuarios") // referencia para a colecao usuarios
                .document(idUsuario) // referencia para o documento do usuario logado
                .get() // recupera os dados do usuario
                .addOnSuccessListener { documentSnapshot ->
                    val dadosUsuario = documentSnapshot.data
                    if (dadosUsuario != null) {
                        val nome = dadosUsuario["nome"] as String
                        val foto = dadosUsuario["foto"] as String
                        binding.editNomePerfil.setText(nome)
                        if (foto.isNotEmpty()) {
                            Picasso
                                .get() // instancia do picasso
                                .load(foto) // carrega a imagem
                                .into(binding.imagePerfil) // exibe a imagem no imageview
                        }
                    }
                }

        }

    }
    private fun uploadImagemStorage(uri: Uri) {

        // fotos -> usuarios -> idUsuario -> perfil -> nomeImagem
        val idUsuario = firebaseAuth.currentUser?.uid // id do usuario logado
        if (idUsuario != null) {

            storage
                .getReference("fotos") // referencia do storage para a pasta fotos do usuario
                .child("usuarios") // referencia para a pasta usuarios
                .child(idUsuario) // referencia para a pasta do usuario logado no app (id)
                .child("perfil.jpg") // referencia para a pasta perfil e nome da imagem a ser salva no storage
                .putFile(uri) // "Realiza o upload da imagem no storage do Firebase utilizando a URI da imagem selecionada pelo usuário no dispositivo dele.
                .addOnSuccessListener { task -> // se o upload for realizado com sucesso

                    exibirMensagem("Upload realizado com sucesso")
                    task.metadata
                        ?.reference
                        ?.downloadUrl
                        ?.addOnSuccessListener { url ->

                            val dados = mapOf(
                                "foto" to url.toString()
                            )
                            atualizarDadosPerfil(idUsuario, dados)
                        }

                }.addOnFailureListener { // se o upload falhar
                    exibirMensagem("Falha ao realizar upload")
                }
        }
    }
    private fun inicializarEventosClick() {

        binding.fabSelecionar.setOnClickListener() {
            if (temPermissaoGaleria) {
                // abrir galeria
                gerenciadorGaleria.launch("image/*") // solicita a selecao de imagem
            } else {
                exibirMensagem("Permissão de galeria não concedida")
                solicitarPermissao()
            }
        }
        binding.btnAtualizarPerfil.setOnClickListener {
            val nomeUsuario = binding.editNomePerfil.text.toString()
            if (nomeUsuario.isNotEmpty()) {

                val idUsuario = firebaseAuth.currentUser?.uid
                if (idUsuario != null) {
                    val dados = mapOf(
                        "nome" to nomeUsuario
                    )
                    atualizarDadosPerfil(idUsuario, dados)
                }
            } else {
                exibirMensagem("Preencha o campo nome")
            }
        }
    }



    private fun atualizarDadosPerfil(idUsuario: String, dados: Map<String, String>) {

        firestore
            .collection("usuarios") // referencia para a colecao usuarios
            .document(idUsuario) // referencia para o documento do usuario logado
            .update(dados) // atualiza os dados do usuario
            .addOnSuccessListener {
                exibirMensagem("Dados atualizados com sucesso")
            }
            .addOnFailureListener {
                exibirMensagem("Falha ao atualizar dados")
            }


    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarPerfil.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Editar Perfil"
            // Só realiza a açaõ de voltar se a toolbar estiver habilitada
            // e o parentActivityName estiver definido no AndroidManifest
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun solicitarPermissao() {
        // verifica se tem permissao CAMERA
        temPermissaoCamera = ContextCompat.checkSelfPermission( // verifica se tem permissao
            this, // contexto atual da activity
            android.Manifest.permission.CAMERA // permissao que deseja verificar
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED // verifica se a permissao foi concedida

        // verifica se tem permissao GALERIA
        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED


        //LISTA DE PERMISSOES NEGADAS
        val listaPermissoesNegadas = mutableListOf<String>()
        // verifica se tem permissao CAMERA
        if (!temPermissaoCamera) { // se nao tem permissao
            listaPermissoesNegadas.add(android.Manifest.permission.CAMERA) // adiciona a permissao na lista de permissoes negadas
        }
        // verifica se tem permissao GALERIA
        if (!temPermissaoGaleria) {
            listaPermissoesNegadas.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        }

        if (listaPermissoesNegadas.isNotEmpty()) {
            //SOLICITAR MULTIPLAS PERMISSOES
            val gerenciadorPermissoes =
                registerForActivityResult( // registra um contrato de permissao de multiplas permissoes
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissoes -> // recebe um mapa de permissoes
                    temPermissaoCamera =
                        permissoes[android.Manifest.permission.CAMERA] ?: temPermissaoCamera
                    temPermissaoGaleria =
                        permissoes[android.Manifest.permission.READ_MEDIA_IMAGES]
                            ?: temPermissaoGaleria
                }
            gerenciadorPermissoes
                .launch(listaPermissoesNegadas.toTypedArray()) // solicita as permissoes negadas na lista
        }
    }
}