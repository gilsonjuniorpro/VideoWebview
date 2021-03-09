package videowebview.ca

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class Utils {

    companion object {
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
}