package com.turtlepaw.nearby_settings.tv_core

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import com.google.android.gms.nearby.*
import com.google.android.gms.nearby.connection.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Serializable
data class AppDetails(
    /**
     * Label of the app shown to the user.
     */
    val label: String,
    /**
     * Developer of the app shown to the user.
     */
    val developer: String,
    /**
     * Website of the app shown to the user.
     */
    val website: String? = null,
    /**
     * Method of contacting the app developer(s) shown to the user. You can provide a:
     *
     * - Email address
     * - Social media handle (e.g. bluesky)
     * - Any other methods that users can contact you via text
     *
     * This is not required, but is highly recommended and may be enforced in later versions.
     */
    val contact: String? = null,
)


class NearbySettingsHost(
    /**
     * Settings schema to be displayed to the user.
     */
    private var settingsSchema: SettingsSchema,
    /**
     * Called when mobile app settings are synced.
     */
    private val onSettingsChanged: (SettingsSchema) -> Unit,
    val context: Context,
    /**
     * If true, the app will persist settings across app launches.
     */
    private val enablePersistence: Boolean = false,
    /**
     * If true, the app will automatically start advertising when [NearbySettingsHost] is created.
     */
    automaticallyStart: Boolean = false,
    /**
     * App name shown to users to identify and distinguish your app.
     */
    private val appDetails: AppDetails
) {
    private val settingsManager = SettingsManager(context)
    private val appId = "com.turtlepaw.nearby_settings"
    private val nearby = Nearby.getConnectionsClient(context)

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Assuming rawAuthenticationToken is a ByteArray
            val authEmoji = convertDigitsToEmoji(info.authenticationDigits)

            Log.d(TAG, "onConnectionInitiated: $authEmoji")

            // Show emoji selection UI
            showAuthScreen.value = true

            currentEndpointId = endpointId
            currentChallenge = authEmoji
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                // Connection established, send schema
                sendSchema(endpointId)
                sendAppDetails(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            // Handle disconnection
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val receivedData = String(payload.asBytes() ?: byteArrayOf())
                    // Parse received settings
                    val receivedSchema = Json.decodeFromString<SettingsSchema>(receivedData)
                    updateSettings(receivedSchema)
                }

                Payload.Type.STREAM -> {
                    // Handle stream payload if needed
                }

                Payload.Type.FILE -> {
                    // Handle file payload if needed
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Handle transfer updates if needed
        }
    }

    private var currentEndpointId: String? = null
    private var currentChallenge: EmojiChallenge? = null
    private val showAuthScreen = mutableStateOf(false)
    // Change to private backing field
    private val _isAdvertising = mutableStateOf(false)
    // Expose as State<Boolean> instead of MutableState<Boolean>
    val isAdvertising: State<Boolean> get() = _isAdvertising

    init {
        // Optionally load previously saved settings
        settingsManager.loadSettings(settingsSchema, enablePersistence)?.let { savedSettings ->
            settingsSchema = savedSettings
            onSettingsChanged(savedSettings)
        }

        if(automaticallyStart){
            startAdvertising()
        }
    }

    fun updateSettings(newSettings: SettingsSchema) {
        settingsSchema = newSettings
        settingsManager.saveSettings(newSettings, enablePersistence)
        onSettingsChanged(newSettings)

        // If currently advertising, send updated schema to connected endpoints
        currentEndpointId?.let { endpointId ->
            sendSchema(endpointId)
        }
    }

    data class EmojiChallenge(
        val correctEmoji: String,
        val options: List<String>
    )

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_POINT_TO_POINT)
            .build()

        nearby.startAdvertising(
            Build.MODEL,
            appId,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            _isAdvertising.value = true
            Log.d(TAG, "Advertising started, state updated: ${_isAdvertising.value}")
        }.addOnFailureListener {
            _isAdvertising.value = false
            Log.e(TAG, "Failed to start advertising", it)
        }.addOnCanceledListener {
            _isAdvertising.value = false
            Log.e(TAG, "Advertising canceled")
        }

        Log.d(TAG, "Advertising started")
    }

    fun stopAdvertising() {
        if (_isAdvertising.value == false) {
            throw IllegalStateException("Can't stop advertising when not advertising")
        }

        nearby.stopAdvertising()
        _isAdvertising.value = false
        Log.d(TAG, "Advertising stopped")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startAdvertisingSuspend() {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_POINT_TO_POINT)
            .build()

        try {
            suspendCancellableCoroutine<Boolean> { continuation ->
                val task = nearby.startAdvertising(
                    Build.MODEL,
                    appId,
                    connectionLifecycleCallback,
                    advertisingOptions
                )

                task.addOnSuccessListener {
                    _isAdvertising.value = true
                    Log.d(TAG, "Advertising started, state updated: ${_isAdvertising.value}")
                    continuation.resume(true)
                }.addOnFailureListener { exception ->
                    _isAdvertising.value = false
                    Log.e(TAG, "Failed to start advertising", exception)
                    continuation.resumeWithException(exception)
                }.addOnCanceledListener {
                    _isAdvertising.value = false
                    Log.e(TAG, "Advertising canceled")
                    continuation.cancel()
                }

                continuation.invokeOnCancellation {
                    try {
                        stopAdvertising()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to cancel advertising", e)
                    }
                }
            }
        } catch (e: Exception) {
            _isAdvertising.value = false
            throw e
        }

        Log.d(TAG, "Advertising started")
    }

    suspend fun stopAdvertisingSuspend() {
        if (_isAdvertising.value == false) {
            throw IllegalStateException("Can't stop advertising when not advertising")
        }

        withContext(Dispatchers.IO) {
            nearby.stopAdvertising()
            _isAdvertising.value = false
            Log.d(TAG, "Advertising stopped")
        }
    }

    private fun generateEmojiChallenge(): Pair<String, List<String>> {
        val emojiList = listOf("😀", "😎", "🎮", "🌟", "🎵", "🎨", "📱", "💻", "🎯", "🎲")
        val correct = emojiList.random()
        val options = (emojiList - correct).shuffled().take(2) + correct
        return Pair(correct, options.shuffled())
    }

    fun convertDigitsToEmoji(digits: String): EmojiChallenge {
        Log.d("SettingsHost", "convertDigitsToEmoji: $digits")
        val emojis = listOf("😀", "😎", "🎮", "🌟", "🎵", "🎨", "📱", "💻", "🎯", "🎲")

        // Simple, consistent hash function
        fun simpleHash(input: String): Int {
            var hash = 0
            for (char in input) {
                hash = (hash * 31 + char.code) and 0xFFFFFFFF.toInt()
            }
            return kotlin.math.abs(hash)
        }

        val hash = simpleHash(digits)
        val index = hash % emojis.size
        val correctEmoji = emojis[index]

        // Generate unique options
        val otherEmojis = emojis.filter { it != correctEmoji }.shuffled().take(2)
        val options = (otherEmojis + correctEmoji).shuffled()

        return EmojiChallenge(correctEmoji, options)
    }


    private fun sendSchema(endpointId: String) {
        Log.d(TAG, "Sending schema to $endpointId, schema: $settingsSchema")
        val schemaBytes = Json.encodeToString(settingsSchema)
            .toByteArray(Charsets.UTF_8)

        nearby.sendPayload(endpointId, Payload.fromBytes(schemaBytes))
    }

    private fun sendAppDetails(endpointId: String) {
        Log.d(TAG, "Sending app name to $endpointId, ")
        if(appDetails.contact == null) Log.w(TAG, "NearbySettingsHost.appDetails#contact isn't set. This is highly recommended to set, as it may be enforced in later versions.")
        val schemaBytes = Json.encodeToString(appDetails)
            .toByteArray(Charsets.UTF_8)

        nearby.sendPayload(endpointId, Payload.fromBytes(schemaBytes))
    }

    @Composable
    fun AuthScreen() {
        if (showAuthScreen.value && currentChallenge != null) {
            EmojiAuthDialog(
                challenge = currentChallenge!!,
                onEmojiSelected = { selectedEmoji ->
                    handleEmojiSelection(selectedEmoji)
                }
            )
        }
    }

    private fun handleEmojiSelection(selectedEmoji: String) {
        currentChallenge?.let { challenge ->
            if (selectedEmoji == challenge.correctEmoji) {
                currentEndpointId?.let { endpointId ->
                    nearby.acceptConnection(endpointId, payloadCallback)
                }
            } else {
                currentEndpointId?.let { endpointId ->
                    nearby.rejectConnection(endpointId)
                }
            }
            showAuthScreen.value = false
        }
    }

    /**
     * Returns {@code true} if the app was granted all the permissions. Otherwise, returns {@code
     * false}.
     *
     * Code adapted from https://github.com/android/connectivity-samples/blob/main/NearbyConnectionsWalkieTalkie/app/src/main/java/com/google/location/nearby/apps/walkietalkie/ConnectionsActivity.java
     */
    private fun hasPermissions(context: Context, permissions: List<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false;
            }
        }
        return true;
    }

    fun requestRequiredPermissions(activity: Activity, requestCode: Int = 0) {
        if (!hasPermissions(context, getRequiredPermissions())) {
            ActivityCompat.requestPermissions(
                activity, getRequiredPermissions().toTypedArray(), requestCode
            );
        }
    }

    companion object {
        private const val TAG = "NearbySettingsHost"
    }
}

/**
 * Code adapted from https://github.com/android/connectivity-samples/blob/main/NearbyConnectionsWalkieTalkie/app/src/main/java/com/google/location/nearby/apps/walkietalkie/ConnectionsActivity.java
 */
fun getRequiredPermissions(): List<String> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES,
        );
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        );
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        );
    } else {
        return listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        );
    }
}