package com.example.stw_b1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stw_b1.databinding.StampCellBinding

class StampAdapter(
    private val stamps: List<Stamp>
    //,private val clickListener: StampClickListener?
) : RecyclerView.Adapter<StampViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StampViewHolder {
        var from = LayoutInflater.from(parent.context)
        var binding = StampCellBinding.inflate(from, parent, false)
        //return StampViewHolder(binding, clickListener)
        return StampViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return stamps.size
    }

    override fun onBindViewHolder(holder: StampViewHolder, position: Int) {
        holder.bindRally(stamps[position])
    }
}
