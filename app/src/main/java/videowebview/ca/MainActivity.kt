package videowebview.ca

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewClientCompat
import videowebview.ca.databinding.ActivityMainBinding
import java.io.*


class MainActivity : AppCompatActivity() {

    lateinit var it: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        it = Intent(this, MainActivity::class.java)

        binding.webview.webViewClient = MyWebViewClient(this)
        binding.webview.settings.apply {
            //useWideViewPort = true
            loadWithOverviewMode = true
            javaScriptEnabled = true
            //allowFileAccessFromFileURLs = true

            //when disabled the video don't plays
            allowUniversalAccessFromFileURLs = true
            allowFileAccess = true
            allowContentAccess = true

            WebSettings.PluginState.ON
        }



        val uuid = "SrQzkLcJqjr1efvssLM9Rr"

        var fileString = readAsset(baseContext, "index.html", uuid)
        saveTextFile(fileString)

        //"file:///android_asset/page.html"
        //var uri = content://media/external/images/media/174518
        //file:///data/user/0/com.raptorswatermark.ca/cache/cropped3845631742083647187.jpg

        binding.webview.loadUrl("file:///data/user/0/videowebview.ca/files/index.html")
        //binding.webview.loadUrl("file:///android_asset/index.html")


        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d("MyApplication", "${message.message()} -- From line ${message.lineNumber()} of ${message.sourceId()}")
                return true
            }
        }

        binding.webview.addJavascriptInterface(WebAppInterface(this), "Android")
    }

    private fun saveTextFile(file: String) {
        try {
            val fileOutputStream: FileOutputStream = openFileOutput("index.html", Context.MODE_PRIVATE)
            val outputWriter = OutputStreamWriter(fileOutputStream)
            outputWriter.write(file)
            outputWriter.close()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readAsset(context: Context, fileName: String, uuid: String): String =
            context.assets
                    .open(fileName)
                    .bufferedReader()
                    .use(BufferedReader::readText).replace("<uuid>", uuid)

    class MyWebViewClient(private val context: Context) : WebViewClientCompat() {
        override fun onPageFinished(view: WebView, url: String) {
            /*val url = "https://play.vidyard.com/SrQzkLcJqjr1efvssLM9Rr.jpg"
            val token = "SrQzkLcJqjr1efvssLM9Rr"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                view.evaluateJavascript("loadMsg('$url', '$token')", null)
            } else {
                view.loadUrl("javascript:loadMsg('$url', '$token')")
            }*/
        }
    }

    class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun playVideo() {
            Toast.makeText(mContext, "it's playing video", Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun pauseVideo() {
            Toast.makeText(mContext, "it's not playing video", Toast.LENGTH_SHORT).show()
        }
    }
}