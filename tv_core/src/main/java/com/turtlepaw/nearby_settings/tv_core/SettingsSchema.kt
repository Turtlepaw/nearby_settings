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
    /**
     * If the setting is required.
     */
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

@Serializable
data class VersionedSettingsSchema(
    val schemaItems: List<SettingSchema>,
    val version: Int = 1
)

class SettingsManager(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Saves settings with version tracking
     */
    fun saveSettings(settingsSchema: SettingsSchema, enablePersistence: Boolean = false) {
        if (enablePersistence) {
            val versionedSchema = VersionedSettingsSchema(settingsSchema.schemaItems)
            val serializedSettings = json.encodeToString(versionedSchema)
            File(context.filesDir, "settings_schema.json").writeText(serializedSettings)
        }
    }

    /**
     * Loads settings and preserves values when schema changes
     */
    fun loadSettings(
        currentSchema: SettingsSchema,
        enablePersistence: Boolean = false
    ): SettingsSchema? {
        if (!enablePersistence) return null

        val file = File(context.filesDir, "settings_schema.json")
        if (!file.exists()) return null

        return try {
            val serializedSettings = file.readText()
            val savedSchema = json.decodeFromString<VersionedSettingsSchema>(serializedSettings)

            // Create map of saved values
            val savedValues = savedSchema.schemaItems.associate { it.key to it.value }

            // Apply saved values to current schema where types match
            SettingsSchema(
                schemaItems = currentSchema.schemaItems.map { setting ->
                    val savedValue = savedValues[setting.key]
                    if (savedValue != null && validateSettingValue(setting, savedValue)) {
                        setting.copy(value = savedValue)
                    } else {
                        setting
                    }
                }
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validates a setting value against its constraints
     */
    fun validateSettingValue(setting: SettingSchema, value: String): Boolean {
        if (value == setting.defaultValue) return true

        return try {
            when (setting.type) {
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
                    val selectedOptions = value.split(",").map { it.trim() }
                    selectedOptions.all { option ->
                        setting.constraints?.options?.contains(option) ?: true
                    }
                }
                SettingType.TOGGLE -> {
                    value == "true" || value == "false"
                }
                SettingType.TEXT -> true
            }
        } catch (e: Exception) {
            false
        }
    }
}