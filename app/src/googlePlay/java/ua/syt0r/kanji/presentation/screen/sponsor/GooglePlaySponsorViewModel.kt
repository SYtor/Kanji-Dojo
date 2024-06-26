package ua.syt0r.kanji.presentation.screen.sponsor

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.launchUnit
import ua.syt0r.kanji.presentation.screen.sponsor.GooglePlaySponsorScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.sponsor.use_case.GooglePlaySendSponsorResultsUseCase

class GooglePlaySponsorViewModel(
    private val viewModelScope: CoroutineScope,
    private val purchaseManager: GooglePlayPurchaseManager,
    private val sendSponsorResultsUseCase: GooglePlaySendSponsorResultsUseCase,
    private val analyticsManager: AnalyticsManager
) : GooglePlaySponsorScreenContract.ViewModel {

    private val email = mutableStateOf("")
    private val message = mutableStateOf("")

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Init)
    override val state: StateFlow<ScreenState> = _state

    private var purchasesJson: List<String>? = null

    override fun loadInputState() {
        _state.value = ScreenState.Loading

        viewModelScope.launch {
            val result = purchaseManager.initialize()
            if (result.isSuccess) {
                _state.value = ScreenState.Input(
                    email = email,
                    message = message,
                    buttonEnabled = mutableStateOf(true),
                    formattedPrice = result.getOrNull()!!.formattedPrice
                )
            } else {
                val errorMessage = result.exceptionOrNull()!!.message
                _state.value = ScreenState.Error(
                    message = errorMessage
                )
                analyticsManager.sendEvent("billing_init_error") {
                    put("error", errorMessage ?: UNKNOWN_ERROR_MESSAGE)
                }
            }
        }

    }

    override fun startPurchase(activity: Activity) = viewModelScope.launchUnit {
        val inputState = _state.value as? ScreenState.Input ?: return@launchUnit
        inputState.buttonEnabled.value = false
        purchaseManager.start(activity)
            .catch {
                _state.value = ScreenState.Error(it.message)
                analyticsManager.sendEvent("purchase_error") {
                    put("error", it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
            }
            .collect {
                when (it) {
                    PurchaseStatus.Canceled -> {
                        inputState.buttonEnabled.value = true
                        analyticsManager.sendEvent("purchase_canceled")
                    }

                    PurchaseStatus.ConsumingResults -> {
                        _state.value = ScreenState.Loading
                    }

                    is PurchaseStatus.Completed -> {
                        purchasesJson = it.purchasesJson
                        sendResults(it.purchasesJson)
                    }
                }
            }
    }


    override fun retry(activity: Activity) {
        val purchasesJson = purchasesJson
        if (purchasesJson != null) {
            viewModelScope.launch { sendResults(purchasesJson) }
        } else {
            loadInputState()
        }
        analyticsManager.sendEvent("sponsor_retry")
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("sponsor")
    }

    private suspend fun sendResults(purchasesJson: List<String>) {
        _state.value = ScreenState.Loading
        val result = sendSponsorResultsUseCase(
            email = email.value,
            message = message.value,
            purchasesJson = purchasesJson
        )
        if (result.isSuccess) {
            _state.value = ScreenState.Completed
            analyticsManager.sendEvent("sponsor_purchase_complete")
        } else {
            val errorMessage = result.exceptionOrNull()!!.message
            _state.value = ScreenState.Error(errorMessage)
            analyticsManager.sendEvent("sponsor_notify_result_error") {
                put("error", errorMessage ?: UNKNOWN_ERROR_MESSAGE)
            }
        }
    }

    companion object {
        private const val UNKNOWN_ERROR_MESSAGE = "unknown_error"
    }

}