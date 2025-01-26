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
  final int? min;
  final int? max;
  final List<String>? options;

  SettingConstraints({this.min, this.max, this.options});

  factory SettingConstraints.fromJson(Map<String, dynamic> json) =>
      _$SettingConstraintsFromJson(json);

  Map<String, dynamic> toJson() => _$SettingConstraintsToJson(this);
}

@JsonSerializable()
class SettingSchema {
  final String key;
  final String label;
  final SettingType type;
  final String? defaultValue;
  final String? value; // New field to store current value
  final SettingConstraints? constraints;
  final String? description;
  final bool required;

  SettingSchema({
    required this.key,
    required this.label,
    required this.type,
    this.defaultValue,
    this.value,
    this.constraints,
    this.description,
    this.required = false,
  });

  // Create a copy with updated value
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

  // Create a copy with updated settings
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