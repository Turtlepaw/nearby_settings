import 'dart:convert';
import 'dart:ffi';
import 'dart:math';
import 'package:crclib/catalog.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:location/location.dart';
import 'package:nearby_connections/nearby_connections.dart';
import 'package:nearby_settings/appDetails.dart';
import 'package:nearby_settings/schema.dart';
import 'package:pair/pair.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';

class DeviceConnection {
  final String id;
  final String name;
  ConnectionStatus status;

  DeviceConnection({
    required this.id,
    required this.name,
    this.status = ConnectionStatus.discovered
  });
}

enum ConnectionStatus {
  discovered,
  connecting,
  connected,
  disconnected
}

class SettingsClient with ChangeNotifier {
  final String appId = "com.turtlepaw.nearby_settings";
  final Nearby _nearby = Nearby();
  Map<String, DeviceConnection> discoveredDevices = {};

  String? _currentEmoji;
  String? _currentEndpointId;
  BuildContext? _context;

  bool _isConnecting = false;
  bool _isDiscovering = false;
  SettingsSchema? _schema;
  AppDetails? _appDetails;

  bool get isConnecting => _isConnecting;
  bool get isDiscovering => _isDiscovering;
  String? get connectedId => _currentEndpointId;
  SettingsSchema? get schema => _schema;
  AppDetails? get appDetails => _appDetails;

  SettingsClient({BuildContext? context})
      : _context = context;

  SettingsClient useContext(BuildContext context){
    _context = context;
    return this;
  }

  Future<void> requestPermissions() async {
    // location permission
    if (!await Permission.location.isGranted) {
      await Permission.location.request();
    }

    // Check Location Status
    if (!await Permission.location.serviceStatus.isEnabled) {
      await Location.instance.requestService();
    }

    // Bluetooth permissions
    bool granted = !(await Future.wait([
      // Check Permissions
      Permission.bluetooth.isGranted,
      Permission.bluetoothAdvertise.isGranted,
      Permission.bluetoothConnect.isGranted,
      Permission.bluetoothScan.isGranted,
    ]))
        .any((element) => false);

    if (!granted) {
      [
        // Ask Permissions
        Permission.bluetooth,
        Permission.bluetoothAdvertise,
        Permission.bluetoothConnect,
        Permission.bluetoothScan
      ].request();
    }

// Check Bluetooth Status
    if (!await Permission.bluetooth.serviceStatus.isEnabled) {
      // TODO: open bluetooth settings
    }

    if (!await Permission.nearbyWifiDevices.isGranted) {
      await Permission.nearbyWifiDevices.request();
    }

    return;
  }

  Future<void> startDiscovery(BuildContext context) async {
    try {
      await requestPermissions();

      discoveredDevices = {};

      final success = await _nearby.startDiscovery(
          "mobile_client", // Explicitly define service ID
          Strategy.P2P_POINT_TO_POINT,
          onEndpointFound: _onEndpointFound,
          onEndpointLost: _onEndpointLost,
          serviceId: appId);

      if (success) {
        print('Discovery started successfully');
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Searching for devices nearby...'),
          ),
        );
        _isDiscovering = true;
        notifyListeners();
      } else {
        print('Failed to start discovery');
      }
    } catch (e) {
      _isDiscovering = false;
      notifyListeners();
      // if STATUS_ALREADY_DISCOVERING 8002; restart
      if (e.toString().contains("STATUS_ALREADY_DISCOVERING")) {
        await stopDiscovery();
        return await startDiscovery(context);
      }
      _showErrorDialog('Failed to start discovery: $e');
    }
  }

  Future<void> stopDiscovery() async {
    try {
      await _nearby.stopDiscovery();
      _isDiscovering = false;

      // Remove all unconnected endpoints
      //discovered = discovered.where((element) => element.key == _currentEndpointId).toSet();

      notifyListeners();
    } catch (e) {
      _showErrorDialog('Failed to stop discovery: $e');
    }
  }

  void _onEndpointFound(String id, String name, String serviceId) {
    print('Endpoint found: $id, $name, $serviceId');
    discoveredDevices[id] = DeviceConnection(
        id: id,
        name: name
    );
    notifyListeners();
  }

  void _onEndpointLost(String? id) {
    print('Endpoint lost: $id');
    if (id != null) {
      discoveredDevices.remove(id);
      // If lost device was current connection, reset
      if (id == _currentEndpointId) {
        _currentEndpointId = null;
        _schema = null;
      }
    }
    notifyListeners();
  }

  void _onConnectionInitiated(String id, ConnectionInfo info) {
    _currentEndpointId = id;

    // Generate and show emoji
    _currentEmoji = _convertDigitsToEmoji(info.authenticationDigits);
    showEmojiScreen(_currentEmoji!);

    // Accept
    Nearby().acceptConnection(id, onPayLoadRecieved: (endpointId, payload) {
      if(payload.type == PayloadType.BYTES && payload.bytes != null && payload.bytes!.isNotEmpty){
        String decodedString = utf8.decode(payload.bytes!);
        var json = jsonDecode(decodedString);

        try {
          var settings = SettingsSchema.fromJson(json);
          _schema = settings;
          notifyListeners();
        } catch (e) {
          try {
            var appDetails = AppDetails.fromJson(json);
            _appDetails = appDetails;
            notifyListeners();
          } catch (e) {
            print('Failed to parse payload as either SettingsSchema or AppDetails');
          }
        }
      }
    });
  }

  Future<bool> requestConnection(String id) {
    discoveredDevices[id]?.status = ConnectionStatus.connecting;
    _isConnecting = true;
    notifyListeners();

    return _nearby.requestConnection(
      'MOBILE_CLIENT',
      id,
      onConnectionResult: (String endpointId, Status status) {
        dismissDialog({ String message = 'Connection rejected' }) {
          // dismiss dialog if poppable
          if (_context != null) {
            if (Navigator.canPop(_context!)) {
              Navigator.pop(_context!);
            }
            ScaffoldMessenger.of(_context!).showSnackBar(
              SnackBar(
                content: Text(message),
              ),
            );
          }
        }

        if (status == Status.CONNECTED) {
          discoveredDevices[endpointId]?.status = ConnectionStatus.connected;
          _currentEndpointId = endpointId;
          dismissDialog(message: "Connected");
          print('Connected to $endpointId');
          // Stop discovery
          stopDiscovery();
          // Redirect
          _context?.push("/settings");
        } else if (status == Status.REJECTED) {
          dismissDialog();
          discoveredDevices[endpointId]?.status = ConnectionStatus.disconnected;
          _isConnecting = false;
          _currentEndpointId = null;
          notifyListeners();
          // disconnect
          _nearby.disconnectFromEndpoint(endpointId);
          print('Connection rejected');
        } else if (status == Status.ERROR) {
          discoveredDevices[endpointId]?.status = ConnectionStatus.disconnected;
          _isConnecting = false;
          notifyListeners();
          dismissDialog();
          print('Connection failed: $status');
        } else {
          discoveredDevices[endpointId]?.status = ConnectionStatus.disconnected;
          _isConnecting = false;
          notifyListeners();
          print('Connection failed: $status');
        }
      },
      onDisconnected: (String endpointId) {
        print('Disconnected from $endpointId');
        _currentEndpointId = null;
        discoveredDevices[endpointId]?.status = ConnectionStatus.disconnected;
        notifyListeners();
        // Pop if /settings
        if (_context?.canPop() ?? false) {
          _context?.pop();
        }
      },
      onConnectionInitiated: _onConnectionInitiated,
    );
  }

  ConnectionStatus getDeviceConnectionStatus(String deviceId) {
    return discoveredDevices[deviceId]?.status ?? ConnectionStatus.discovered;
  }

  void showEmojiScreen(String emoji) {
    if (_context == null) return;

    showDialog(
      context: _context!,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return Dialog(
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'Select this emoji on your TV',
                  style: Theme.of(context).textTheme.titleLarge,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 20),
                Text(
                  emoji,
                  style: const TextStyle(fontSize: 80),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  void _showErrorDialog(String message) {
    if (_context == null) return;

    showDialog(
      context: _context!,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Error'),
          content: Text(message),
          actions: [
            TextButton(
              child: const Text('OK'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

// In SettingsClient class
  Future<void> sendSettings(SettingsSchema newSchema) async {
    if (_currentEndpointId == null) {
      _showErrorDialog('No active connection');
      return;
    }

    try {
      // Convert the entire schema to JSON
      final settingsJson = json.encode(newSchema.toJson());
      final settingsBytes = utf8.encode(settingsJson);

      // Send the entire schema as bytes payload
      await _nearby.sendBytesPayload(_currentEndpointId!, settingsBytes);

      // Update the local schema state
      _schema = newSchema;
      notifyListeners();
    } catch (e) {
      _showErrorDialog('Failed to send settings: $e');
    }
  }

  String _convertDigitsToEmoji(String digits) {
    print("Digits: $digits");
    final emojis = ["😀", "😎", "🎮", "🌟", "🎵", "🎨", "📱", "💻", "🎯", "🎲"];

    // Simple, consistent hash function
    int simpleHash(String input) {
      int hash = 0;
      for (int i = 0; i < input.length; i++) {
        hash = (hash * 31 + input.codeUnitAt(i)) & 0xFFFFFFFF;
      }
      return hash.abs();
    }

    final hash = simpleHash(digits);
    final index = hash % emojis.length;
    return emojis[index];
  }
}
