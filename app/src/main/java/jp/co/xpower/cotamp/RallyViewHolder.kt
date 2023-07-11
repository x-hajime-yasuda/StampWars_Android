package jp.co.xpower.cotamp

import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.cotamp.databinding.RallyCellBinding

class RallyViewHolder(
    private val cardCellBinding: RallyCellBinding,
    private val clickListener: RallyClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {
    fun bindRally(rally: Rally){
        cardCellBinding.cover.setImageResource(rally.cover)
        cardCellBinding.title.text = rally.title
        cardCellBinding.description.text = rally.detail
        cardCellBinding.cardView.setOnClickListener{
            clickListener.onClick(rally)
        }
    }
}
