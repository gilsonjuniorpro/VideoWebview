package videowebview.ca

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.widget.FrameLayout

class MyChrome internal constructor(private val activity: Activity) :
    WebChromeClient() {
    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null
    private var mOriginalSystemUiVisibility = 0

    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
        Log.d(
            "MyApplication",
            "${message.message()} -- From line ${message.lineNumber()} of ${message.sourceId()}"
        )
        return true
    }

    override fun onHideCustomView() {
        (activity.window.decorView as FrameLayout).removeView(mCustomView)
        mCustomView = null
        activity.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mCustomViewCallback!!.onCustomViewHidden()
        mCustomViewCallback = null
    }

    override fun onShowCustomView(
        parentView: View,
        paramCustomViewCallback: CustomViewCallback
    ) {
        if (mCustomView != null) {
            onHideCustomView()
            return
        }
        mCustomView = parentView
        mOriginalSystemUiVisibility =
            activity.window.decorView.systemUiVisibility
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mCustomViewCallback = paramCustomViewCallback
        (activity.window.decorView as FrameLayout).addView(
            mCustomView,
            FrameLayout.LayoutParams(-1, -1)
        )
        activity.window.decorView.systemUiVisibility = 3846
    }
}