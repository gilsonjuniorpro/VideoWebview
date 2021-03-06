package videowebview.ca

import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import videowebview.ca.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webview.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        binding.webview.settings.apply {
            domStorageEnabled = true
            loadWithOverviewMode = true
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            //when disabled the video don't plays
            allowUniversalAccessFromFileURLs = true
            allowFileAccess = true
            allowContentAccess = true

            WebSettings.PluginState.ON
        }

        binding.webview.webChromeClient = MyChrome(this)

        val uuid = "SrQzkLcJqjr1efvssLM9Rr"
        var fileString = Utils.readAsset(baseContext, "index.html", uuid)
        Utils.saveTextFile(fileString, applicationContext)

        binding.webview.loadUrl("file:///data/user/0/videowebview.ca/files/index.html")
        binding.webview.addJavascriptInterface(WebAppInterface(this), "Android")
    }
}