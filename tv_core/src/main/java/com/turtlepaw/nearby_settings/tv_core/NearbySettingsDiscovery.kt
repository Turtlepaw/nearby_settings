package com.turtlepaw.nearby_settings.tv_core

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices.TV_1080p
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogo
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import com.google.accompanist.drawablepainter.rememberDrawablePainter

/**
 * Use this UI component to help users discover your app supports Nearby Settings.
 *
 * This will provide the users a link and QR code to download the app on their mobile device.
 */
@Composable
fun NearbySettingsDiscovery(
    /**
     * Static size for the QR code.
     */
    qrCodeSize: Dp? = null,
    /**
     * The fraction of the row size to use for the QR code.
     *
     * This is only used if [qrCodeSize] is `null`.
     */
    dynamicQrCodeSizeFraction: Float = 0.08f,
    /**
     * Color to use on text and other elements. This color should be well-visible on the background.
     */
    onBackground: Color = MaterialTheme.colorScheme.onBackground,
    /**
     * Text to use for the large text (the download link)
     */
    largeTextStyle: TextStyle = MaterialTheme.typography.displaySmall,
    /**
     * Text to use for the small text (the description)
     */
    smallTextStyle: TextStyle = MaterialTheme.typography.bodyLarge
){
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    val data = QrData.Url("https://nearbysettings.pages.dev/users")
    val options = QrVectorOptions.Builder()
        .setPadding(0f)
        .setLogo(
            QrVectorLogo(
                drawable = ContextCompat
                    .getDrawable(LocalContext.current, R.drawable.nearby_settings_icon),
                size = .25f,
                padding = QrVectorLogoPadding.Natural(.2f),
                shape = QrVectorLogoShape
                    .Circle
            )
        )
        .setBackground(
            QrVectorBackground(
                color = QrVectorColor.Transparent,
            )
        )
        .setColors(
            QrVectorColors(
                dark = QrVectorColor
                    .Solid(
                        onBackground.toArgb()
                    ),
                ball = QrVectorColor.Solid(
                    onBackground.toArgb()
                ),
                frame = QrVectorColor.Solid(
                    onBackground.toArgb()
                ),
            )
        )
        .setShapes(
            QrVectorShapes(
                darkPixel = QrVectorPixelShape
                    .RoundCorners(.5f),
                ball = QrVectorBallShape
                    .RoundCorners(0.35f),
                frame = QrVectorFrameShape
                    .RoundCorners(0.35f),
            )
        )
        .build()

    val drawable = QrCodeDrawable(data, options)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp) // Increased padding for better TV viewing
            .onSizeChanged { rowSize = it },
        horizontalArrangement = Arrangement.Start, // Changed to Start to allow text to expand
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dynamicQrCodeSize = (rowSize.width * dynamicQrCodeSizeFraction).dp

        Image(
            painter = rememberDrawablePainter(drawable = drawable),
            contentDescription = "QR Code",
            modifier = Modifier.size(qrCodeSize ?: dynamicQrCodeSize)
        )

        Spacer(modifier = Modifier.width(32.dp)) // Increased spacing between QR and text

        Column(
            modifier = Modifier
                .weight(1f), // Added weight to allow text to take remaining space
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "nearbysettings.pages.dev",
                style = largeTextStyle,
                color = onBackground,
                maxLines = 1, // Ensure single line
                modifier = Modifier.fillMaxWidth() // Allow text to take full width
            )
            Spacer(modifier = Modifier.height(12.dp)) // Increased spacing between texts
            Text(
                "Open this link on your mobile device to download Nearby Settings",
                style = smallTextStyle,
                color = onBackground.copy(0.9f)
            )
        }
    }
}

/**
 * Dialog that displays the [NearbySettingsDiscovery] component.
 */
@Composable
fun NearbySettingsDiscoveryDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    BackHandler {
        onDismiss()
    }

    Box(
        modifier = modifier.fillMaxSize().zIndex(100f).background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                )
                .clip(shape = MaterialTheme.shapes.medium)
        ) {
            NearbySettingsDiscovery()
        }
    }
}
@Preview(
    device = TV_1080p,
    showBackground = true,
    widthDp = 1920, // Standard 1080p TV width
    heightDp = 1080  // Standard 1080p TV height
)
@Composable
private fun DiscoveryDialogPreview() {
    NearbySettingsDiscoveryDialog {}
}

@Preview(
    device = TV_1080p,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 1920, // Standard 1080p TV width
    heightDp = 1080  // Standard 1080p TV height
)
@Composable
private fun DiscoveryDialogDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        NearbySettingsDiscoveryDialog {}
    }
}