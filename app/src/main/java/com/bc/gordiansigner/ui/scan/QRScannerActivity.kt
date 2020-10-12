package com.bc.gordiansigner.ui.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import com.bc.gordiansigner.R
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import com.bc.gordiansigner.ui.DialogController
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_qrscanner.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QRScannerActivity : BaseAppCompatActivity() {

    companion object {
        private const val SOFT_DELAY_TIME = 500L
        private const val QR_CODE_STRING = "qr_code_string"

        fun extractResultData(intent: Intent): String = intent.getStringExtra(QR_CODE_STRING) ?: ""
    }

    @Inject
    internal lateinit var dialogController: DialogController

    override fun layoutRes() = R.layout.activity_qrscanner

    override fun viewModel(): BaseViewModel? = null

    private val compositeDisposable = CompositeDisposable()

    override fun initComponents() {
        super.initComponents()

        title = "Scan QR Code"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rxPermission = RxPermissions(this)
        Observable.timer(SOFT_DELAY_TIME, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { rxPermission.requestEach(Manifest.permission.CAMERA) }
            .subscribe { permission ->
                if (!permission.granted) {
                    if (permission.shouldShowRequestPermissionRationale) {
                        finish()
                    } else {
                        dialogController.alert(
                            R.string.enable_camera_access,
                            R.string.to_get_started_allow_access_camera,
                            R.string.enable_access
                        ) {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", this.packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            } catch (ignore: Throwable) {
                            }
                        }
                    }
                }
            }.let { compositeDisposable.add(it) }

        val formats = listOf(BarcodeFormat.QR_CODE)
        scannerView.decoderFactory = DefaultDecoderFactory(formats)
        scannerView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let {
                    Log.d("QRScannerActivity", it)
                    val intent = Intent().apply { putExtra(QR_CODE_STRING, it) }
                    this@QRScannerActivity.setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Completable.timer(SOFT_DELAY_TIME, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { scannerView.resume() }
            .let { compositeDisposable.add(it) }
    }

    override fun onPause() {
        scannerView.pause()
        super.onPause()
    }

    override fun deinitComponents() {
        compositeDisposable.dispose()
        super.deinitComponents()
    }
}