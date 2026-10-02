package com.example.batterytg

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*

/**
 * Handles the "Upgrade to Premium" purchase.
 *
 * SETUP REQUIRED BEFORE THIS WORKS:
 * 1. Create a release listing for this app in Google Play Console (Internal Testing track is enough).
 * 2. Under Monetize > Products > In-app products, create a product with the exact ID
 *    "premium_5_devices" below.
 * 3. Add your Google account as a License Tester so you can complete test purchases
 *    for free while developing.
 * Until step 2 is done, queryProductDetails will return nothing and the button will
 * show "Upgrade not available yet".
 */
class BillingManager(private val activity: Activity, private val onResult: (success: Boolean, message: String) -> Unit) {

    companion object {
        const val PRODUCT_ID = "premium_5_devices"
    }

    private var productDetails: ProductDetails? = null

    private val purchasesListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) handlePurchase(purchase)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            onResult(false, "Purchase cancelled.")
        } else {
            onResult(false, "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun startPurchaseFlow() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    onResult(false, "Billing unavailable right now.")
                    return
                }
                val product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(listOf(product))
                    .build()

                billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
                    val firstProduct = productDetailsList?.firstOrNull()

                    if (result.responseCode != BillingClient.BillingResponseCode.OK || firstProduct == null) {
                        onResult(false, "Upgrade not available yet. (Product not configured in Play Console.)")
                        return@queryProductDetailsAsync
                    }

                    productDetails = firstProduct
                    launchFlow(firstProduct)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingManager", "Billing service disconnected")
            }
        })
    }

    private fun launchFlow(details: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        // Acknowledge the purchase with Google, then tell our server to unlock this chat.
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) { }
        }

        val key = Prefs.getReportKey(activity)
        Api.post("/unlock-premium", """{"key":"$key"}""") { result ->
            result.onSuccess { resp ->
                if (resp.code == 200) {
                    Prefs.setPremium(activity, true)
                    onResult(true, "Upgraded! You can now connect up to 5 devices.")
                } else {
                    onResult(false, "Purchased, but couldn't confirm with the server. Try again or contact support.")
                }
            }.onFailure {
                onResult(false, "Purchased, but couldn't reach the server to confirm.")
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
