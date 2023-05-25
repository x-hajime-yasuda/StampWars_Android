package jp.co.xpower.app.stw

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.app.stw.databinding.StampCellBinding

class StampViewHolder(
    private val stampCellBinding: StampCellBinding
    //, private val clickListener: StampClickListener
) : RecyclerView.ViewHolder(stampCellBinding.root) {
    fun bindRally(stamp: Stamp){
        // 未達成は背景なし
        if(stamp.cover == -1){
            stampCellBinding.cover.visibility = View.INVISIBLE
        }
        else {
            stampCellBinding.cover.setImageResource(stamp.cover)
        }
        //stampCellBinding.title.isSelected = true
        stampCellBinding.title.text = stamp.title
        //stampCellBinding.cardView.setOnClickListener{
        //    clickListener.onClick(stamp)
        //}
    }
}
