package videowebview.ca

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import videowebview.ca.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webview.webViewClient = WebViewClient()

        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val url = "https://play.vidyard.com/SrQzkLcJqjr1efvssLM9Rr.jpg"
                val token = "SrQzkLcJqjr1efvssLM9Rr"

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    view.evaluateJavascript("loadMsg('$url', '$token')", null)
                } else {
                    view.loadUrl("javascript:loadMsg('$url', '$token')")
                }
            }
        }

        binding.webview.loadUrl("file:///android_asset/index.html")

        binding.webview.webChromeClient = WebChromeClient()

        val webSettings = binding.webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.pluginState = WebSettings.PluginState.ON
    }
}