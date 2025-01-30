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
    /**
     * Only applies to [SettingType.NUMBER], [SettingType.TEXT] and [SettingType.MULTI_SELECT].
     *
     * - [SettingType.NUMBER] will limit the value
     * - [SettingType.TEXT] will limit the length of the string
     * - [SettingType.MULTI_SELECT] will limit the number of selected options
     */
    val min: Int? = null,
    /**
     * Only applies to [SettingType.NUMBER], [SettingType.TEXT] and [SettingType.MULTI_SELECT].
     *
     * - [SettingType.NUMBER] will limit the value
     * - [SettingType.TEXT] will limit the length of the string
     * - [SettingType.MULTI_SELECT] will limit the number of selected options
     */
    val max: Int? = null,
    /**
     * Only applies to and required for [SettingType.SELECT] and [SettingType.MULTI_SELECT]. The options will be shown to the user.
     */
    val options: List<String>? = null
)

@Serializable
data class SettingParent(
    val key: String,
    /**
     * Required parent value for this setting to be visible.
     *
     * The parent must be a [SettingType.TOGGLE] or it will be ignored.
     */
    val requiredBoolValue: Boolean? = null,
    /**
     * Required parent value for this setting to be visible.
     *
     * The parent must be a [SettingType.SELECT] or it will be ignored.
     */
    val requiredStringValue: String? = null,
)

@Serializable
data class GroupData(
    val key: String,
    /**
     * Label of the group shown to the user. If null, no label will be shown.
     */
    val label: String? = null,
    /**
     * Description of the group shown to the user. Markdown is supported.
     */
    val description: String? = null,
)

@Serializable
data class SettingSchema(
    /**
     * Key of the setting used internally to identify it.
     */
    val key: String,
    /**
     * Label of the setting shown to the user.
     */
    val label: String,
    /**
     * Type of the setting.
     */
    val type: SettingType,
    /**
     * Default value of the setting stored as a string.
     */
    val defaultValue: String? = null,
    /**
     * Current value of the setting stored as a string. May be overridden if [NearbySettingsHost.enablePersistence] is true.
     */
    val value: String? = defaultValue,
    /**
     * Constraints for the setting.
     */
    val constraints: SettingConstraints? = null,
    /**
     * Description of the setting. Markdown is supported.
     */
    val description: String? = null,
    /**
     * If the setting is required.
     */
    val required: Boolean = false,
    /**
     * Parent that controls this setting's visibility.
     *
     * If specified, this setting is only shown when the parent setting is a specified value.
     */
    val parent: SettingParent? = null,
    /**
     * Group of the setting reflected in the UI.
     */
    val group: GroupData? = null
)

@Serializable
data class SettingsSchema(
    val schemaItems: List<SettingSchema>
) {
    /**
     * Returns whether a setting should be visible based on its parent's state
     */
    fun isSettingVisible(key: String): Boolean {
        val setting = schemaItems.find { it.key == key } ?: return false

        // If no parent specified, always visible
        if (setting.parent == null) return true

        // Find parent setting
        val parentSetting = schemaItems.find { it.key == setting.parent.key } ?: return false
        val parentValue = parentSetting.value ?: parentSetting.defaultValue

        return when (parentSetting.type) {
            SettingType.TOGGLE -> parentValue.toBoolean() == setting.parent.requiredBoolValue
            SettingType.SELECT -> parentValue == setting.parent.requiredStringValue
            else -> true
        }
    }

    /**
     * Returns all visible settings based on current values
     */
    fun getVisibleSettings(): List<SettingSchema> {
        return schemaItems.filter { isSettingVisible(it.key) }
    }

    /**
     * Updates a setting value and returns a new schema
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

/**
 * Used internally to migrate from older versions of the schema defined.
 */
@Serializable
private data class VersionedSettingsSchema(
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