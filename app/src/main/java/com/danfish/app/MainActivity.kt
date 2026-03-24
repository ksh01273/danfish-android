package com.danfish.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var offlineView: View

    private val APP_URL = "https://fish.danmap.co.kr"
    private val LOCATION_PERMISSION_REQUEST = 1001

    private var isNetworkAvailable = true
    private var pendingGeolocationOrigin: String? = null
    private var pendingGeolocationCallback: GeolocationPermissions.Callback? = null
    // ── 네트워크 상태 콜백 ──
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread {
                isNetworkAvailable = true
                if (offlineView.visibility == View.VISIBLE) {
                    offlineView.visibility = View.GONE
                    webView.visibility = View.VISIBLE
                    webView.reload()
                }
            }
        }
        override fun onLost(network: Network) {
            runOnUiThread { isNetworkAvailable = false }
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 스플래시 스크린 (Android 12+ SplashScreen API)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progress_bar)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        offlineView = findViewById(R.id.offline_view)

        setupWebView()
        setupSwipeRefresh()
        registerNetworkCallback()

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(APP_URL)
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setGeolocationEnabled(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "$userAgentString DanFishApp/1.0"
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return when {
                    url.startsWith("http://") || url.startsWith("https://") -> false
                    else -> {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "앱을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    showOfflineView()
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?, callback: GeolocationPermissions.Callback?
            ) {
                if (hasLocationPermission()) {
                    callback?.invoke(origin, true, false)
                } else {
                    pendingGeolocationOrigin = origin
                    pendingGeolocationCallback = callback
                    requestLocationPermission()
                }
            }
        }
    }
    // ── SwipeRefresh 설정 ──
    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent)
        swipeRefresh.setOnRefreshListener {
            if (isNetworkAvailable) {
                webView.reload()
            } else {
                swipeRefresh.isRefreshing = false
                showOfflineView()
            }
        }
    }

    // ── 오프라인 뷰 표시 ──
    private fun showOfflineView() {
        webView.visibility = View.GONE
        offlineView.visibility = View.VISIBLE
    }
    // ── 위치 권한 처리 ──
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
            pendingGeolocationCallback?.invoke(pendingGeolocationOrigin, granted, false)
            pendingGeolocationCallback = null
            pendingGeolocationOrigin = null
        }
    }
    // ── 네트워크 감지 등록 ──
    private fun registerNetworkCallback() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, networkCallback)

        // 초기 상태 체크
        val activeNetwork = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(activeNetwork)
        isNetworkAvailable = capabilities?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) == true
    }

    // ── 뒤로가기 ──
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }
}