package jp.co.xpower.cotamp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.cotamp.databinding.StampCellBinding

class StampAdapter(
    private var stamps: List<Stamp>
    //,private val clickListener: StampClickListener?
) : RecyclerView.Adapter<StampViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StampViewHolder {
        var from = LayoutInflater.from(parent.context)
        var binding = StampCellBinding.inflate(from, parent, false)
        //return StampViewHolder(binding, clickListener)
        return StampViewHolder(binding)
    }

    fun setData(r:List<Stamp>){
        stamps = r
    }

    override fun getItemCount(): Int {
        return stamps.size
    }

    override fun onBindViewHolder(holder: StampViewHolder, position: Int) {
        holder.bindRally(stamps[position])
    }
}
