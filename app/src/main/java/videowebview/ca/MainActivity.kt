package videowebview.ca

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import videowebview.ca.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var it: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        it = Intent(this, MainActivity::class.java)

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

        binding.button.setOnClickListener{
            loadPage(it)
        }

        binding.webview.addJavascriptInterface(WebAppInterface(this), "Android")
    }

    class WebAppInterface internal constructor(c: Context) {
        var mContext: Context

        /** Show a toast from the web page  */
        @JavascriptInterface
        fun startNewActivity() {
            println("test")
        }

        /** Instantiate the interface and set the context  */
        init {
            mContext = c
        }
    }

    @SuppressLint("JavascriptInterface")
    fun loadPage(view: View) {
        val browser = WebView(this)
        browser.settings.javaScriptEnabled = true
        browser.loadUrl("file:///android_asset/page.html")
        setContentView(browser)
        val ws = browser.settings
        ws.javaScriptEnabled = true
        browser.addJavascriptInterface(object : Any() {
            @JavascriptInterface // For API 17+
            fun performClick(string: String) {
                Toast.makeText(this@MainActivity, string, Toast.LENGTH_SHORT).show()
            }
        }, "ok")
    }
}