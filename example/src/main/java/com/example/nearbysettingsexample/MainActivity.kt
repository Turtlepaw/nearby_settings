package com.example.nearbysettingsexample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.tv.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import com.example.nearbysettingsexample.ui.theme.NearbySettingsExampleTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.turtlepaw.nearby_settings.tv_core.NearbySettingsHost
import com.turtlepaw.nearby_settings.tv_core.SettingConstraints
import com.turtlepaw.nearby_settings.tv_core.SettingSchema
import com.turtlepaw.nearby_settings.tv_core.SettingType
import com.turtlepaw.nearby_settings.tv_core.SettingsSchema
import com.turtlepaw.nearby_settings.tv_core.rememberRequiredPermissions
import kotlinx.coroutines.launch

val defaultSchema = SettingsSchema(
    schemaItems = listOf(
        SettingSchema(
            key = "text_input",
            label = "Text Input",
            description = "**Markdown** *is* supported!",
            type = SettingType.TEXT,
        ),
        SettingSchema(
            key = "number_input",
            label = "Number Input",
            type = SettingType.NUMBER,
        ),
        SettingSchema(
            key = "toggle_input",
            label = "Toggle Input",
            type = SettingType.TOGGLE,
        ),
        SettingSchema(
            key = "select_input",
            label = "Select Input",
            type = SettingType.SELECT,
            constraints = SettingConstraints(
                options = listOf("option1", "option2", "option3")
            )
        ),
        SettingSchema(
            key = "multiselect_input",
            label = "Multiselect Input",
            type = SettingType.MULTI_SELECT,
            constraints = SettingConstraints(
                options = listOf("option1", "option2", "option3")
            )
        )
    )
);

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var schema by remember { mutableStateOf(defaultSchema) }

            val settingsHost = remember {
                NearbySettingsHost(
                    settingsSchema = defaultSchema,
                    onSettingsChanged = { newSettings ->
                        Log.d("SettingsHost", "New settings: $newSettings")
                        schema = newSettings
                    },
                    context = this,
                    enablePersistence = true,
                    automaticallyStart = false
                )
            }

            val isAdvertising by settingsHost.isAdvertising
            var isAdvertisingLoading by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            DisposableEffect(isAdvertising) {
                Log.d("MainActivity", "isAdvertising: $isAdvertising")
                if (isAdvertising) {
                    isAdvertisingLoading = false
                }
                onDispose { }
            }

            NearbySettingsExampleTheme {
                val permissions = rememberRequiredPermissions {}
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    settingsHost.AuthScreen()

                    Text(
                        if (permissions.allPermissionsGranted) "Permissions granted" else "Permissions not granted, can't start advertising",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        enabled = !isAdvertisingLoading,
                        onClick = {
                            if (permissions.allPermissionsGranted) {
                                coroutineScope.launch {
                                    isAdvertisingLoading = true
                                    if (!isAdvertising) {
                                        try {
                                            settingsHost.startAdvertisingSuspend()
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Error starting advertising", e)
                                            isAdvertisingLoading = false
                                        }
                                    } else {
                                        settingsHost.stopAdvertisingSuspend()
                                    }
                                    isAdvertisingLoading = false
                                }
                            } else {
                                if (permissions.shouldShowRationale)
                                    permissions.launchMultiplePermissionRequest()
                                else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Please grant permissions",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Open app settings in android settings
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null)
                                    ).also(::startActivity)
                                }
                            }
                        }
                    ) {
                        if (isAdvertisingLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = LocalContentColor.current,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = if (!permissions.allPermissionsGranted) "Grant Permissions" else if (isAdvertising) "Stop Advertising" else "Start Advertising"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    schema.schemaItems.forEach { setting ->
                        Column(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(vertical = 16.dp, horizontal = 16.dp)
                                .defaultMinSize(minWidth = 200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = setting.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Value
                            Text(
                                text = setting.value ?: "No value set",
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NearbySettingsExampleTheme {
        Greeting("Android")
    }
}