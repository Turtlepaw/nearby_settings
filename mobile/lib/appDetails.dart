import 'package:json_annotation/json_annotation.dart';

part 'appDetails.g.dart';

@JsonSerializable()
class AppDetails {
  /// Label of the app shown to the user.
  final String label;

  /// Developer of the app shown to the user.
  final String developer;

  /// Website of the app shown to the user.
  final String? website;

  /// Method of contacting the app developer(s) shown to the user. You can provide a:
  ///
  /// - Email address
  /// - Social media handle (e.g. bluesky)
  /// - Any other methods that users can contact you via text
  ///
  /// This is not required, but is highly recommended and may be enforced in later versions.
  final String? contact;

  AppDetails({required this.label, required this.developer, this.website, this.contact});

  factory AppDetails.fromJson(Map<String, dynamic> json) =>
      _$AppDetailsFromJson(json);

  Map<String, dynamic> toJson() => _$AppDetailsToJson(this);
}