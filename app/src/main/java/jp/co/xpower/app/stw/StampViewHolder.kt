package jp.co.xpower.app.stw

import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.app.stw.databinding.StampCellBinding

class StampViewHolder(
    private val stampCellBinding: StampCellBinding
    //, private val clickListener: StampClickListener
) : RecyclerView.ViewHolder(stampCellBinding.root) {
    fun bindRally(stamp: Stamp){
        stampCellBinding.cover.setImageResource(stamp.cover)
        stampCellBinding.title.text = stamp.title
        //stampCellBinding.cardView.setOnClickListener{
        //    clickListener.onClick(stamp)
        //}
    }
}
