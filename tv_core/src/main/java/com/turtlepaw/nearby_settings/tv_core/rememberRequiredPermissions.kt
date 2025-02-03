package com.turtlepaw.nearby_settings.tv_core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNearbyPermissions(
    dialog: Boolean = true,
    launchPermissionsOnStart: Boolean = true,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
): NearbyPermissionsState {
    val permissions = rememberMultiplePermissionsState(
        permissions = getRequiredPermissions(),
        onPermissionsResult = onPermissionsResult
    )

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(launchPermissionsOnStart) {
        if (launchPermissionsOnStart) {
            if (dialog) {
                showDialog = true
            } else {
                permissions.launchMultiplePermissionRequest()
            }
        }
    }

    if (showDialog) {
        PermissionRequestDialog(
            onDismissRequest = {
                showDialog = false
            },
            onConfirmation = {
                showDialog = false
                permissions.launchMultiplePermissionRequest()
            }
        )
    }

    return remember(permissions) {
        NearbyPermissionsState(
            state = permissions,
            showDialog = { showDialog = true }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
class NearbyPermissionsState(
    val state: MultiplePermissionsState,
    private val showDialog: () -> Unit
): MultiplePermissionsState by state {
    override fun launchMultiplePermissionRequest() {
        showDialog()
    }
}