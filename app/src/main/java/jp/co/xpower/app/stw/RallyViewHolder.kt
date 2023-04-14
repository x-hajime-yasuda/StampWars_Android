package jp.co.xpower.app.stw

import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.app.stw.databinding.RallyCellBinding

class RallyViewHolder(
    private val cardCellBinding: RallyCellBinding,
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

