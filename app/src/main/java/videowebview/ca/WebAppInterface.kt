package videowebview.ca

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val mContext: Context) {
    @JavascriptInterface
    fun playVideo() {
        Toast.makeText(mContext, "it's playing video", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun pauseVideo() {
        Toast.makeText(mContext, "it's not playing video", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun setFullscreen() {
        Toast.makeText(mContext, "fullscreen mode", Toast.LENGTH_SHORT).show()
    }
}