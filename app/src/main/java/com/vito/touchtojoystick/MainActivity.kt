package com.vito.touchtojoystick

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vito.touchtojoystick.service.AutoClickService
import com.vito.touchtojoystick.service.ForegroundService
import com.vito.touchtojoystick.ui.theme.TouchToJoystickTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TouchToJoystickTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(32.dp),

                    ) {
                        Greeting()
                        Button(onClick = {
                            checkOverlayPermission()
                            startService()
                        }) {
                            Text("SETUP")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AutoClickService.instance?.stopSelf()
    }
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            // send user to the device settings
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
    }

    private fun checkAccess(): Boolean {
        val string = getString(R.string.accessibility_service_id)
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val list = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)

        Log.d("TEST", "my access id $string")
        for (id in list) {
            Log.d("TEST", "access id $id")
            if (string == id.id) {
                return true
            }
        }
        return false
    }

    private fun startService() {
        // check if the user has already granted
        // the Draw over other apps permission
        val hasPermission = checkAccess()
        if (!hasPermission) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }

        if (Settings.canDrawOverlays(this)) {
            // start the service based on the android version
            Log.d("TEST", "CAN START FOREGROUND SERVICE")
            startForegroundService(Intent(this, ForegroundService::class.java))
            startForegroundService(Intent(this, ForegroundService::class.java))

            moveTaskToBack(true)

            ForegroundService.instance?.getWindow()?.close()
        } else {
            Log.d("TEST", "CAN'T START FOREGROUND SERVICE")
        }
    }
}

@Composable
fun Greeting() {
    Text(text = "Allow TouchToJoystick to \"Display over other apps\" (the joystick) \nand enable accessibility to translate joystick movements into auto-clicks")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TouchToJoystickTheme {
        Greeting()
    }
}
