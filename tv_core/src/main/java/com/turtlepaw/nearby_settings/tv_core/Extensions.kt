package com.turtlepaw.nearby_settings.tv_core

/**
 * Extension function to get a setting by key with type safety
 */
fun SettingsSchema.getSetting(key: String): SettingSchema? {
    return schemaItems.find { it.key == key }
}

/**
 * Extension property to get a setting's value with null safety
 */
val SettingSchema.settingValue: String?
    get() = value ?: defaultValue

/**
 * Type-safe extension properties for different setting types
 */
val SettingSchema.booleanValue: Boolean?
    get() = settingValue?.toBoolean()

val SettingSchema.stringValue: String?
    get() = settingValue

val SettingSchema.intValue: Int?
    get() = settingValue?.toIntOrNull()

val SettingSchema.selectedOptions: List<String>
    get() = settingValue?.split(",")?.map { it.trim() } ?: emptyList()

/**
 * Reified extension function to get a typed setting
 */
inline fun <reified T> SettingsSchema.getTypedValue(key: String): T? {
    val setting = getSetting(key) ?: return null
    return when (T::class) {
        Boolean::class -> setting.booleanValue as T?
        String::class -> setting.stringValue as T?
        Int::class -> setting.intValue as T?
        List::class -> setting.selectedOptions as T?
        else -> null
    }
}