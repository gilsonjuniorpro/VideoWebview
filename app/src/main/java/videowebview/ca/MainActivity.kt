package videowebview.ca

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import videowebview.ca.databinding.ActivityMainBinding
import java.io.File
import java.net.URLConnection

class MainActivity : AppCompatActivity() {
    lateinit var it: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        it = Intent(this, MainActivity::class.java)

        binding.webview.webViewClient = MyWebViewClient(this)
        binding.webview.settings.apply {
            useWideViewPort = true
            loadWithOverviewMode = true
            javaScriptEnabled = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccess = true
            allowContentAccess = true
            WebSettings.PluginState.ON
        }

        binding.webview.loadUrl("file:///android_asset/index.html")

        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d("MyApplication", "${message.message()} -- From line ${message.lineNumber()} of ${message.sourceId()}")
                return true
            }
        }

        binding.webview.addJavascriptInterface(WebAppInterface(this), "Android")
    }

    class MyWebViewClient(private val context: Context) : WebViewClientCompat() {
        override fun onPageFinished(view: WebView, url: String) {
            val url = "https://play.vidyard.com/SrQzkLcJqjr1efvssLM9Rr.jpg"
            //val url = "https://gamingtrend.com/wp-content/uploads/2020/07/youtube-thumb.jpg"
            val token = "SrQzkLcJqjr1efvssLM9Rr"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                view.evaluateJavascript("loadMsg('$url', '$token')", null)
            } else {
                view.loadUrl("javascript:loadMsg('$url', '$token')")
            }
        }
    }

    class WebAppInterface(private val mContext: Context) {
        /** Show a toast from the web page  */
        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun isPlaying(isIt: Boolean) {
            if(isIt) {
                Toast.makeText(mContext, "The video is playing", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(mContext, "The video is not playing", Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun performClick(string: String) {
            Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show()
        }
    }
}