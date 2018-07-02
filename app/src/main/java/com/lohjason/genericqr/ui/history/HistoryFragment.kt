package com.lohjason.genericqr.ui.history

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lohjason.genericqr.R
import com.lohjason.genericqr.db.HistoryEntry
import kotlinx.android.synthetic.main.fragment_history.*

/**
 * HistoryFragment
 * Created by jason on 26/6/18.
 */
class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var tvSelectAll: TextView
    private lateinit var tvDelete: TextView
    private lateinit var tvNumItems: TextView

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyEntries: ArrayList<HistoryEntry>
    private lateinit var checkedEntries: HashSet<Int>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        historyViewModel = ViewModelProviders.of(requireActivity()).get(HistoryViewModel::class.java)
        historyEntries = historyViewModel.getEntriesLiveData().value!!
        checkedEntries = historyViewModel.getCheckedEntries()
        setupViews()
        setupObservers()
        historyAdapter.notifyDataSetChanged()
    }

    private fun setupObservers() {
//        val entriesObserver = Observer<ArrayList<HistoryEntry>> {
//            it?.let {
//                Logg.d("+_", "History Entries Observed changed to size: ${it.size}")
//                historyAdapter.notifyDataSetChanged()
//            }
//        }
//        historyViewModel.getEntriesLiveData().observe(this, entriesObserver)

        val refreshObserver = Observer<Boolean> {
            it?.let {
                val numItemsText = ": ${historyEntries.size} items"
                tvNumItems.text = numItemsText
                historyAdapter.notifyDataSetChanged()
            }
        }
        historyViewModel.getNotifyRefreshLiveData().observe(this@HistoryFragment, refreshObserver)
    }

    private fun setupViews() {
        tvNumItems = tv_history_toolbar_num_items
        tvDelete = tv_history_delete_entries
        tvSelectAll = tv_history_select_all
        rvHistory = rv_history
        historyAdapter = HistoryAdapter(historyEntries, checkedEntries)

        tvDelete.setOnClickListener {
            historyViewModel.deleteSelectedEntries(requireContext())
            historyAdapter.notifyDataSetChanged()
        }

        tvSelectAll.setOnClickListener {
            if (!historyViewModel.isAllChecked()) {
                historyAdapter.setAllChecked()
            } else {
                historyAdapter.clearChecked()
            }
        }

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvHistory.layoutManager = layoutManager
//        rvHistory.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        rvHistory.adapter = historyAdapter

        historyViewModel.updateHistoryEntries(requireContext())
    }

}