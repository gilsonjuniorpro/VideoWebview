package videowebview.ca

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.flybits.android.kernel.ContentAnalytics
import com.flybits.android.kernel.ExperienceContentAnalytics
import com.flybits.android.kernel.PushContentAnalytics
import com.flybits.android.kernel.api.FlyContentData
import com.flybits.commons.library.utils.Utilities
import com.flybits.concierge.*
import com.flybits.concierge.activities.ConciergeActivity
import com.flybits.concierge.activities.SimpleCardDetailActivity
import com.flybits.concierge.models.SimpleCard
import com.flybits.concierge.utils.getGlideImageTarget
import com.flybits.concierge.utils.setStreamLineWebViewStyle
import com.flybits.concierge.videoplayer.ActivityBridge
import com.flybits.concierge.videoplayer.FlybitsVideoPlayer
import com.flybits.concierge.videoplayer.exoplayer.ExoPlayerPlayer
import com.flybits.concierge.videoplayer.vidyard.FullscreenActivity
import com.flybits.concierge.videoplayer.vidyard.FullscreenFragment
import com.flybits.concierge.viewactionhandlers.SimpleCardUserActionHandler
import com.flybits.concierge.viewholders.FlybitsViewHolder
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * [FlybitsViewProvider] for text link cards.
 */
class SimpleCardViewProvider @JvmOverloads internal constructor(
        private val contentAnalytics: ContentAnalytics?,
        private val experienceContentAnalytics: ExperienceContentAnalytics?,
        private val pushContentAnalytics: PushContentAnalytics?,
        private val userActionHandler: UserActionHandler<SimpleCard>,
        private val contentLayoutId: Int = -1
) : FlybitsViewProvider<SimpleCard> {

    // Constructors that are mostly defined for Java compatibility.
    /**
     * Instantiates the [SimpleCardViewProvider] with the default [UserActionHandler] and [contentLayoutId].
     *
     * @param context The [Context] (app context is recommended).
     */
    constructor(context: Context) : this(
            ContentAnalytics(context),
            ExperienceContentAnalytics(context),
            PushContentAnalytics(context),
            SimpleCardUserActionHandler()
    )

    /**
     * Instantiates the [SimpleCardViewProvider] with provided [UserActionHandler].
     * When overriding only a subset of the default handling logic, extend and override [SimpleCardViewProvider].
     *
     * @param context The [Context] (app context is recommended).
     *
     * @param userActionHandler The [UserActionHandler] which will respond to user actions in the UI.
     */
    constructor(context: Context, userActionHandler: UserActionHandler<SimpleCard>) : this(
            ContentAnalytics(context),
            ExperienceContentAnalytics(context),
            PushContentAnalytics(context),
            userActionHandler
    )

    /**
     * Instantiates the [SimpleCardViewProvider] with provided [contentLayoutId] and default [UserActionHandler].
     *
     * @param context The [Context] (app context is recommended).
     *
     * @param contentLayoutId The [Int] value that represents content layout ID to be inflated by this [FlybitsViewProvider].
     */
    constructor(context: Context, contentLayoutId: Int) : this(
            ContentAnalytics(context),
            ExperienceContentAnalytics(context),
            PushContentAnalytics(context),
            SimpleCardUserActionHandler(),
            contentLayoutId
    )

    /**
     * Instantiates the [SimpleCardViewProvider] with provided [UserActionHandler] and [contentLayoutId].
     * When overriding only a subset of the default handling logic, extend and override [SimpleCardViewProvider].
     *
     * @param context The [Context] (app context is recommended).
     *
     * @param userActionHandler The [UserActionHandler] which will respond to user actions in the UI.
     *
     * @param contentLayoutId The [Int] value that represents content layout ID to be inflated by this [FlybitsViewProvider].
     */
    constructor(
            context: Context,
            userActionHandler: UserActionHandler<SimpleCard>,
            contentLayoutId: Int
    ) : this(
            ContentAnalytics(context),
            ExperienceContentAnalytics(context),
            PushContentAnalytics(context),
            userActionHandler,
            contentLayoutId
    )

    override fun getClassType(): Class<SimpleCard> {
        return SimpleCard::class.java
    }

    override fun getContentType(): String {
        return CONTENT_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup): FlybitsViewHolder<SimpleCard> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.flybits_streamline_item_link, parent, false)
        return SimpleCardViewHolder(
                view,
                contentAnalytics,
                experienceContentAnalytics,
                pushContentAnalytics,
                userActionHandler,
                contentLayoutId
        )
    }

    companion object {
        const val CONTENT_TYPE = "concierge-card-link"
        const val componentID_link = "link"
        const val componentType = "link"

        const val SIMPLE_CARD_TAG = "SimpleCardTag"

        const val FLYBITS_JAVASCRIPT_INTERFACE = "Android"
        const val FLYBITS_HTML_BASE_FILE = "index.html"

        private fun configureView(
                data: SimpleCard,
                simpleCardView: View,
                userActionHandler: UserActionHandler<SimpleCard>,
                flybitsNavigator: FlybitsNavigator,
                contentAnalytics: ContentAnalytics?,
                experienceContentAnalytics: ExperienceContentAnalytics?,
                pushContentAnalytics: PushContentAnalytics?,
                contentLayoutId: Int
        ) {
            val contentView: ViewStub? =
                simpleCardView.findViewById(R.id.flybits_streamline_link_content)

            val emptyView: View =
                simpleCardView.findViewById(R.id.flybits_streamline_link_emptyTxt)

            contentView?.visibility = View.GONE
            emptyView.visibility = View.GONE

            when (data.title?.value) {
                null -> {
                    emptyView.visibility = View.VISIBLE
                }
                else -> {
                    if (contentLayoutId != -1) {
                        contentView?.layoutResource = contentLayoutId
                    }
                    contentView?.visibility = View.VISIBLE

                    val bannerView: ImageView? =
                        simpleCardView.findViewById(R.id.flybits_streamline_link_imgHeader)
                    val exoPlayerView: PlayerView? =
                        simpleCardView.findViewById(R.id.flybits_streamline_link_exoplayer)
                    val vidyardPlayerView: WebView? =
                            simpleCardView.findViewById(R.id.flybits_streamline_link_vidyard)
                    val headerView: TextView? =
                        simpleCardView.findViewById(R.id.flybits_streamline_link_header)
                    val titleView: TextView? =
                        simpleCardView.findViewById(R.id.flybits_streamline_link_title)
                    val descriptionView: WebView? =
                        simpleCardView.findViewById(R.id.flybits_streamline_link_description)
                    val linkViewProvider: TextView? =
                        simpleCardView.findViewById(R.id.flybits_streamline_link_action)

                    bannerView?.apply {
                        data.image?.value.let {
                            this.visibility = View.GONE
                            if (!it.isNullOrEmpty()) {
                                Glide.with(this.context).load(it)
                                    .apply(RequestOptions().centerCrop())
                                    .into(getGlideImageTarget(this, SIMPLE_CARD_TAG))
                            }
                        }
                    }

                    // ExoPlayer view
                    exoPlayerView?.apply {
                        bannerView?.visibility =
                            View.INVISIBLE // To remove this after ES changes for content object.

                        val flybitsVideoPlayer = FlybitsVideoPlayer.getInstance(context)
                        val exoPlayerPlayer = (flybitsVideoPlayer.player(
                                data.content,
                                "ExoPlayer" // This can be a constant passed by the Content to distinguis between different Video Players.
                        ) as ExoPlayerPlayer)

                        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                                context,
                                Util.getUserAgent(context, "simpleExoPlayer")
                        )

                        val firstSource: MediaSource =
                            ExtractorMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")) // Sample Video

                        this.player = exoPlayerPlayer.getExoPlayer()

                        // To release the player once view is detached.
                        this.addOnAttachStateChangeListener(object :
                                View.OnAttachStateChangeListener {
                            override fun onViewAttachedToWindow(v: View) {}

                            override fun onViewDetachedFromWindow(v: View) {
                                exoPlayerPlayer.pause()
                            }
                        })

                        val scrollBounds = Rect()
                        if (this.getLocalVisibleRect(scrollBounds)) {
                            // If visible in screen
                        } else {
                            exoPlayerPlayer.pause()
                        }

                        exoPlayerPlayer.setMedia(firstSource)
                    }

                    val activity = ActivityBridge.getCurrentActivity()
                    vidyardPlayerView?.apply {
                        applySettingToWebview(vidyardPlayerView)
                        webViewClient = WebViewClient()
                        webChromeClient = FlybitsChrome(activity)
                        //webChromeClient = WebChromeClient()



                        val uuid = "Lgmr3D3SmXAtCZPSaHiWTM"
                        var fileString: String = readAsset(activity, FLYBITS_HTML_BASE_FILE, uuid)

                        // replacing this part with loadDataWithBaseURL so we don't need to save file again but doing this we have a
                        // problem getting actions from video
                        saveTextFile(fileString, activity)
                        loadUrl("file:///data/user/0/com.flybits.android.concierge/files/index.html")
                        //binding.webview.loadDataWithBaseURL(null, fileString , "text/html", "utf-8", null)

                        addJavascriptInterface(WebAppInterface(activity), FLYBITS_JAVASCRIPT_INTERFACE)
                    }

                    headerView?.apply {
                        data.header?.value.let {
                            if (it.isNullOrEmpty()) {
                                this.visibility = View.GONE
                            } else {
                                this.text = it
                            }
                        }
                    }

                    headerView?.apply {
                        data.header?.value.let {
                            if (it.isNullOrEmpty()) {
                                this.visibility = View.GONE
                            } else {
                                this.text = it
                            }
                        }
                    }

                    titleView?.text = data.title?.value
                    descriptionView?.apply {
                        data.description?.value.let {
                            when {
                                it.isNullOrEmpty() -> {
                                    this.visibility = View.GONE
                                }
                                else -> {
                                    val rtf = Utilities.toRTF(it)
                                    this.loadDataWithBaseURL(
                                            null,
                                            setStreamLineWebViewStyle(
                                                    rtf,
                                                    this.context,
                                                    R.style.Concierge_StreamLined_Link_Description
                                            ),
                                            "text/html",
                                            "UTF-8",
                                            null
                                    )
                                    this.setBackgroundColor(Color.TRANSPARENT)
                                    this.isVerticalScrollBarEnabled = false
                                }
                            }
                        }
                    }

                    linkViewProvider?.apply {
                        this.text = data.link?.text?.value.toString()
                        this.setOnClickListener {
                            userActionHandler.onUserAction(
                                    SimpleCardUserActionHandler.LINK_CLICKED,
                                    data,
                                    flybitsNavigator
                            )

                            // analytics expected format
                            // ctx.flybits.contentAnalytics.query.componentEngaged.{Content ID}.{Component ID}.{Component Name}.{Component Type}
                            val componentName = this.text.toString().replace(".", "")
                            val key =
                                data.content.id + "." + componentID_link + "." + componentName + "." + componentType
                            contentAnalytics?.trackComponentEngaged(key)
                            contentAnalytics?.trackEngaged(data.content)

                            // ctx.flybits.experienceContentAnalytics.query.componentEngaged.{Content ID}.{Experience ID}.{Component ID}.{Component Name}.{Component Type}
                            data.content.experienceId?.let {
                                val keyExperienceContentAnalytics =
                                    data.content.id + "." + it + "." + componentID_link + "." + componentName + "." + componentType
                                experienceContentAnalytics?.trackComponentEngaged(
                                        keyExperienceContentAnalytics
                                )
                            } ?: run {
                                // ctx.flybits.pushContentAnalytics.query.componentEngaged.{Content ID}.{Push ID}.{Component ID}.{Component Name}.{Component Type}
                                data.content.pushRequestId?.let {
                                    val keyPushContentAnalytics =
                                        data.content.id + "." + it + "." + componentID_link + "." + componentName + "." + componentType
                                    pushContentAnalytics?.trackComponentEngaged(
                                            keyPushContentAnalytics
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        /**
         * This method will return all files found in the assets folder. Note: will not traverse
         * nested directory. Returned list is not sorted
         * @return List of file names
         */
        private fun getFileFromAssets(activity: Activity): String? {
            var file: String? = null
            val assetManager: AssetManager = activity.assets
            try {
                for (name in assetManager.list("")) {
                    if (name.toLowerCase().endsWith("index.html")) {
                        file = name
                    }
                }
            } catch (ioe: IOException) {
                val msg = "Could not read files from assets folder"
            }
            return file
        }

        fun saveTextFile(file: String, context: Context) {
            try {
                val fileOutputStream: FileOutputStream = context.openFileOutput(
                        "index.html",
                        Context.MODE_PRIVATE
                )
                val outputWriter = OutputStreamWriter(fileOutputStream)
                outputWriter.write(file)
                outputWriter.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun readAsset(context: Context, fileName: String, uuid: String): String {
            return context.assets
                    .open(fileName)
                    .bufferedReader()
                    .use(BufferedReader::readText).replace("<uuid>", uuid)
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

    class ActionLog(
        val vidId: String,
        val action: String,
        val time: String
    )

    class WebAppInterface(private val mContext: Context) {
        var listLActionLog: MutableList<ActionLog> = mutableListOf()

        @JavascriptInterface
        fun play(vidId: String, time: String) {
            addLog(vidId, "play", time)
            Toast.makeText(mContext, "it's playing video $time", Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun pause(vidId: String, time: String) {
            addLog(vidId, "pause", time)
            Toast.makeText(mContext, "it's not playing video $time", Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun setFullscreen(vidId: String, time: String) {
            Toast.makeText(mContext, "fullscreen mode", Toast.LENGTH_SHORT).show()
            val activity = ActivityBridge.getCurrentActivity()
            activity?.run {
                pause(vidId, time)
                val intent = Intent(activity.baseContext, FullscreenActivity::class.java)
                activity.startActivity(intent)
            }
        }

        @JavascriptInterface
        fun setLogAction(action: String, vidId: String, time: String) {
            addLog(vidId, action, time)
            Toast.makeText(mContext, "the action was: $action, at time: $time", Toast.LENGTH_SHORT).show()
        }

        private fun addLog(vidId: String, action: String, time: String) {
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

    class FlybitsChrome internal constructor(private val activity: Activity) : WebChromeClient() {
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
            Log.d(
                    "MyApplication",
                    "onHideCustomView"
            )
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
            Log.d(
                    "MyApplication",
                    "onHideCustomView"
            )
            //val intent = Intent(activity.baseContext, FullscreenActivity::class.java)
            //activity.startActivity(intent)

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

    class SimpleCardViewHolder(
            val view: View,
            val contentAnalytics: ContentAnalytics?,
            val experienceContentAnalytics: ExperienceContentAnalytics?,
            val pushContentAnalytics: PushContentAnalytics?,
            private val userActionHandler: UserActionHandler<SimpleCard>,
            private val contentLayoutId: Int = -1
    ) : FlybitsViewHolder<SimpleCard>(view) {
        override fun bindData(data: SimpleCard, flybitsNavigator: FlybitsNavigator) {
            configureView(
                    data,
                    view,
                    userActionHandler,
                    flybitsNavigator,
                    contentAnalytics,
                    experienceContentAnalytics,
                    pushContentAnalytics,
                    contentLayoutId
            )
        }
    }

    override fun onPushOpened(model: SimpleCard, flybitsNavigator: FlybitsNavigator) {
        model.let {
            val bundle = Bundle()
            bundle.putParcelable(SimpleCardDetailActivity.EXTRA_SIMPLECARD_DETAILS, it)
            bundle.putParcelable(
                    SimpleCardDetailActivity.EXTRA_USER_ACTION_HANDLER,
                    userActionHandler
            )
            flybitsNavigator.openActivity(SimpleCardDetailActivity::class.java, bundle)
        }
    }
}