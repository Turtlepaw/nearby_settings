package com.turtlepaw.nearby_settings.tv_core

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.TV_1080p
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ButtonShape
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@Composable
fun PermissionRequestDialog(onConfirmation: () -> Unit, onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(30.dp)
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.network_manage),
                        contentDescription = "Network Manage",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.nearby_settings_icon),
                        contentDescription = "Cloud",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary.copy(0.2f)
                    )
                }


                Text(
                    text = "Nearby Settings requires permissions",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 15.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Nearby Settings requires permissions to allow nearby mobile devices to edit app settings.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                PermissionDescription(
                    icon = {
                        Icon(
                            painterResource(R.drawable.location_on),
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    title = "Location",
                    supportingText = "Used to discover nearby devices",
                    placement = Placement.Top
                )

                PermissionDescription(
                    icon = {
                        Icon(
                            painterResource(R.drawable.nearby),
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    title = "Nearby Devices",
                    supportingText = "Used to advertise and connect to nearby devices",
                    placement = Placement.Bottom
                )

                Spacer(modifier = Modifier.height(2.dp))

                PermissionActionButton(onConfirmation, "Continue")
                PermissionActionButton(onDismissRequest, "Don't Allow")
            }
        }
    }
}

private enum class Placement {
    Top,
    Bottom
}

@Composable
private fun PermissionActionButton(onClick: () -> Unit, text: String) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = ButtonDefaults.shape(
            shape = MaterialTheme.shapes.large,
        )
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalContentColor.current,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        )
    }
}

@Composable
private fun PermissionDescription(
    icon: @Composable () -> Unit,
    title: String,
    supportingText: String,
    placement: Placement
) {
    val bottomCorner = if (placement == Placement.Top) {
        MaterialTheme.shapes.small.bottomStart
    } else {
        MaterialTheme.shapes.large.bottomStart
    }

    val topCorner = if (placement == Placement.Top) {
        MaterialTheme.shapes.large.topStart
    } else {
        MaterialTheme.shapes.small.topStart
    }

    Box(
        modifier = Modifier.fillMaxWidth().border(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium.copy(
                bottomEnd = bottomCorner,
                bottomStart = bottomCorner,
                topEnd = topCorner,
                topStart = topCorner
            ),
            width = 2.dp
        ).padding(20.dp),
    ) {
        Row {
            Box(
                content = { icon() },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(0.2f),
                        shape = CircleShape
                    )
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    title,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    supportingText,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
private fun PermissionsRequestDefaultPreview() {
    PermissionRequestDialog({}, {})
}

@Preview(
    device = TV_1080p,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 1920, // Standard 1080p TV width
    heightDp = 1080  // Standard 1080p TV height
)
@Composable
private fun PermissionsRequestDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        PermissionRequestDialog({}, {})
    }
}