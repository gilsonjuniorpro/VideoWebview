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
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                testMethod(binding.webview)
            }
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

        binding.webview.evaluateJavascript("pause()",
            ValueCallback<String?> {
                Toast.makeText(
                    this,
                    "pause evaluateJavascript ${it.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.webview.evaluateJavascript("play()",
            ValueCallback<String?> {
                Toast.makeText(this, "play evaluateJavascript ${it.toString()}", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        binding.webview.evaluateJavascript("getGreetings()",
            ValueCallback<String?> { value ->
                Toast.makeText(this, "getGreetings evaluateJavascript ${value}", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun testMethod(webView: WebView) {
        var call = "javascript:sayHello()"
        call = "javascript:alertMessage(\"" + "content" + "\")"
        call = "javascript:toastMessage(\"" + "content" + "\")"
        call = "javascript:sumToJava(1,2)"
        webView.loadUrl(call)
    }
}