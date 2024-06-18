package com.viniciussantos.whatssap.utlis

import android.app.Activity
import android.widget.Toast

fun Activity.exibirMensagem(mensagem: String){
    // Exibir mensagem
    Toast.makeText(this,mensagem,Toast.LENGTH_LONG).show()
}