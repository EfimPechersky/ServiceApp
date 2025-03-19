package com.example.serviceapp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

class RateCheckService: Service() {
    val handler = Handler(Looper.getMainLooper())
    var rateCheckAttempt = 0
    private var attempts =0
    private var is_changed=false
    lateinit var startRate: BigDecimal
    lateinit var targetRate: BigDecimal
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    val rateCheckInteractor = RateCheckInteractor()
    private fun sendRateUpdate(message:String) {
        val intent = Intent("RATE_UPDATE").apply {
            putExtra("rate_message", message)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    fun startFetchingRate() {
        attempts=0;
        coroutineScope.launch {
            startRate=rateCheckInteractor.requestRate().toBigDecimal()
            while (attempts<RATE_CHECK_ATTEMPTS_MAX) {
                var rate=rateCheckInteractor.requestRate()
                Log.d(TAG, rate)
                if (startRate>targetRate) {
                    if (targetRate > rate.toBigDecimal()) {
                        Log.d(TAG, "Changed")
                        sendRateUpdate("Rate decreased")
                        onDestroy()
                    }
                }else{
                    if (targetRate < rate.toBigDecimal()) {
                        Log.d(TAG, "Changed")
                        sendRateUpdate("Rate increased")
                        onDestroy()
                    }
                }

                delay(RATE_CHECK_INTERVAL)
            }
        }
    }
    val rateCheckRunnable: Runnable = Runnable {
        Log.d(TAG, "Subscribed")
        startFetchingRate()
    }

    private fun requestAndCheckRate() {

    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRate = BigDecimal(intent?.getStringExtra(ARG_START_RATE))
        targetRate = BigDecimal(intent?.getStringExtra(ARG_TARGET_RATE))

        Log.d(TAG, "onStartCommand startRate = $startRate targetRate = $targetRate")

        handler.post(rateCheckRunnable)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rateCheckRunnable)
        coroutineScope.cancel()
    }


    companion object {
        const val TAG = "RateCheckService"
        const val RATE_CHECK_INTERVAL = 5000L
        const val RATE_CHECK_ATTEMPTS_MAX = 100

        const val ARG_START_RATE = "ARG_START_RATE"
        const val ARG_TARGET_RATE = "ARG_TARGET_RATE"

        fun startService(context: Context, startRate: String, targetRate: String) {
            context.startService(Intent(context, RateCheckService::class.java).apply {
                putExtra(ARG_START_RATE, startRate)
                putExtra(ARG_TARGET_RATE, targetRate)
            })
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, RateCheckService::class.java))
        }
    }
}