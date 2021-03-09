package videowebview.ca

import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import videowebview.ca.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object{
        const val FLYBITS_JAVASCRIPT_INTERFACE = "Android"
        const val FLYBITS_HTML_BASE_FILE = "index.html"
    }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applySettingToWebview(binding.webview)
        binding.webview.webViewClient = WebViewClient()
        binding.webview.webChromeClient = MyChrome(this)

        //SrQzkLcJqjr1efvssLM9Rr  Lgmr3D3SmXAtCZPSaHiWTM
        val uuid = "Lgmr3D3SmXAtCZPSaHiWTM"
        var fileString: String = Utils.readAsset(baseContext, FLYBITS_HTML_BASE_FILE, uuid)

        // replacing this part with loadDataWithBaseURL so we don't need to save file again but doing this we have a
        // problem getting actions from video
        Utils.saveTextFile(fileString, applicationContext)
        binding.webview.loadUrl("file:///data/user/0/videowebview.ca/files/index.html")
        //binding.webview.loadDataWithBaseURL(null, fileString , "text/html", "utf-8", null)

        binding.webview.addJavascriptInterface(WebAppInterface(this, binding.webview), FLYBITS_JAVASCRIPT_INTERFACE)
    }

    override fun onDestroy() {
        binding.webview.removeJavascriptInterface(FLYBITS_JAVASCRIPT_INTERFACE)
        super.onDestroy()
    }

    fun applySettingToWebview(webview: WebView){
        webview.settings.apply {
            loadWithOverviewMode = true
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            //when disabled the video don't plays
            allowUniversalAccessFromFileURLs = true
            allowFileAccess = true
        }
    }

}