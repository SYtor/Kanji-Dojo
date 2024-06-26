package ua.syt0r.kanji.presentation.screen.sponsor

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface GooglePlayPurchaseManager {

    suspend fun initialize(): Result<PurchaseInitializationResult>

    @Throws
    suspend fun start(activity: Activity): Flow<PurchaseStatus>

}

data class PurchaseInitializationResult(
    val formattedPrice: String
)

sealed interface PurchaseStatus {
    data object Canceled : PurchaseStatus
    data object ConsumingResults : PurchaseStatus
    data class Completed(
        val purchasesJson: List<String>
    ) : PurchaseStatus
}

class DefaultGooglePlayPurchaseManager(
    context: Context,
    private val coroutineScope: CoroutineScope
) : GooglePlayPurchaseManager, BillingClientStateListener, PurchasesUpdatedListener {

    private val connectionStateUpdates = MutableSharedFlow<BillingResult>()
    private val purchaseUpdates = MutableSharedFlow<Pair<BillingResult, List<Purchase>?>>()

    private val billingClient: BillingClient = createBillingClient(context)
    private var productDetails: ProductDetails? = null

    override suspend fun initialize(): Result<PurchaseInitializationResult> {
        when (billingClient.connectionState) {
            ConnectionState.DISCONNECTED -> {
                val billingResult = connectionStateUpdates
                    .onStart { billingClient.startConnection(this@DefaultGooglePlayPurchaseManager) }
                    .first()
                when (billingResult.responseCode) {
                    BillingResponseCode.OK -> {}
                    else -> return Result.failure(billingResult.asError())
                }
            }

            ConnectionState.CLOSED -> {
                return Result.failure(Throwable("Can't connect to Google Play"))
            }

            ConnectionState.CONNECTING,
            ConnectionState.CONNECTED -> {
            }
        }

        val productDetailsResult = getProductDetails()

        return when (productDetailsResult.isSuccess) {
            true -> {
                val initializationResult = PurchaseInitializationResult(
                    formattedPrice = productDetails!!.oneTimePurchaseOfferDetails!!.formattedPrice
                )
                Result.success(initializationResult)
            }

            false -> Result.failure(productDetailsResult.exceptionOrNull()!!)
        }
    }

    override suspend fun start(activity: Activity): Flow<PurchaseStatus> = flow {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails!!)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val flowStartResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        when (flowStartResult.responseCode) {
            BillingResponseCode.OK -> {}
            else -> throw flowStartResult.asError()
        }

        val (billingStatus, purchases) = purchaseUpdates.first()

        when (billingStatus.responseCode) {
            BillingResponseCode.OK -> {}
            BillingResponseCode.USER_CANCELED -> {
                emit(PurchaseStatus.Canceled)
                return@flow
            }

            else -> throw billingStatus.asError()
        }

        purchases?.forEach {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(it.purchaseToken)
                .build()
            val consumeResult = billingClient.consumePurchase(consumeParams)
            if (consumeResult.billingResult.responseCode != BillingResponseCode.OK)
                throw consumeResult.billingResult.asError()
        }

        val purchasesData = purchases?.map { it.originalJson } ?: emptyList()

        emit(PurchaseStatus.Completed(purchasesData))
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        coroutineScope.launch { connectionStateUpdates.emit(billingResult) }
    }

    override fun onBillingServiceDisconnected() {

    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        coroutineScope.launch { purchaseUpdates.emit(billingResult to purchases) }
    }

    private fun createBillingClient(context: Context): BillingClient {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()
        return BillingClient.newBuilder(context)
            .enablePendingPurchases(pendingPurchasesParams)
            .setListener(this)
            .build()
    }

    private suspend fun getProductDetails(): Result<ProductDetails> {
        val currentProductDetails = productDetails
        if (currentProductDetails != null) {
            return Result.success(currentProductDetails)
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        return when (productDetailsResult.billingResult.responseCode) {
            BillingResponseCode.OK -> {
                productDetailsResult.productDetailsList
                    ?.firstOrNull()
                    ?.also { productDetails = it }
                    ?.let { Result.success(it) }
                    ?: Result.failure(Throwable("No product details found"))
            }

            else -> {
                Result.failure(productDetailsResult.billingResult.asError())
            }
        }
    }

    companion object {
        private const val PRODUCT_ID = "token_of_contribution"
    }

    private fun BillingResult.asError(): Throwable {
        val message = debugMessage.takeIf { it.isNotEmpty() }
            ?: "Error code $responseCode"
        return Throwable(message)
    }

}