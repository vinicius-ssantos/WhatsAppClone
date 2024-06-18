package com.viniciussantos.whatssap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize // anotação para transformar a classe em parcelable (serializável) para passar dados entre activities
data class Usuario(
    var id: String = "",
    var nome: String = "",
    var email: String = "",
    var foto: String = ""
): Parcelable // implementa a interface Parcelable para ser serializável
