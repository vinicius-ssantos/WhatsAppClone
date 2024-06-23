package com.viniciussantos.whatssap.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Conversa(
    val idUsuarioRementente: String = "",
    val idUsuarioDestinatario: String = "",
    val foto: String = "",
    val nome: String = "",
    val ultimaMensagem: String = "",
    @ServerTimestamp // Anotação para pegar a data do servidor do Firebase Firestore
    val data: Date? = null
)