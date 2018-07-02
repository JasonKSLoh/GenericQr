package com.lohjason.genericqr.ui.results

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.vision.barcode.Barcode
import com.lohjason.genericqr.R
import com.lohjason.genericqr.ui.MainViewModel
import com.lohjason.genericqr.util.ClassificationUtils
import com.lohjason.genericqr.util.FileProviderUtils
import com.lohjason.genericqr.util.IntentUtils
import kotlinx.android.synthetic.main.fragment_results.*

/**
 * ResultsFragment
 * Created by jason on 25/6/18.
 */
class ResultsFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var btnHistory: TextView
    private lateinit var btnEmail: TextView
    private lateinit var btnSaveContact: TextView
    private lateinit var btnCall: TextView
    private lateinit var btnMessage: TextView
    private lateinit var btnUrl: TextView
    private lateinit var btnCopyClipboard: TextView
    private lateinit var btnSearch: TextView
    private lateinit var btnShareImage: TextView
    private lateinit var tvData: TextView
    private lateinit var tvType: TextView

    private lateinit var barcode: Barcode

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
        }
        setObservers()
    }

    private fun setupViews() {
        btnHistory = btn_history
        btnEmail = btn_email
        btnSaveContact = btn_save_contact
        btnCall = btn_call
        btnMessage = btn_message
        btnUrl = btn_url
        btnSearch = btn_search
        btnShareImage = btn_share_img
        tvData = tv_data
        tvType = tv_type
        btnCopyClipboard = btn_copy_clipboard

        tvData.movementMethod = ScrollingMovementMethod()

        btnHistory.setOnClickListener {
            mainViewModel.clearVisionBarcode()
            mainViewModel.setFragmentToShow(MainViewModel.FRAGMENT.HISTORY)
        }
        btnEmail.setOnClickListener { broadcastEmailIntent() }
        btnCall.setOnClickListener { broadcastCallIntent() }
        btnMessage.setOnClickListener { broadcastMessageIntent() }
        btnUrl.setOnClickListener { broadcastUrlIntent() }
        btnSaveContact.setOnClickListener { broadcastSaveContactIntent() }
        btnSearch.setOnClickListener { broadcastWebSearchIntent() }
        btnShareImage.setOnClickListener { broadcastShareImageIntent() }
        btnCopyClipboard.setOnClickListener {
            val dataFormatText = ClassificationUtils.getBarcodeDataFormatString(barcode.valueFormat)
            copyToClipboard(barcode.rawValue, dataFormatText)
        }
    }


    private fun setObservers() {
        val barcodeObserver = Observer<Barcode> {
            it?.let {
                barcode = it
                if (it.valueFormat == Barcode.TEXT) {
                    barcode.valueFormat = ClassificationUtils.checkDataType(barcode.rawValue)
                }
                val dataFormatText = ClassificationUtils.getBarcodeDataFormatString(it.valueFormat)
                val barcodeFormatText = ClassificationUtils.getBarcodeFormatString(it.format)
                copyToClipboard(it.rawValue, dataFormatText)
                val typeText = "$barcodeFormatText : $dataFormatText"
                tvType.text = typeText
                tvData.text = it.rawValue
                setViewVisibility(it.valueFormat, it.format)

                mainViewModel.barcodeReceived(requireContext(), it.rawValue, typeText)
            }
        }
        mainViewModel.getVisionBarcodeLive().observe(this@ResultsFragment, barcodeObserver)
    }


    private fun broadcastWebSearchIntent() {
        val intent = IntentUtils.getWebSearchIntent(requireContext(), barcode.rawValue)
        startActivity(intent)
    }

    private fun broadcastShareImageIntent() {
        val intent = FileProviderUtils.getShareBitmapIntent(requireContext())
        intent?.let {
            startActivity(intent)
            return
        }
    }

    private fun broadcastUrlIntent() {
        var url = if (barcode.url.url.isNotBlank()) {
            barcode.url.url
        } else {
            barcode.rawValue.trim()
        }
        if (!url.startsWith("http")) {
            url = "http://$url"
        }
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun broadcastEmailIntent() {
        val uri = Uri.parse("mailto:${barcode.email.address}")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(Intent.createChooser(intent, "Send Email"))
    }

    private fun broadcastCallIntent() {
        var phoneNumber = barcode.rawValue
        barcode.sms?.let {
            phoneNumber = it.phoneNumber
        }
        barcode.phone?.let {
            phoneNumber = it.number
        }
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        startActivity(intent)
    }

    private fun broadcastMessageIntent() {
        var phoneNumber = barcode.rawValue
        barcode.sms?.let {
            phoneNumber = it.phoneNumber
        }
        barcode.phone?.let {
            phoneNumber = it.number
        }
        val uri = Uri.fromParts("sms", phoneNumber, null)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun broadcastSaveContactIntent() {
        barcode.let {
            it.contactInfo?.let {
                val intent = IntentUtils.getSaveContactIntent(it)
                startActivity(intent)
                return
            }
        }
        if (barcode.rawValue.matches(android.util.Patterns.PHONE.toRegex())) {
            val intent = IntentUtils.getSavePhoneNumberContactIntent(barcode.rawValue.trim())
            startActivity(intent)
            return
        }

        Toast.makeText(requireContext(), "Invalid Contact :(", Toast.LENGTH_SHORT).show()
    }


    private fun setViewVisibility(dataType: Int, barcodeFormat: Int) {
        btnEmail.visibility = View.GONE
        btnSaveContact.visibility = View.GONE
        btnCall.visibility = View.GONE
        btnMessage.visibility = View.GONE
        btnUrl.visibility = View.GONE


        when (barcodeFormat) {
            Barcode.CODE_128,
            Barcode.CODABAR,
            Barcode.CODE_39,
            Barcode.CODE_93,
            Barcode.EAN_13,
            Barcode.EAN_8,
            Barcode.ITF,
            Barcode.UPC_A,
            Barcode.UPC_E -> {
                return
            }
        }

        when (dataType) {
            Barcode.EMAIL -> {
                btnEmail.visibility = View.VISIBLE
            }
            Barcode.URL -> {
                btnUrl.visibility = View.VISIBLE
            }
            Barcode.PHONE,
            Barcode.SMS -> {
                btnCall.visibility = View.VISIBLE
                btnMessage.visibility = View.VISIBLE
                btnSaveContact.visibility = View.VISIBLE
            }
            Barcode.CONTACT_INFO -> {
                btnSaveContact.visibility = View.VISIBLE
            }
            else -> {

            }
        }
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.primaryClip = clipData
        Toast.makeText(requireContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show()
    }

}