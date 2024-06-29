package com.viniciussantos.whatssap.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.viniciussantos.whatssap.R
import com.viniciussantos.whatssap.activities.MensagensActivity
import com.viniciussantos.whatssap.adapters.ContatosAdapter
import com.viniciussantos.whatssap.adapters.ConversasAdapter
import com.viniciussantos.whatssap.databinding.FragmentContatosBinding
import com.viniciussantos.whatssap.databinding.FragmentConversasBinding
import com.viniciussantos.whatssap.model.Conversa
import com.viniciussantos.whatssap.model.Usuario
import com.viniciussantos.whatssap.utlis.Constantes
import com.viniciussantos.whatssap.utlis.exibirMensagem


class ConversasFragment : Fragment() {

    private lateinit var binding: FragmentConversasBinding
    private lateinit var eventoSnapshot: ListenerRegistration

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var conversasAdapter: ConversasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversasBinding.inflate(
            inflater,
            container,
            false
        )

        conversasAdapter = ConversasAdapter { conversa ->
            val intent = Intent(context, MensagensActivity::class.java) //
            val usuario = Usuario(
                id = conversa.idUsuarioDestinatario,
                nome = conversa.nome,
                foto = conversa.foto

            )
            intent.putExtra("dadosDestinatario", usuario)
//            intent.putExtra("origem", Constantes.ORIGEM_CONVERSA)
            startActivity(intent)
        }
        binding.rvConversas.adapter = conversasAdapter
        binding.rvConversas.layoutManager = LinearLayoutManager(context)
        binding.rvConversas.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

        return binding.root

    }

    override fun onStart() {
        super.onStart()
        adicionarListernerConversas()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }

    private fun adicionarListernerConversas() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid // id do usuário logado
        if (idUsuarioRemetente != null) { // Verifica se o id do usuário logado é diferente de nulo
            eventoSnapshot = firestore // Acessa o Firestore do Firebase para buscar as conversas
                .collection(Constantes.CONVERSAS) // Acessa a coleção de conversas
                .document(idUsuarioRemetente) // Busca o documento do usuário logado
                .collection(Constantes.ULTIMAS_CONVERSAS) // Acessa a coleção de últimas conversas
                .orderBy("data", Query.Direction.DESCENDING) // Ordena as conversas pela data de forma decrescente
                .addSnapshotListener { querySnapshot, exception ->

                    if (exception != null) {
                        activity?.exibirMensagem("Erro ao recuperar conversas")
                    }

                    val listaConversas = mutableListOf<Conversa>()
                    val documentos = querySnapshot?.documents

                    documentos?.forEach { documentSnapshot ->
                        val conversa = documentSnapshot.toObject(Conversa::class.java)
                        if (conversa != null) {
                            listaConversas.add(conversa)
                            Log.i(
                                "exibicao_conversas",
                                "${conversa.nome} - ${conversa.ultimaMensagem}"
                            )
                        }
                    }

                    if (listaConversas.isNotEmpty()) {
                        //Atualiza a lista de conversas (adapter)
                        conversasAdapter.adicionarLista(listaConversas)

                    }


                }
        }
    }
}