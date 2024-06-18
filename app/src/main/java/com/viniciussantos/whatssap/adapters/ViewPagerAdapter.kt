package com.viniciussantos.whatssap.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.viniciussantos.whatssap.fragments.ContatosFragment
import com.viniciussantos.whatssap.fragments.ConversasFragment

// A classe ViewPagerAdapter é um adaptador personalizado que estende FragmentStateAdapter.
// Ela é usada para gerenciar quais fragmentos são exibidos em um ViewPager2.
class ViewPagerAdapter(
    // A lista de abas que o ViewPager2 irá exibir.
    private val abas: List<String>,
    // O FragmentManager é usado para gerenciar os fragmentos que o ViewPager2 exibe.
    fragmentManager: FragmentManager,
    // O Lifecycle é usado para gerenciar o ciclo de vida dos fragmentos que o ViewPager2 exibe.
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    // Retorna o número de abas que o ViewPager2 irá exibir.
    override fun getItemCount(): Int {
        return abas.size // 0 -> Conversas, 1 -> Contatos
    }

    // Cria o fragmento para a aba especificada.
    // A posição do fragmento na lista de abas determina qual fragmento é criado.
    override fun createFragment(position: Int): Fragment {
        // Se a posição for 1, o fragmento de Contatos é criado.
        when (position) {
            1 -> return ContatosFragment()
        }
        // Se a posição for qualquer outra (neste caso, só pode ser 0), o fragmento de Conversas é criado.
        return ConversasFragment()
    }
}