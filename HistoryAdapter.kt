package com.evilcalc.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val items: MutableList<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expr: TextView = view.findViewById(R.id.historyExpr)
        val result: TextView = view.findViewById(R.id.historyResult)
        val delete: Button = view.findViewById(R.id.historyDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.expr.text = item.expression
        val percentSuffix = if (item.percentAmount != null) " [${item.percentAmount}]" else ""
        holder.result.text = "${item.result}$percentSuffix"

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.delete.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount() = items.size
}
