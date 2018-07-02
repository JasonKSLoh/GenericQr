package com.lohjason.genericqr.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.google.android.gms.vision.barcode.Barcode
import com.lohjason.genericqr.db.HistoryDb
import com.lohjason.genericqr.db.HistoryEntry
import com.lohjason.genericqr.util.Logg
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicLong

/**
 * MainViewModel
 * Created by jason on 25/6/18.
 */
class MainViewModel : ViewModel() {

    enum class FRAGMENT(val value: String) {
        SCAN("scan_fragment"),
        RESULT("result_fragment"),
        HISTORY("history_fragment")
    }

    private val visionBarcodeLive: MutableLiveData<Barcode> = MutableLiveData()
    private val fragmentToShowLive: MutableLiveData<FRAGMENT> = MutableLiveData()
    private val compositeDisposable = CompositeDisposable()

    private val itemLastSaved: AtomicLong = AtomicLong(0L)
    private val debounceThreshold = 1000L


    init {
        visionBarcodeLive.value = null
        fragmentToShowLive.value = FRAGMENT.SCAN
    }

    fun getVisionBarcodeLive(): LiveData<Barcode> {
        return visionBarcodeLive
    }

    fun setVisionBarcode(barcode: Barcode) {
        visionBarcodeLive.postValue(barcode)
    }

    fun clearVisionBarcode() {
        visionBarcodeLive.postValue(null)
    }

    fun getFragmentToShowLive(): LiveData<FRAGMENT> {
        return fragmentToShowLive
    }

    fun setFragmentToShow(fragmentType: FRAGMENT) {
        fragmentToShowLive.postValue(fragmentType)
    }


    fun barcodeReceived(context: Context, data: String, type: String) {
        val historyEntry = HistoryEntry(null, data, type, System.currentTimeMillis())
        val dao = HistoryDb.getInstance(context).historyDao()
        val disposable = Single.fromCallable {
            if (historyEntry.timestamp >= itemLastSaved.get() + debounceThreshold) {
                itemLastSaved.set(historyEntry.timestamp)
                dao.insertOrUpdateItem(historyEntry)
            } else {
                itemLastSaved.set(historyEntry.timestamp)
                0
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                               if (it > 0) {
                                   Logg.d("+_", "Item saved")
                               } else {
                                   Logg.d("+_", "Item not saved")
                               }
                           }, {
                               Logg.d("+_", "Item not saved", it)
                           })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}