package com.turtlepaw.nearby_settings.tv_core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceColors
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text

@Composable
fun EmojiAuthDialog(
    challenge: SettingsHost.EmojiChallenge,
    onEmojiSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select the emoji shown on your mobile device",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    challenge.options.forEach { emoji ->
                        EmojiOption(
                            emoji = emoji,
                            onClick = { onEmojiSelected(emoji) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiOption(
    emoji: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(80.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.2f),
                        blurRadius = 5f
                    )
                )
            )
        }
    }
}

// Preview
@Preview
@Composable
fun EmojiAuthDialogPreview() {
    val challenge = SettingsHost.EmojiChallenge("😀", listOf("😀", "😎", "🎮"))
    EmojiAuthDialog(challenge = challenge, onEmojiSelected = {})
}