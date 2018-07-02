package com.lohjason.genericqr.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.lohjason.genericqr.R
import com.lohjason.genericqr.ui.history.HistoryFragment
import com.lohjason.genericqr.ui.results.ResultsFragment
import com.lohjason.genericqr.ui.scanner.VisionScannerFragment
import com.lohjason.genericqr.util.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var btnScan: TextView
    private lateinit var tvScanMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        tvScanMessage = tv_request_scan_message
        btnScan = tv_request_scan

        btnScan.setOnClickListener {
            startScanner()
        }
    }

    private fun setupObservers() {
        val fragmentChangeObserver = Observer<MainViewModel.FRAGMENT> {
            when (it) {
                MainViewModel.FRAGMENT.SCAN -> {
                    startScanner()
                }
                MainViewModel.FRAGMENT.RESULT -> {
                    swapToResults()
                }
                MainViewModel.FRAGMENT.HISTORY -> {
                    swapToHistory()
                }
                null -> {
                }
            }
        }
        mainViewModel.getFragmentToShowLive().observe(this@MainActivity, fragmentChangeObserver)
    }

    private fun startScanner() {
        if (PermissionUtils.hasCameraPermission(this@MainActivity)) {
            swapToScanner()
            tvScanMessage.visibility = View.GONE
            btnScan.visibility = View.GONE
        } else {
            PermissionUtils.requestCameraPermission(this@MainActivity)
            tvScanMessage.visibility = View.VISIBLE
            btnScan.visibility = View.VISIBLE
        }
    }

    private fun swapToResults() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val tag = MainViewModel.FRAGMENT.RESULT.value
        var resultsFragment = fragmentManager.findFragmentByTag(tag)
        if (resultsFragment == null) {
            resultsFragment = ResultsFragment()
        }
        fragmentTransaction.replace(R.id.container_main, resultsFragment, tag).commit()
    }

    private fun swapToScanner() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val tag = MainViewModel.FRAGMENT.SCAN.value
        var scanFragment = fragmentManager.findFragmentByTag(tag)
        if (scanFragment == null) {
            scanFragment = VisionScannerFragment()
        }
        fragmentTransaction.replace(R.id.container_main, scanFragment, tag).commit()
    }

    private fun swapToHistory() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val tag = MainViewModel.FRAGMENT.HISTORY.value
        var historyFragment = fragmentManager.findFragmentByTag(tag)
        if (historyFragment == null) {
            historyFragment = HistoryFragment()
        }
        fragmentTransaction.replace(R.id.container_main, historyFragment, tag).commit()
    }

    private fun showPermissionReasonDialog() {
        AlertDialog.Builder(this@MainActivity)
                .setTitle(getString(R.string.camera_permission_title))
                .setMessage(getString(R.string.camera_permission_rationale))
                .setCancelable(true)
                .show()
    }

    private fun showOpenSettingsDialog() {
        AlertDialog.Builder(this@MainActivity)
                .setTitle(getString(R.string.camera_permission_title))
                .setMessage("${getString(R.string.camera_permission_rationale)}\nOpen Settings to grant this permission?")
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    PermissionUtils.openSettingsPage(this@MainActivity)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.not_now)) { dialog, _ ->
                    Toast.makeText(this@MainActivity,
                                   getString(R.string.settings_page_rationale),
                                   Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PermissionUtils.REQUEST_CODE_CAMERA) {
            if (!grantResults.isEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanner()
                } else {
                    if (PermissionUtils.canRequestCameraPermission(this@MainActivity)) {
                        showPermissionReasonDialog()
                    } else {
                        showOpenSettingsDialog()
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun showAboutDialog() {
        AlertDialog.Builder(this@MainActivity)
                .setTitle("About this App")
                .setMessage("Usage:\n" +
                                    "- Scanning: Point the camera at a barcode and wait for the scan. If nothing happens, try moving the camera closer or further\n" +
                                    "- History: Long press on an entry to copy the text to your clipboard\n" +
                                    "\n" +
                                    "Privacy:\n" +
                                    "We do not collect or store any of your information whatsoever")
                .setCancelable(true)
                .show()
    }

    override fun onBackPressed() {
        when (mainViewModel.getFragmentToShowLive().value) {
            MainViewModel.FRAGMENT.SCAN -> {
                finish()
            }
            MainViewModel.FRAGMENT.RESULT -> {
                mainViewModel.clearVisionBarcode()
                mainViewModel.setFragmentToShow(MainViewModel.FRAGMENT.SCAN)
            }
            MainViewModel.FRAGMENT.HISTORY -> {
                mainViewModel.setFragmentToShow(MainViewModel.FRAGMENT.SCAN)
            }
            null -> {
                super.onBackPressed()
            }
        }
    }
}
