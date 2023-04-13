package com.example.stw_b1

import androidx.recyclerview.widget.RecyclerView
import com.example.stw_b1.databinding.CardCellBinding

class CardViewHolder(
    private val cardCellBinding: CardCellBinding,
    private val clickListener: RallyClickListener
    ) : RecyclerView.ViewHolder(cardCellBinding.root) {
    fun bindRally(rally: Rally){
        cardCellBinding.cover.setImageResource(rally.cover)
        cardCellBinding.title.text = rally.title
        cardCellBinding.description.text = rally.description
        cardCellBinding.cardView.setOnClickListener{
            clickListener.onClick(rally)
        }
    }
}
