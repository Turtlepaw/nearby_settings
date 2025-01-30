import 'package:json_annotation/json_annotation.dart';

part 'schema.g.dart';

enum SettingType {
  @JsonValue('text')
  text,
  @JsonValue('number')
  number,
  @JsonValue('toggle')
  toggle,
  @JsonValue('select')
  select,
  @JsonValue('multiselect')
  multiSelect
}

@JsonSerializable()
class SettingConstraints {
  /// Only applies to [SettingType.number], [SettingType.text] and [SettingType.multiSelect]
  ///
  /// - [SettingType.number] will limit the value
  /// - [SettingType.text] will limit the length of the string
  /// - [SettingType.multiSelect] will limit the number of selected options
  final int? min;

  /// Only applies to [SettingType.number], [SettingType.text] and [SettingType.multiSelect]
  ///
  /// - [SettingType.number] will limit the value
  /// - [SettingType.text] will limit the length of the string
  /// - [SettingType.multiSelect] will limit the number of selected options
  final int? max;

  /// Only applies to and required for [SettingType.select] and [SettingType.multiSelect]
  /// The options will be shown to the user.
  final List<String>? options;

  SettingConstraints({this.min, this.max, this.options});

  factory SettingConstraints.fromJson(Map<String, dynamic> json) =>
      _$SettingConstraintsFromJson(json);

  Map<String, dynamic> toJson() => _$SettingConstraintsToJson(this);
}

@JsonSerializable()
class SettingParent {
  /// Key of the parent setting
  final String key;

  /// Required parent value for this setting to be visible.
  ///
  /// The parent must be a [SettingType.toggle] or it will be ignored.
  final bool? requiredBoolValue;

  /// Required parent value for this setting to be visible.
  ///
  /// The parent must be a [SettingType.select] or it will be ignored.
  final String? requiredStringValue;

  SettingParent({
    required this.key,
    this.requiredBoolValue,
    this.requiredStringValue,
  });

  factory SettingParent.fromJson(Map<String, dynamic> json) =>
      _$SettingParentFromJson(json);

  Map<String, dynamic> toJson() => _$SettingParentToJson(this);
}

@JsonSerializable()
class GroupData {
  /// Key of the parent setting
  final String key;

  /// Label of the group shown to the user. If null, no label will be shown.
  final String? label;

  /// Description of the group shown to the user. Markdown is supported.
  final String? description;

  GroupData({
    required this.key,
    this.label,
    this.description,
  });

  factory GroupData.fromJson(Map<String, dynamic> json) =>
      _$GroupDataFromJson(json);

  Map<String, dynamic> toJson() => _$GroupDataToJson(this);
}

@JsonSerializable()
class SettingSchema {
  /// Key of the setting used internally to identify it.
  final String key;

  /// Label of the setting shown to the user.
  final String label;

  /// Type of the setting.
  final SettingType type;

  /// Default value of the setting stored as a string.
  final String? defaultValue;

  /// Current value of the setting stored as a string.
  final String? value;

  /// Constraints for the setting.
  final SettingConstraints? constraints;

  /// Description of the setting. Markdown is supported.
  final String? description;

  /// If the setting is required.
  final bool required;

  /// Parent that controls this setting's visibility.
  ///
  /// If specified, this setting is only shown when the parent setting is a specified value.
  final SettingParent? parent;

  /// Group of the setting reflected in the UI.
  final GroupData? group;

  SettingSchema({
    required this.key,
    required this.label,
    required this.type,
    this.defaultValue,
    String? value,
    this.constraints,
    this.description,
    this.required = false,
    this.parent,
    this.group
  }) : value = value ?? defaultValue;

  /// Create a copy with updated value
  SettingSchema copyWith({String? value}) {
    return SettingSchema(
      key: key,
      label: label,
      type: type,
      defaultValue: defaultValue,
      value: value ?? this.value,
      constraints: constraints,
      description: description,
      required: required,
      parent: parent,
      group: group
    );
  }

  factory SettingSchema.fromJson(Map<String, dynamic> json) =>
      _$SettingSchemaFromJson(json);

  Map<String, dynamic> toJson() => _$SettingSchemaToJson(this);
}

@JsonSerializable()
class SettingsSchema {
  final List<SettingSchema> schemaItems;

  SettingsSchema({required this.schemaItems});

  /// Returns whether a setting should be visible based on its parent's state
  bool isSettingVisible(String key) {
    final setting = schemaItems.firstWhere(
          (s) => s.key == key,
      orElse: () => throw Exception('Setting not found: $key'),
    );

    // If no parent specified, always visible
    if (setting.parent == null) return true;

    // Find parent setting
    final parentSetting = schemaItems.firstWhere(
          (s) => s.key == setting.parent!.key,
      orElse: () => throw Exception('Parent setting not found: ${setting.parent!.key}'),
    );

    final parentValue = parentSetting.value ?? parentSetting.defaultValue;
    if (parentValue == null) return true;

    switch (parentSetting.type) {
      case SettingType.toggle:
        if (setting.parent!.requiredBoolValue == null) return true;
        return parentValue.toLowerCase() == setting.parent!.requiredBoolValue.toString();
      case SettingType.select:
        if (setting.parent!.requiredStringValue == null) return true;
        return parentValue == setting.parent!.requiredStringValue;
      default:
        return true;
    }
  }

  /// Returns all visible settings based on current values
  List<SettingSchema> getVisibleSettings() {
    return schemaItems.where((setting) => isSettingVisible(setting.key)).toList();
  }

  /// Create a copy with updated settings
  SettingsSchema updateSetting(String key, String newValue) {
    return SettingsSchema(
      schemaItems: schemaItems.map((setting) {
        return setting.key == key
            ? setting.copyWith(value: newValue)
            : setting;
      }).toList(),
    );
  }

  factory SettingsSchema.fromJson(Map<String, dynamic> json) =>
      _$SettingsSchemaFromJson(json);

  Map<String, dynamic> toJson() => _$SettingsSchemaToJson(this);
}