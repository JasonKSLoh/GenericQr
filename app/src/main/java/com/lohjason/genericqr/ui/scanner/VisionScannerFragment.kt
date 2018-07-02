package com.lohjason.genericqr.ui.scanner

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.lohjason.genericqr.R
import com.lohjason.genericqr.ui.MainViewModel
import com.lohjason.genericqr.util.FileProviderUtils
import com.lohjason.genericqr.util.Logg
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_google_scanner.*

@Suppress("DEPRECATION")
/**
 * VisionScannerFragment
 * Created by jason on 26/6/18.
 */
class VisionScannerFragment : Fragment() {

    companion object {
        const val RESOLUTION_DEFAULT = 960
        const val FPS_DEFAULT = 24.0f
    }

    private var cameraSource: CameraSource? = null
    private lateinit var cameraSourcePreview: CameraSourcePreview
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var tvToggleFlash: TextView
    private lateinit var tvAbout: TextView
    private lateinit var tvGotoHistory: TextView
    private lateinit var mainViewModel: MainViewModel
    private var lastBarcode: Barcode? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_google_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvToggleFlash = tv_toggle_flash
        tvGotoHistory = tv_scan_history
        tvAbout = tv_about
        tvToggleFlash.setOnClickListener { toggleFlash() }
        tvGotoHistory.setOnClickListener { mainViewModel.setFragmentToShow(MainViewModel.FRAGMENT.HISTORY) }
        tvAbout.setOnClickListener { showAboutDialog() }
        cameraSourcePreview = view.findViewById(R.id.sv_camera)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
        }
    }

    private fun initCamera() {
        barcodeDetector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()

        val constrainedBarcodeDetector = ConstrainedBarcodeDetector(barcodeDetector, 70)

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    val barcode = getLongestBarcode(barcodes)
//                    val barcode = barcodes.valueAt(0)

                    barcode?.let {
                        if (lastBarcode != null && lastBarcode!!.rawValue == barcode.rawValue) {
//                            mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

                            Single.fromCallable { FileProviderUtils.saveBarcode(requireContext(), barcode.rawValue, barcode.format) }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                                   if (!it) {
                                                       Toast.makeText(context,
                                                                      "Error storing image in temp cache. Sharing this image will not work",
                                                                      Toast.LENGTH_SHORT).show()
                                                   }

                                               }, {
                                                   Logg.d("+_", "An error Occurred: ${it.message}", it)
                                                   Toast.makeText(context,
                                                                  "Error storing image in temp cache. Sharing this image will not work",
                                                                  Toast.LENGTH_SHORT).show()
                                               })

                            mainViewModel.setVisionBarcode(barcode)
                            mainViewModel.setFragmentToShow(MainViewModel.FRAGMENT.RESULT)
                        } else {
                            lastBarcode = barcode
                        }
                    }
                }
            }
        })

        cameraSource = CameraSource.Builder(context, constrainedBarcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(FPS_DEFAULT)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
                .setRequestedPreviewSize(RESOLUTION_DEFAULT, RESOLUTION_DEFAULT)
                .build()
        cameraSource?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
    }

    private fun getLongestBarcode(barcodeSparseArray: SparseArray<Barcode>): Barcode? {
        var barcode: Barcode? = null
        var longestLen = 0
        for (i in 0 until barcodeSparseArray.size()) {
            val currentCode = barcodeSparseArray.valueAt(i)
            if (currentCode.rawValue.length >= longestLen) {
                longestLen = currentCode.rawValue.length
                barcode = currentCode
            }
        }
        return barcode
    }

    private fun toggleFlash() {
        cameraSource?.let {
            if (it.flashMode != Camera.Parameters.FLASH_MODE_TORCH) {
                tvToggleFlash.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flash_off_24dp, 0, 0, 0)
                it.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
            } else {
                Logg.d("+_", "Flash was on")
                tvToggleFlash.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flash_on_24dp, 0, 0, 0)
                it.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
            }
        }
    }

    private fun showAboutDialog() {
        val formattedText = Html.fromHtml("<h5>Usage</h5>" +
                                                  "<b>Scanning</b><br>" +
                                                  "- Point the Camera at a barcode and that's it.<br>" +
                                                  "- If nothing happens, try moving the camera closer or further away<br>" +
                                                  "<b>History</b><br>" +
                                                  "- Long press on an entry to copy it to the clipboard" +
                                                  "<br>" +
                                                  "<h5>Privacy</h5>" +
                                                  "- We do not collect, store or share any of your information<br>" +
                                                  "- We do not display any Ads")

        AlertDialog.Builder(requireContext())
                .setTitle("About this App")
                .setMessage(formattedText)
                .setCancelable(true)
                .show()
    }

    override fun onPause() {
        cameraSource?.let {
            cameraSourcePreview.stop()
            cameraSourcePreview.release()
        }
        super.onPause()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        initCamera()
        cameraSourcePreview.start(cameraSource)
        Logg.d("+_", "Started Cam")
    }

}