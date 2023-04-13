package com.example.stw_b1

import androidx.recyclerview.widget.RecyclerView
import com.example.stw_b1.databinding.StampCellBinding

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
