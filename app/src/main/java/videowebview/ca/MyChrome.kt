package videowebview.ca

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.widget.FrameLayout

class MyChrome internal constructor(private val activity: Activity) : WebChromeClient() {
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

    /**
     * Go to fullscreen mode and keep the state of the video
     */
    override fun onShowCustomView(
        parentView: View,
        paramCustomViewCallback: CustomViewCallback
    ) {
        if (mCustomView != null) {
            onHideCustomView()
            return
        }
        mCustomView = parentView
        mOriginalSystemUiVisibility = activity.window.decorView.systemUiVisibility
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mCustomViewCallback = paramCustomViewCallback
        (activity.window.decorView as FrameLayout).addView(
            mCustomView,
            FrameLayout.LayoutParams(-1, -1)
        )

        activity.window.decorView.systemUiVisibility = (
                // Do not let system steal touches for showing the navigation bar
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        // Keep the app content behind the bars even if user swipes them up
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    /**
     * Back from fullscreen mode to normal visualization and keep the state of the video
     */
    override fun onHideCustomView() {
        super.onHideCustomView()
        (activity.window.decorView as FrameLayout).removeView(mCustomView)
        mCustomView = null
        activity.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mCustomViewCallback!!.onCustomViewHidden()
        mCustomViewCallback = null
    }
}