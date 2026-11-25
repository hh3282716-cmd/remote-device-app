package com.example.myremotecontrol

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RemoteControlService : AccessibilityService() {

    private var webSocket: WebSocket? = null
    private val serverUrl = "wss://aspect-referenced-alliance-norfolk.trycloudflare.com"
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        connectToServer()
    }

    private fun connectToServer() {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("Device Connected and Ready")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    if (json.getString("action") == "tap") {
                        val x = json.getDouble("x").toFloat()
                        val y = json.getDouble("y").toFloat()
                        performTap(x, y)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                handler.postDelayed({ connectToServer() }, 5000)
            }
        })
    }

    private fun performTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "Service Destroyed")
    }
}
