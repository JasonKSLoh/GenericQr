package com.lohjason.genericqr.ui.history

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.widget.Toast
import com.lohjason.genericqr.db.HistoryDb
import com.lohjason.genericqr.db.HistoryEntry
import com.lohjason.genericqr.util.Logg
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * HistoryViewModel
 * Created by jason on 27/6/18.
 */
@Suppress("unused")
class HistoryViewModel : ViewModel() {

    private val entriesLiveData: MutableLiveData<ArrayList<HistoryEntry>> = MutableLiveData()
    private val checkedEntries = HashSet<Int>()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val notifyRefreshLiveData: MutableLiveData<Boolean> = MutableLiveData()

    init {
        entriesLiveData.value = ArrayList()
        notifyRefreshLiveData.value = null
    }

    fun getNotifyRefreshLiveData(): LiveData<Boolean>{
        return notifyRefreshLiveData
    }

    fun getEntriesLiveData(): LiveData<ArrayList<HistoryEntry>> {
        return entriesLiveData
    }

    fun getCheckedEntries(): HashSet<Int> {
        return checkedEntries
    }

    fun isAllChecked(): Boolean {
        return entriesLiveData.value?.size == checkedEntries.size
    }

    private fun sendNotifyRefresh() {
        val newVal = false
        notifyRefreshLiveData.value?.let {
            !it
        }
        notifyRefreshLiveData.postValue(newVal)
    }

    fun updateHistoryEntries(context: Context) {
        val dao = HistoryDb.getInstance(context).historyDao()
        val disposable = dao.getAllEntries().subscribeOn(Schedulers.io())
                .onErrorReturn {
                    Logg.d("+_", it.message, it)
                    ArrayList()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ entries ->
                               Logg.d("+_", "Found ${entries.size} entries in the DB")
                               entriesLiveData.value?.let {
                                   it.clear()
                                   it.addAll(entries)
                               }
                               sendNotifyRefresh()
                           }, {
                               val errMsg = "An error has occurred in fetching the entries"
                               Logg.d("+_", errMsg, it)
                               Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                               entriesLiveData.value?.clear()
                               sendNotifyRefresh()
                           })
        compositeDisposable.add(disposable)
    }

    fun deleteEntry(context: Context, historyEntry: HistoryEntry) {
        val dao = HistoryDb.getInstance(context).historyDao()
        val disposable = Single.fromCallable { dao.deleteItem(historyEntry) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                               if (it >= 0) {
                                   entriesLiveData.value?.remove(historyEntry)
                               } else {
                                   val errMsg = "An error has occurred in deleting the entry"
                                   Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                               }
                               checkedEntries.clear()
                               sendNotifyRefresh()
                           }, {
                               val errMsg = "An error has occurred in deleting the entry"
                               Logg.d("+_", errMsg, it)
                               Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                               checkedEntries.clear()
                               sendNotifyRefresh()
                           })
        compositeDisposable.add(disposable)
    }

    fun deleteSelectedEntries(context: Context) {
        val dao = HistoryDb.getInstance(context).historyDao()
        val idList = ArrayList<Int>()
        idList.addAll(checkedEntries)
        val disposable = Single.fromCallable { dao.deleteItemsById(idList) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                               if (it >= 0) {
                                   val iterator = entriesLiveData.value?.iterator()
                                   iterator?.let {
                                       while (it.hasNext()) {
                                           val entry = it.next()
                                           if (checkedEntries.contains(entry.id)) {
                                               it.remove()
                                           }
                                       }
                                   }
                                   Toast.makeText(context, "Removed: $it items", Toast.LENGTH_SHORT).show()
                               } else {
                                   val errMsg = "An error has occurred in deleting the entries"
                                   Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                               }
                               checkedEntries.clear()
                               sendNotifyRefresh()
                           }, {
                               val errMsg = "An error has occurred in deleting the entries"
                               Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                               Logg.d("+_", errMsg, it)
                               checkedEntries.clear()
                               sendNotifyRefresh()
                           })
        compositeDisposable.add(disposable)
    }

    fun deleteAllEntries(context: Context) {
        val dao = HistoryDb.getInstance(context).historyDao()
        val disposable = Single.fromCallable { dao.deleteAllItems() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                               checkedEntries.clear()
                               if (it >= 0) {
                                   entriesLiveData.value?.clear()
                                   val msg = "Deleted $it entries"
                                   Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                               } else {
                                   val errMsg = "An error has occurred in deleting all entries"
                                   Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                               }
                               sendNotifyRefresh()
                           }, {
                               checkedEntries.clear()
                               val errMsg = "An error has occurred in deleting all entries"
                               Logg.d("+_", errMsg)
                               Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                               sendNotifyRefresh()
                           })
        compositeDisposable.addAll(disposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}