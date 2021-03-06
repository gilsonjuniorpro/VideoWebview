package videowebview.ca

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val mContext: Context) {

    var listLActionLog: MutableList<ActionLog> = mutableListOf()

    @JavascriptInterface
    fun playVideo(time: String) {
        addLog("play", time)
        Toast.makeText(mContext, "it's playing video $time", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun pauseVideo(time: String) {
        addLog("pause", time)
        Toast.makeText(mContext, "it's not playing video $time", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun setFullscreen() {
        Toast.makeText(mContext, "fullscreen mode", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun toastMessage(message: String?) {
        Toast.makeText(
            mContext,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    @JavascriptInterface
    fun onSumResult(result: Int) {
        Toast.makeText(mContext, "onSumResult result=$result", Toast.LENGTH_SHORT).show()
    }

    fun addLog(action: String, time: String){
        listLActionLog.add(
            ActionLog(
                action,
                time
            )
        )
    }

    fun getLog(): MutableList<ActionLog> {
        return listLActionLog
    }
}