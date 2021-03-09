package videowebview.ca

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import videowebview.ca.databinding.ActivityFullscreenBinding

class FullscreenActivity : AppCompatActivity() {

    lateinit var binding: ActivityFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Utils.applySettingToWebview(binding.webview)
        binding.webview.webViewClient = WebViewClient()
        binding.webview.webChromeClient = MyChrome(this)

        val uuid = "Lgmr3D3SmXAtCZPSaHiWTM"
        var fileString: String = Utils.readAsset(baseContext, MainActivity.FLYBITS_HTML_BASE_FILE, uuid)

        // replacing this part with loadDataWithBaseURL so we don't need to save file again but doing this we have a
        // problem getting actions from video
        Utils.saveTextFile(fileString, applicationContext)
        binding.webview.loadUrl("file:///data/user/0/videowebview.ca/files/index.html")
        //binding.webview.loadDataWithBaseURL(null, fileString , "text/html", "utf-8", null)

        binding.webview.addJavascriptInterface(WebAppInterface(this), MainActivity.FLYBITS_JAVASCRIPT_INTERFACE)
    }

    override fun onDestroy() {
        binding.webview.removeJavascriptInterface(MainActivity.FLYBITS_JAVASCRIPT_INTERFACE)
        super.onDestroy()
    }
}