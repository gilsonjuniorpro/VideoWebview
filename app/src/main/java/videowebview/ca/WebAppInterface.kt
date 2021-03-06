package videowebview.ca

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val mContext: Context) {
    var listLActionLog: MutableList<ActionLog> = mutableListOf()

    @JavascriptInterface
    fun playVideo(vidId: String, time: String) {
        addLog(vidId, "play", time)
        Toast.makeText(mContext, "it's playing video $time", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun pauseVideo(vidId: String, time: String) {
        addLog(vidId, "pause", time)
        Toast.makeText(mContext, "it's not playing video $time", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun setFullscreen() {
        Toast.makeText(mContext, "fullscreen mode", Toast.LENGTH_SHORT).show()
    }

    fun addLog(vidId: String, action: String, time: String){
        listLActionLog.add(
            ActionLog(
                vidId,
                action,
                time
            )
        )
    }

    fun getLog(): MutableList<ActionLog> {
        return listLActionLog
    }
}