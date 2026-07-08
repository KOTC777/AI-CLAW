package com.toolbox.core.websnapshot

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

/**
 * Engine for capturing web pages as single self-contained HTML files.
 * Images and CSS are inlined as Base64.
 */
class WebSnapshotEngine(private val context: Context) {

    /**
     * Capture a web page as a single HTML file.
     * @param url The URL to capture.
     * @param outputDir The directory to save the file.
     * @return The path to the saved HTML file.
     */
    suspend fun capture(url: String, outputDir: File): String {
        val snapshotId = System.currentTimeMillis().toString()
        val outputFile = File(outputDir, "$snapshotId.html")

        val html = loadAndCapture(url)

        FileOutputStream(outputFile).use { fos ->
            fos.write(html.toByteArray(Charsets.UTF_8))
        }

        return outputFile.absolutePath
    }

    /**
     * Load a URL in a headless WebView and capture the HTML.
     */
    private suspend fun loadAndCapture(url: String): String = suspendCancellableCoroutine { cont ->
        val webView = WebView(context).apply {
            settings.javaScriptEnabled = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                view?.evaluateJavascript(GET_HTML_JS) { html ->
                    val cleaned = cleanHtml(html)
                    webView.destroy()
                    if (cont.isActive) {
                        cont.resume(cleaned)
                    }
                }
            }
        }

        webView.loadUrl(url)

        cont.invokeOnCancellation {
            webView.destroy()
        }
    }

    /**
     * Clean up the captured HTML (unescape, add charset, etc.).
     */
    private fun cleanHtml(rawJs: String): String {
        // JavaScript returns the HTML wrapped in quotes and escaped
        var html = rawJs
        if (html.startsWith("\"") && html.endsWith("\"")) {
            html = html.substring(1, html.length - 1)
        }
        // Unescape common JS escape sequences
        html = html
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")

        // Add charset meta if missing
        if (!html.contains("charset", ignoreCase = true)) {
            html = html.replace("<head>", "<head>\n<meta charset=\"UTF-8\">")
        }

        return html
    }

    companion object {
        private const val GET_HTML_JS = """
            (function() {
                return document.documentElement.outerHTML;
            })();
        """
    }
}
