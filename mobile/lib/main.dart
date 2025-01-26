import 'package:dynamic_color/dynamic_color.dart';
import 'package:flutter/material.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import 'package:nearby_settings/settings.dart';
import 'package:nearby_settings/settings_client.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';

final _rootNavigatorKey = GlobalKey<NavigatorState>();

CustomTransitionPage buildPageWithDefaultTransition<T>({
  required BuildContext context,
  required GoRouterState state,
  required Widget child,
}) {
  return CustomTransitionPage<T>(
    key: state.pageKey,
    child: child,
    transitionDuration: const Duration(milliseconds: 250),
    reverseTransitionDuration: const Duration(milliseconds: 150),
    transitionsBuilder: (context, animation, secondaryAnimation, child) =>
        FadeTransition(opacity: animation, child: child),
  );
}

Page<dynamic> Function(BuildContext, GoRouterState) defaultPageBuilder<T>(
    Widget child) =>
        (BuildContext context, GoRouterState state) {
      return buildPageWithDefaultTransition<T>(
        context: context,
        state: state,
        child: child,
      );
    };

final _router = GoRouter(
  //debugLogDiagnostics: kDebugMode,
    initialLocation: '/',
    navigatorKey: _rootNavigatorKey,
    routes: [
      GoRoute(
          path: '/',
          builder: (context, state) => const HomePage()
      ),
      GoRoute(
          path: '/settings',
          builder: (context, state) => const SettingsPage()
      ),
    ]);

void main() {
  runApp(ChangeNotifierProvider(
    child: const App(),
    create: (context) =>
      SettingsClient(
        context: context,
      )
  ));
}

class App extends StatelessWidget {
  const App({super.key});

  static final _color = Colors.deepPurple;
  static final _defaultLightColorScheme =
      ColorScheme.fromSeed(seedColor: _color);

  static final _defaultDarkColorScheme =
      ColorScheme.fromSeed(seedColor: _color, brightness: Brightness.dark);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return DynamicColorBuilder(builder: (lightColorScheme, darkColorScheme) {
      return MaterialApp.router(
        routerConfig: _router,
        title: 'Flutter Demo',
        theme: ThemeData(
          colorScheme: lightColorScheme ?? _defaultLightColorScheme,
          useMaterial3: true,
          iconTheme: const IconThemeData(
              color: Colors.black, fill: 1, weight: 400, opticalSize: 24),
        ),
        darkTheme: ThemeData(
          colorScheme: darkColorScheme ?? _defaultDarkColorScheme,
          useMaterial3: true,
          iconTheme: const IconThemeData(
            color: Colors.white,
            fill: 1,
            weight: 400,
            opticalSize: 24,
          ),
        ),
        themeMode: ThemeMode.system,
      );
    });
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  void initState() {
    final settingsClient = Provider.of<SettingsClient>(context, listen: false).useContext(context);
    if(!settingsClient.isDiscovering) {
          settingsClient.startDiscovery(context);
    }

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final settingsClient = Provider.of<SettingsClient>(context).useContext(context);
    final isDiscovering = settingsClient.isDiscovering;
    final theme = Theme.of(context);
    return Scaffold(
        appBar: AppBar(
          title: const Text("Nearby Settings"),
          bottom: isDiscovering
              ? PreferredSize(
                  preferredSize: const Size.fromHeight(2.0),
                  child: LinearProgressIndicator(year2023: false,),
                )
              : null,
          actions: [
            Tooltip(
              message: isDiscovering ? "Stop Discovery" : "Start Discovery",
              child: IconButton(
                  onPressed: () {
                    if (isDiscovering) {
                      settingsClient.stopDiscovery();
                    } else {
                      settingsClient.startDiscovery(context);
                    }
                  },
                  icon: Icon(isDiscovering ? Symbols.stop_rounded : Symbols.sensors_rounded)),
            )
          ],
        ),
        body: Center(
            // Center is a layout widget. It takes a single child and positions it
            // in the middle of the parent.
            child: ListView(
          children: [
            const SizedBox(height: 10),
            ...settingsClient.discoveredDevices.entries.map((entry) {
              final key = entry.key;
              final device = entry.value;
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 10),
                  clipBehavior: Clip.antiAlias,
                  child: InkWell(
                    onTap: () async {
                      if (settingsClient.connectedId == device.id) {
                        context.push('/settings');
                        return;
                      }

                      final result = await settingsClient.requestConnection(device.id);

                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(
                            result ? 'Connected' : 'Failed to connect',
                          ),
                        ),
                      );
                    },
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Row(
                            children: [
                              Icon(Symbols.connected_tv_rounded, size: 32),
                              const SizedBox(width: 16),
                              Text(device.name, style: theme.textTheme.titleMedium),
                            ],
                          ),
                          if (device.status == ConnectionStatus.connected)
                            Row(
                              children: [
                                Text("Connected", style: theme.textTheme.bodySmall),
                                //const SizedBox(width: 10),
                                //Icon(Symbols.open_in_new_rounded, color: theme.colorScheme.onSurfaceVariant, size: 18)
                              ],
                            )
                          else if(device.status == ConnectionStatus.connecting)
                            SizedBox(
                              height: 35,
                              width: 35,
                              child: CircularProgressIndicator(year2023: false, strokeWidth: 3,),
                            )
                        ],
                      ),
                    ),
                  ));
            })
          ],
        )));
  }
}
