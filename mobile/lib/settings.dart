import 'package:flutter/material.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:nearby_settings/schema.dart';
import 'package:nearby_settings/settings_client.dart';
import 'package:provider/provider.dart';

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
          return setting.copyWith(
              value: setting.value ?? setting.defaultValue
          );
        }).toList()
    );

    // Send the entire updated schema
    await provider.sendSettings(updatedSchema);

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Settings updated'),
      ),
    );
  }

  Widget _buildSettingWidget(SettingSchema setting) {
    switch (setting.type) {
      case SettingType.text:
        return TextFormField(
          initialValue: setting.value ?? setting.defaultValue,
          decoration: InputDecoration(
            labelText: setting.label,
            helperText: setting.description,
          ),
          onChanged: (value) => _updateSettingValue(setting.key, value),
        );
      case SettingType.number:
        return TextFormField(
          initialValue: setting.value ?? setting.defaultValue,
          keyboardType: TextInputType.number,
          decoration: InputDecoration(
            labelText: setting.label,
            helperText: setting.description,
          ),
          onChanged: (value) => _updateSettingValue(setting.key, value),
        );
      case SettingType.toggle:
        return SwitchListTile(
          title: Text(setting.label),
          subtitle: setting.description != null
              ? Text(setting.description!)
              : null,
          value: (setting.value ?? setting.defaultValue ?? 'false') == 'true',
          onChanged: (bool value) {
            _updateSettingValue(setting.key, value.toString());
          },
        );
      case SettingType.select:
        return DropdownButtonFormField<String>(
          decoration: InputDecoration(
            labelText: setting.label,
            helperText: setting.description,
          ),
          value: setting.value ?? setting.defaultValue,
          items: setting.constraints?.options?.map((String value) {
            return DropdownMenuItem<String>(
              value: value,
              child: Text(value),
            );
          }).toList(),
          onChanged: (String? newValue) {
            if (newValue != null) {
              _updateSettingValue(setting.key, newValue);
            }
          },
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

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<SettingsClient>(context);
    final schema = provider.schema;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
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
        children: schema.schemaItems.map((setting) {
          return Card(
            margin: const EdgeInsets.symmetric(vertical: 8),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    setting.label,
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  if (setting.description != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        "desk",
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ),
                  const SizedBox(height: 16),
                  _buildSettingWidget(setting),
                ],
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
}

// MultiSelectChip remains the same as in the previous implementation
class MultiSelectChip extends StatefulWidget {
  final SettingSchema setting;
  final Function(List<String>) onSelectionChanged;

  const MultiSelectChip({
    super.key,
    required this.setting,
    required this.onSelectionChanged
  });

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
          onSelected: (bool selected) {
            setState(() {
              if (selected) {
                _selectedItems.add(option);
              } else {
                _selectedItems.remove(option);
              }
              widget.onSelectionChanged(_selectedItems);
            });
          },
        );
      }).toList(),
    );
  }
}