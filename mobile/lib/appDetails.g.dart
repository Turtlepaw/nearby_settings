// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'appDetails.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AppDetails _$AppDetailsFromJson(Map<String, dynamic> json) => AppDetails(
      label: json['label'] as String,
      developer: json['developer'] as String,
      website: json['website'] as String?,
      contact: json['contact'] as String?,
    );

Map<String, dynamic> _$AppDetailsToJson(AppDetails instance) =>
    <String, dynamic>{
      'label': instance.label,
      'developer': instance.developer,
      'website': instance.website,
      'contact': instance.contact,
    };
