package com.live.ishqexpress.data

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.live.ishqexpress.ui.common.CoinPack
import com.live.ishqexpress.ui.common.DemoWallet
import com.live.ishqexpress.ui.common.sampleCoinPacks

/**
 * Coins are digital goods, so Google Play policy requires they are sold through Google Play Billing.
 * Each [CoinPack] maps to a one-time *consumable* in-app product in Play Console: `coins_<base coins>`
 * (coins_70, coins_350, coins_700, coins_1400, coins_3500, coins_7000).
 *
 * TODO(backend): send [Purchase.getPurchaseToken] to the server, verify it with the Play Developer API,
 * credit the wallet there, and only then consume. Until the backend exists the client credits locally.
 */
object PlayBilling : PurchasesUpdatedListener {

    sealed interface Result {
        data class Granted(val productId: String) : Result
        data object Pending : Result
        data object Cancelled : Result
        data class Failed(val message: String) : Result
    }

    private var client: BillingClient? = null
    private var pendingCallback: ((Result) -> Unit)? = null

    /** productId → details loaded from Play. Empty until Play answers (or if products aren't set up). */
    val products = mutableStateMapOf<String, ProductDetails>()

    fun productId(p: CoinPack) = "coins_${p.coins}"

    /** Localised price from Play, falling back to the catalogue price while loading. */
    fun price(p: CoinPack): String = products[productId(p)]?.oneTimePurchaseOfferDetails?.formattedPrice ?: p.price

    fun isAvailable(p: CoinPack) = products.containsKey(productId(p))

    fun init(context: Context) {
        if (client != null) return
        val c = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
        client = c
        c.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) return
                loadProducts()
                recoverPurchases()
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun loadProducts() {
        val c = client ?: return
        val list = sampleCoinPacks.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(productId(it)).setProductType(BillingClient.ProductType.INAPP).build()
        }
        c.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(list).build()) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                details.productDetailsList.forEach { products[it.productId] = it }
            }
        }
    }

    /** Purchases that completed while the app was closed (or were pending) are credited on next start. */
    private fun recoverPurchases() {
        val c = client ?: return
        c.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases.forEach { handle(it, null) }
        }
    }

    fun buy(activity: Activity, pack: CoinPack, onResult: (Result) -> Unit) {
        val c = client
        val details = products[productId(pack)]
        if (c == null || details == null) { onResult(Result.Failed("This coin pack isn't available right now. Please try again later.")); return }
        pendingCallback = onResult
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()))
            .build()
        val r = c.launchBillingFlow(activity, params)
        if (r.responseCode != BillingClient.BillingResponseCode.OK) {
            pendingCallback = null
            onResult(Result.Failed("Google Play couldn't start the purchase (${r.responseCode})."))
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val cb = pendingCallback
        pendingCallback = null
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handle(it, cb) }
            BillingClient.BillingResponseCode.USER_CANCELED -> cb?.invoke(Result.Cancelled)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> { recoverPurchases(); cb?.invoke(Result.Failed("Finishing a previous purchase — your coins will appear shortly.")) }
            else -> cb?.invoke(Result.Failed("Purchase failed (${result.responseCode}). You were not charged."))
        }
    }

    private fun handle(p: Purchase, cb: ((Result) -> Unit)?) {
        when (p.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                val c = client ?: return
                c.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) { r, _ ->
                    if (r.responseCode != BillingClient.BillingResponseCode.OK) return@consumeAsync
                    val id = p.products.firstOrNull() ?: return@consumeAsync
                    if (cb != null) cb(Result.Granted(id))
                    else sampleCoinPacks.firstOrNull { productId(it) == id }?.let { DemoWallet.coins += (it.coins + it.bonus) * p.quantity }
                }
            }
            Purchase.PurchaseState.PENDING -> cb?.invoke(Result.Pending)
            else -> {}
        }
    }
}
