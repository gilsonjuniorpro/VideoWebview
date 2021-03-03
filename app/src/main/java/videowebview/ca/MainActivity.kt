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
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    view.evaluateJavascript("loadMsg('How are you today!')", null)
                } else {
                    view.loadUrl("javascript:loadMsg('How are you today!')")
                }*/

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    view.evaluateJavascript("loadMsg('SrQzkLcJqjr1efvssLM9Rr')", null)
                } else {
                    view.loadUrl("javascript:loadMsg('SrQzkLcJqjr1efvssLM9Rr')")
                }
            }
        }

        binding.webview.loadUrl("file:///android_asset/index.html")

        binding.webview.webChromeClient = WebChromeClient()

        val webSettings = binding.webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.pluginState = WebSettings.PluginState.ON


        /*
        //Java
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("<your html file>"); //not in scope of this gist
        webview.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                //Here you want to use .loadUrl again
                //on the webview object and pass in
                //"javascript:<your javaScript function"
                webview.loadUrl("javascript:myJavaScriptFunc('" + argumentPassingIn + "')"); //if passing in an object. Mapping may need to take place
            }
        });

        //HTML
        <!DOCTYPE html>
        <html>
        <head><title>Demo</title></head>
        <body>
          <h1>Body</h1>
          <script>
            myJavaScriptFunc(val){
              //Do something with val
            }
          </script>
        </body>
        </html>
        */
    }
}