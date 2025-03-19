package com.example.serviceapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    val usdRate = MutableLiveData<String>()
    val rateCheckInteractor = RateCheckInteractor()

    fun onCreate() {
        refreshRate()
    }

    fun onRefreshClicked() {
        refreshRate()
    }

    private fun refreshRate() {
        GlobalScope.launch(Dispatchers.Main) {
            val rate = rateCheckInteractor.requestRate()
            Log.d(TAG, "usdRate = $rate")
            usdRate.value = rate
        }
    }

    companion object {
        const val TAG = "MainViewModel"
        const val USD_RATE_URL = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=RUB&api_key=4c7dc0db2293645087a3a50aee2adbb210dabc1262de063cdd28c8eb0a8e8a73"
    }
}