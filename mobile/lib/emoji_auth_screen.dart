import 'package:flutter/material.dart';

class EmojiAuthScreen extends StatelessWidget {
  final String emoji;

  const EmojiAuthScreen({super.key, required this.emoji});

  @override
  Widget build(BuildContext context) {
    return Dialog(
      child: Container(
        padding: EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              'Select this emoji on your TV',
              style: Theme.of(context).textTheme.titleLarge,
              textAlign: TextAlign.center,
            ),
            SizedBox(height: 24),
            Text(
              emoji,
              style: TextStyle(fontSize: 64),
            ),
          ],
        ),
      ),
    );
  }
}