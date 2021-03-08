package videowebview.ca

import android.webkit.JavascriptInterface
import android.widget.Toast

class JSBridge(){
    @JavascriptInterface
    fun showMessageInNative(message:String){
        val msg = message
        val name = "ok"
    }
}