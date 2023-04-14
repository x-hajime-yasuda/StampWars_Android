package jp.co.xpower.app.stw

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.app.stw.databinding.RallyCellBinding

class RallyAdapter(
    private val rallys: List<Rally>,
    private val clickListener: RallyClickListener
) : RecyclerView.Adapter<RallyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RallyViewHolder {
        var from = LayoutInflater.from(parent.context)
        var binding = RallyCellBinding.inflate(from, parent, false)
        return RallyViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int {
        return rallys.size
    }

    override fun onBindViewHolder(holder: RallyViewHolder, position: Int) {
        holder.bindRally(rallys[position])
    }
}
