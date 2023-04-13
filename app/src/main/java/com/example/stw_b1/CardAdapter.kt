package com.example.stw_b1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stw_b1.databinding.CardCellBinding

class CardAdapter(
        private val rallys: List<Rally>,
        private val clickListener: RallyClickListener
    ) : RecyclerView.Adapter<CardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        var from = LayoutInflater.from(parent.context)
        var binding = CardCellBinding.inflate(from, parent, false)
        return CardViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int {
        return rallys.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bindRally(rallys[position])
    }
}