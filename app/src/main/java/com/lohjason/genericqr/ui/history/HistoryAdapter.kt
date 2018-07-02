package com.lohjason.genericqr.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.lohjason.genericqr.R
import com.lohjason.genericqr.db.HistoryEntry
import com.lohjason.genericqr.util.Logg
import java.text.SimpleDateFormat
import java.util.*

/**
 * HistoryAdapter
 * Created by jason on 26/6/18.
 */
class HistoryAdapter(entryList: ArrayList<HistoryEntry>, checkEntrySet: HashSet<Int>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val historyList = entryList
    private val checkedSet = checkEntrySet

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentEntry = historyList[position]
        holder.setData(currentEntry)
        if(checkedSet.contains(currentEntry.id)){
            holder.setChecked(true)
        }
        holder.itemView.setOnClickListener {
            currentEntry.id?.let {
                toggleViewHolderChecklist(holder, it)
            }
        }
        holder.itemView.setOnLongClickListener {
            copyToClipboard(it.context, currentEntry.data, currentEntry.type)
            true
        }
    }

    fun setAllChecked() {
        for(historyEntry in historyList){
            historyEntry.id?.let {
                Logg.d("+_", "Added item for id: $it")
                checkedSet.add(it)
            }
        }
        notifyDataSetChanged()
    }

    fun clearChecked(){
        checkedSet.clear()
        notifyDataSetChanged()
    }

    private fun toggleViewHolderChecklist(holder: HistoryViewHolder, id: Int){
        val checked = holder.toggleChecked()
        if(checked){
            Logg.d("+_", "Added item with id $id to set")
            checkedSet.add(id)
        } else {
            Logg.d("+_", "Removed item with id $id from set")
            checkedSet.remove(id)
        }
    }

    class HistoryViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        private var tvData: TextView = itemView!!.findViewById(R.id.tv_historyitem_data)
        private var tvType: TextView = itemView!!.findViewById(R.id.tv_historyitem_type)
        private var tvTimestamp: TextView = itemView!!.findViewById(R.id.tv_historyitem_timestamp)
        private var checkboxDelete: CheckBox = itemView!!.findViewById(R.id.checkbox_delete)


        fun setData(historyEntry: HistoryEntry){
            tvData.text = historyEntry.data
            tvType.text = historyEntry.type
            tvTimestamp.text = SimpleDateFormat("dd MMM YYYY", Locale.getDefault()).format(Date(historyEntry.timestamp))
            checkboxDelete.isChecked = false
        }

        fun setChecked(checked: Boolean) {
            checkboxDelete.isChecked = checked
        }

        fun toggleChecked(): Boolean {
            checkboxDelete.isChecked = !checkboxDelete.isChecked
            return checkboxDelete.isChecked
        }
    }


    private fun copyToClipboard(context: Context, text: String, label: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.primaryClip = clipData
        Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
    }

}