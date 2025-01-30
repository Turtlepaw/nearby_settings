// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'schema.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SettingConstraints _$SettingConstraintsFromJson(Map<String, dynamic> json) =>
    SettingConstraints(
      min: (json['min'] as num?)?.toInt(),
      max: (json['max'] as num?)?.toInt(),
      options:
          (json['options'] as List<dynamic>?)?.map((e) => e as String).toList(),
    );

Map<String, dynamic> _$SettingConstraintsToJson(SettingConstraints instance) =>
    <String, dynamic>{
      'min': instance.min,
      'max': instance.max,
      'options': instance.options,
    };

SettingParent _$SettingParentFromJson(Map<String, dynamic> json) =>
    SettingParent(
      key: json['key'] as String,
      requiredBoolValue: json['requiredBoolValue'] as bool?,
      requiredStringValue: json['requiredStringValue'] as String?,
    );

Map<String, dynamic> _$SettingParentToJson(SettingParent instance) =>
    <String, dynamic>{
      'key': instance.key,
      'requiredBoolValue': instance.requiredBoolValue,
      'requiredStringValue': instance.requiredStringValue,
    };

GroupData _$GroupDataFromJson(Map<String, dynamic> json) => GroupData(
      key: json['key'] as String,
      label: json['label'] as String?,
      description: json['description'] as String?,
    );

Map<String, dynamic> _$GroupDataToJson(GroupData instance) => <String, dynamic>{
      'key': instance.key,
      'label': instance.label,
      'description': instance.description,
    };

SettingSchema _$SettingSchemaFromJson(Map<String, dynamic> json) =>
    SettingSchema(
      key: json['key'] as String,
      label: json['label'] as String,
      type: $enumDecode(_$SettingTypeEnumMap, json['type']),
      defaultValue: json['defaultValue'] as String?,
      value: json['value'] as String?,
      constraints: json['constraints'] == null
          ? null
          : SettingConstraints.fromJson(
              json['constraints'] as Map<String, dynamic>),
      description: json['description'] as String?,
      required: json['required'] as bool? ?? false,
      parent: json['parent'] == null
          ? null
          : SettingParent.fromJson(json['parent'] as Map<String, dynamic>),
      group: json['group'] == null
          ? null
          : GroupData.fromJson(json['group'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$SettingSchemaToJson(SettingSchema instance) =>
    <String, dynamic>{
      'key': instance.key,
      'label': instance.label,
      'type': _$SettingTypeEnumMap[instance.type]!,
      'defaultValue': instance.defaultValue,
      'value': instance.value,
      'constraints': instance.constraints,
      'description': instance.description,
      'required': instance.required,
      'parent': instance.parent,
      'group': instance.group,
    };

const _$SettingTypeEnumMap = {
  SettingType.text: 'text',
  SettingType.number: 'number',
  SettingType.toggle: 'toggle',
  SettingType.select: 'select',
  SettingType.multiSelect: 'multiselect',
};

SettingsSchema _$SettingsSchemaFromJson(Map<String, dynamic> json) =>
    SettingsSchema(
      schemaItems: (json['schemaItems'] as List<dynamic>)
          .map((e) => SettingSchema.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$SettingsSchemaToJson(SettingsSchema instance) =>
    <String, dynamic>{
      'schemaItems': instance.schemaItems,
    };
