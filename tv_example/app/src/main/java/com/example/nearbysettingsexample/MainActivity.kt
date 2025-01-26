package com.example.nearbysettingsexample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.tv.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import com.example.nearbysettingsexample.ui.theme.NearbySettingsExampleTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi

val defaultSchema = SettingsSchema(
    schemaItems = listOf(
        SettingSchema(
            key = "text_input",
            label = "Text Input",
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

            val settingsHost = NearbySettingsHost(
                settingsSchema = defaultSchema,
                onSettingsChanged = { newSettings ->
                    Log.d("SettingsHost", "New settings: $newSettings")
                    schema = newSettings
                },
                context = this,
                enablePersistence = true,
                automaticallyStart = true
            )

            val isAdvertising by settingsHost.isAdvertising

            NearbySettingsExampleTheme {
                val permissions = rememberRequiredPermissions {}
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    settingsHost.AuthScreen()

                    if (permissions.allPermissionsGranted) {
                        Text("Permissions granted", color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    schema.schemaItems.forEach { setting ->
                        Card(
                            onClick = {},
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                Text(
                                    text = setting.label,
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Value
                                Text(
                                    text = setting.value ?: "",
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if(permissions.allPermissionsGranted){
                                if(!isAdvertising){
                                    settingsHost.startAdvertising()
                                } else {
                                    settingsHost.stopAdvertising()
                                }
                            } else {
                                permissions.launchMultiplePermissionRequest()
                            }
                        }
                    ) {
                        Text(
                            text = if (isAdvertising) "Stop Advertising" else "Start Advertising"
                        )
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
    TVCoreTheme {
        Greeting("Android")
    }
}