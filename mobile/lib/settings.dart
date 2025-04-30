import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import 'package:nearby_settings/schema.dart';
import 'package:nearby_settings/settings_client.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  void _updateSettingValue(String key, String value) async {
    final provider = Provider.of<SettingsClient>(context, listen: false);

    // Create a new settings schema with updated value
    final updatedSchema = provider.schema!.updateSetting(key, value);

    // Update the provider's schema
    await provider.sendSettings(updatedSchema);
  }

  void _saveSettings() async {
    final provider = Provider.of<SettingsClient>(context, listen: false);

    // Create a new schema with updated values
    final updatedSchema = SettingsSchema(
        schemaItems: provider.schema!.schemaItems.map((setting) {
          // You can add any additional transformations or validations here
          return setting.copyWith(value: setting.value ?? setting.defaultValue);
        }).toList());

    // Send the entire updated schema
    await provider.sendSettings(updatedSchema);

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Settings updated'),
      ),
    );
  }

  Widget _buildSettingWidget(SettingSchema setting, {bool enabled = true}) {
    switch (setting.type) {
      case SettingType.text:
        return TextFormField(
          initialValue: setting.value ?? setting.defaultValue,
          //maxLength: setting.constraints?.max,
          //maxLengthEnforcement: MaxLengthEnforcement.enforced,
          validator: (value) {
            print(
                "Validating: $value \n Result: ${value == null ||
                    value.length < (setting.constraints?.min ?? 0)}");
            if (value == null ||
                value.length < (setting.constraints?.min ?? 0)) {
              return 'Minimum length is ${setting.constraints?.min} characters';
            }
            return null;
          },
          onSaved: (value) {
            print("Saved: $value");
          },
          onChanged: (value) => _updateSettingValue(setting.key, value),
          enabled: enabled,
        );
      case SettingType.number:
        return TextFormField(
          initialValue: setting.value ?? setting.defaultValue,
          keyboardType: TextInputType.number,
          decoration: InputDecoration(
            //labelText: setting.label,
          ),
          onChanged: (value) => _updateSettingValue(setting.key, value),
          enabled: enabled,
        );
      case SettingType.toggle:
        return SwitchListTile(
          shape: Theme
              .of(context)
              .cardTheme
              .shape ??
              const RoundedRectangleBorder(
                  borderRadius: BorderRadius.all(Radius.circular(8))),
          title: Text(setting.label),
          subtitle: setting.description != null
              ? buildDescription(setting.description!)
              : null,
          value: (setting.value ?? setting.defaultValue ?? 'false') == 'true',
          onChanged: enabled
              ? (bool value) {
            _updateSettingValue(setting.key, value.toString());
          }
              : null,
        );
      case SettingType.select:
        return DropdownButtonFormField<String>(
          decoration: InputDecoration(
            labelText: setting.label,
            //helperText: setting.description,
          ),
          value: setting.value ?? setting.defaultValue,
          items: setting.constraints?.options?.map((String value) {
            return DropdownMenuItem<String>(
              value: value,
              child: Text(value),
            );
          }).toList(),
          onChanged: enabled
              ? (String? newValue) {
            if (newValue != null) {
              _updateSettingValue(setting.key, newValue);
            }
          }
              : null,
        );
      case SettingType.multiSelect:
        return MultiSelectChip(
          setting: setting,
          onSelectionChanged: (List<String> selectedItems) {
            _updateSettingValue(setting.key, selectedItems.join(','));
          },
        );
      default:
        return Text('Unsupported setting type: ${setting.type}');
    }
  }

  Widget buildDescription(String description) {
    return MarkdownBody(
        data: description,
        styleSheet: MarkdownStyleSheet.fromTheme(Theme.of(context)),
        listItemCrossAxisAlignment: MarkdownListItemCrossAxisAlignment.start,
        onTapLink: assistedLaunchUrl
    );
  }

  void assistedLaunchUrl(String text, String? href, String title) async {
    if (href == null) return print("href is null");
    final url = Uri.parse(href);

    try {
      await launchUrl(url);
    } catch (e) {
      // show snackbar
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Could not launch: $e'),
        ),
      );
    }
  }

  Widget _buildSettingCard(SettingSchema setting) {
    final showLabel = setting.type != SettingType.toggle;
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (showLabel)
              Text(
                setting.label,
                style: Theme
                    .of(context)
                    .textTheme
                    .titleMedium,
              ),
            if (setting.description != null && showLabel)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: buildDescription(setting.description!),
              ),
            if (showLabel) const SizedBox(height: 16),
            _buildSettingWidget(
              setting,
              enabled: Provider
                  .of<SettingsClient>(context)
                  .schema!
                  .isSettingVisible(setting.key),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildGroupCard(GroupData groupData,
      List<SettingSchema> groupSettings) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (groupData.description != null || groupData.label != null)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24).add(
                const EdgeInsets.only(top: 16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (groupData.label != null)
                    Text(
                      groupData.label!,
                      style: Theme
                          .of(context)
                          .textTheme
                          .titleLarge,
                    ),
                  if (groupData.label != null)
                    buildDescription(groupData.description!)
                ],
              ),
            ),
          //const Divider(height: 1),
          ListView.separated(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            padding: const EdgeInsets.all(16),
            itemCount: groupSettings.length,
            separatorBuilder: (context, index) => const SizedBox(),
            //const Divider(height: 32),
            itemBuilder: (context, index) {
              final setting = groupSettings[index];
              return Card.outlined(
                // color: Theme
                //     .of(context)
                //     .colorScheme
                //     .surfaceContainer,
                child: Container(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (setting.type != SettingType.toggle)
                        Text(
                          setting.label,
                          style: Theme
                              .of(context)
                              .textTheme
                              .titleMedium,
                        ),
                      if (setting.description != null &&
                          setting.type != SettingType.toggle)
                        Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: buildDescription(setting.description!),
                        ),
                      if (setting.type != SettingType.toggle)
                        const SizedBox(height: 16),
                      _buildSettingWidget(
                        setting,
                        enabled: Provider
                            .of<SettingsClient>(context)
                            .schema!
                            .isSettingVisible(setting.key),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<SettingsClient>(context);
    final schema = provider.schema;
    return Scaffold(
      appBar: AppBar(
        title: Text(provider.appDetails?.label ?? "App Settings"),
        actions: [
          if (schema != null)
            IconButton(
              icon: const Icon(Icons.save),
              onPressed: _saveSettings,
            ),
        ],
      ),
      body: schema == null
          ? const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(),
            SizedBox(height: 16),
            Text('Loading settings...'),
          ],
        ),
      )
          : ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // First, display ungrouped settings
          ...schema.schemaItems
              .where((setting) => setting.group == null)
              .map(_buildSettingCard),

          // Then, display grouped settings
          ...schema.schemaItems
              .where((setting) => setting.group != null)
              .fold<Map<String, List<SettingSchema>>>(
            {},
                (groups, setting) {
              final groupKey = setting.group!.key;
              groups.putIfAbsent(groupKey, () => []);
              groups[groupKey]!.add(setting);
              return groups;
            },
          )
              .entries
              .map((entry) {
            // Find the GroupData object for this key
            final groupData = schema.schemaItems
                .firstWhere(
                    (setting) => setting.group?.key == entry.key)
                .group!;
            return _buildGroupCard(groupData, entry.value);
          }),

          Card.outlined(
            child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Symbols.warning_rounded,
                          color: Theme
                              .of(context)
                              .colorScheme
                              .error,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          "Keep in mind",
                          style: Theme
                              .of(context)
                              .textTheme
                              .titleMedium
                              ?.copyWith(
                              color: Theme
                                  .of(context)
                                  .colorScheme
                                  .error),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      "Setting labels, descriptions, and other text are defined by the developers of ${provider.appDetails?.label != null ? "\"${provider.appDetails?.label}\"" :                          "the connected app"} and don't reflect Beaverfy's views.",
                      textAlign: TextAlign.center,
                    ),
                    if (provider.appDetails != null)
                      const SizedBox(height: 16),
                    if (provider.appDetails != null)
                      OutlinedButton.icon(
                        onPressed: () {
                          showAboutDeveloperDialog();
                        },
                        label: Text("View developer information"),
                        icon: const Icon(Symbols.info_rounded),
                      )
                  ],
                )),
          ),

          const SizedBox(height: 16),
        ],
      ),
    );
  }

  void showAboutDeveloperDialog() {
    showDialog(
        context: context,
        builder: (context) {
          final provider = Provider.of<SettingsClient>(context);
          return Dialog(
            child: provider.appDetails == null
                ? const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CircularProgressIndicator(),
                  SizedBox(height: 16),
                  Text('Loading app details...'),
                ],
              ),
            )
                : ListView(
              padding: const EdgeInsets.all(16),
              shrinkWrap: true,
              physics: NeverScrollableScrollPhysics(),
              children: [
                Text("About the developer",
                    textAlign: TextAlign.center,
                    style: Theme
                        .of(context)
                        .textTheme
                        .titleLarge),
                const SizedBox(height: 5),
                Text(
                    "The developer has provided this information for their app",
                    textAlign: TextAlign.center,
                    style: Theme
                        .of(context)
                        .textTheme
                        .bodyMedium),
                const SizedBox(height: 12),
                Card.outlined(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      children: [
                        Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Symbols.edit_note_rounded, color: Theme.of(context).colorScheme.primary,),
                            const SizedBox(width: 8),
                            Text("Developer Name",
                                textAlign: TextAlign.center,
                                style:
                                Theme
                                    .of(context)
                                    .textTheme
                                    .titleMedium?.copyWith(color: Theme.of(context).colorScheme.primary))
                          ],
                        ),
                        const SizedBox(height: 8),
                        Text(
                          provider.appDetails!.developer,
                          style: Theme.of(context).textTheme.bodyMedium,),
                      ],
                    ),
                  ),
                ),
                Card.outlined(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      children: [
                        Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Symbols.email_rounded, color: Theme.of(context).colorScheme.primary,),
                            const SizedBox(width: 8),
                            Text("Contact",
                                textAlign: TextAlign.center,
                                style:
                                Theme
                                    .of(context)
                                    .textTheme
                                    .titleMedium?.copyWith(color: Theme.of(context).colorScheme.primary))
                          ],
                        ),
                        const SizedBox(height: 8),
                        MarkdownBody(
                          data: provider.appDetails!.contact ?? "None",
                          styleSheet: MarkdownStyleSheet.fromTheme(Theme.of(
                              context)).copyWith(
                              textAlign: WrapAlignment.center
                          ), onTapLink: assistedLaunchUrl,),
                      ],
                    ),
                  ),
                ),
                Card.outlined(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        children: [
                          Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Symbols.globe_rounded, color: Theme.of(context).colorScheme.primary,),
                              const SizedBox(width: 8),
                              Text("Website",
                                  textAlign: TextAlign.center,
                                  style:
                                  Theme
                                      .of(context)
                                      .textTheme
                                      .titleMedium?.copyWith(color: Theme.of(context).colorScheme.primary))
                            ],
                          ),
                          const SizedBox(height: 8),
                          MarkdownBody(
                            data: provider.appDetails!.website ?? "None",
                            styleSheet: MarkdownStyleSheet.fromTheme(Theme.of(
                                context)).copyWith(
                                textAlign: WrapAlignment.center
                            ), onTapLink: assistedLaunchUrl,),
                        ],
                      ),
                    ))
              ],
            ),
          );
        });
  }
}

// MultiSelectChip remains the same as in the previous implementation
class MultiSelectChip extends StatefulWidget {
  final SettingSchema setting;
  final Function(List<String>) onSelectionChanged;
  final bool enabled;

  const MultiSelectChip({super.key,
    required this.setting,
    required this.onSelectionChanged,
    this.enabled = true});

  @override
  _MultiSelectChipState createState() => _MultiSelectChipState();
}

class _MultiSelectChipState extends State<MultiSelectChip> {
  List<String> _selectedItems = [];

  @override
  void initState() {
    super.initState();
    // Initialize with default or existing values
    if (widget.setting.value != null) {
      _selectedItems = widget.setting.value!.split(',');
    } else if (widget.setting.defaultValue != null) {
      _selectedItems = widget.setting.defaultValue!.split(',');
    }
  }

  @override
  Widget build(BuildContext context) {
    final options = widget.setting.constraints?.options ?? [];

    return Wrap(
      spacing: 8,
      children: options.map((option) {
        return FilterChip(
          label: Text(option),
          selected: _selectedItems.contains(option),
          onSelected: widget.enabled
              ? (bool selected) {
            setState(() {
              if (selected) {
                _selectedItems.add(option);
              } else {
                _selectedItems.remove(option);
              }
              widget.onSelectionChanged(_selectedItems);
            });
          }
              : null,
        );
      }).toList(),
    );
  }
}
