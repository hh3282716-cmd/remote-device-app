package com.example.myremotecontrol

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusText = TextView(this).apply {
            textSize = 18f
            setPadding(40, 100, 40, 40)
            text = "جاري التحقق من صلاحية التحكم..."
        }
        setContentView(statusText)
    }

    override fun onResume() {
        super.onResume()
        if (!isAccessibilityServiceEnabled(RemoteControlService::class.java)) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } else {
            statusText.text = "تم تفعيل الصلاحية والاتصال بالخادم تلقائياً."
        }
    }

    private fun isAccessibilityServiceEnabled(service: Class<*>): Boolean {
        val expectedComponentName = "$packageName/${service.name}"
        val enabledServicesSetting = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            if (colonSplitter.next().equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
