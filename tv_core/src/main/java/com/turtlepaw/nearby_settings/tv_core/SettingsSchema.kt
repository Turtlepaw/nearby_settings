package com.turtlepaw.nearby_settings.tv_core

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
enum class SettingType {
    @SerialName("text")
    TEXT,
    @SerialName("number")
    NUMBER,
    @SerialName("toggle")
    TOGGLE,
    @SerialName("select")
    SELECT,
    @SerialName("multiselect")
    MULTI_SELECT
}

@Serializable
data class SettingConstraints(
    val min: Int? = null,
    val max: Int? = null,
    val options: List<String>? = null
)

@Serializable
data class SettingSchema(
    val key: String,
    val label: String,
    val type: SettingType,
    val defaultValue: String? = null,
    val value: String? = defaultValue, // New field to store current value
    val constraints: SettingConstraints? = null,
    /**
     * Description of the setting. Markdown is supported.
     */
    val description: String? = null,
    val required: Boolean = false
)

@Serializable
data class SettingsSchema(
    val schemaItems: List<SettingSchema>
) {
    /**
     * Returns the value of the setting with the given key.
     */
    fun updateSetting(key: String, newValue: String): SettingsSchema {
        return copy(
            schemaItems = schemaItems.map { setting ->
                if (setting.key == key)
                    setting.copy(value = newValue)
                else
                    setting
            }
        )
    }
}

class SettingsManager(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Saves settings only if persistence is enabled
     */
    fun saveSettings(settingsSchema: SettingsSchema, enablePersistence: Boolean = false) {
        if (enablePersistence) {
            val serializedSettings = json.encodeToString(settingsSchema)
            File(context.filesDir, "settings_schema.json").writeText(serializedSettings)
        }
    }

    /**
     * Loads previously saved settings if persistence was used
     * @param enablePersistence Whether to attempt loading persisted settings
     */
    fun loadSettings(enablePersistence: Boolean = false): SettingsSchema? {
        if (!enablePersistence) return null

        val file = File(context.filesDir, "settings_schema.json")
        return if (file.exists()) {
            try {
                val serializedSettings = file.readText()
                json.decodeFromString<SettingsSchema>(serializedSettings)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    /**
     * Validates a setting value against its constraints
     */
    fun validateSettingValue(setting: SettingSchema, value: String): Boolean {
        return when (setting.type) {
            SettingType.NUMBER -> {
                val numValue = value.toIntOrNull()
                numValue != null &&
                        (setting.constraints?.min == null || numValue >= setting.constraints.min) &&
                        (setting.constraints?.max == null || numValue <= setting.constraints.max)
            }
            SettingType.SELECT -> {
                setting.constraints?.options?.contains(value) ?: true
            }
            SettingType.MULTI_SELECT -> {
                val selectedOptions = value.split(",")
                selectedOptions.all { option ->
                    setting.constraints?.options?.contains(option) ?: true
                }
            }
            else -> true
        }
    }
}
