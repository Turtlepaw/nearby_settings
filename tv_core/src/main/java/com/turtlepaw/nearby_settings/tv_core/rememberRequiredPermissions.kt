package com.turtlepaw.nearby_settings.tv_core

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberRequiredPermissions(onPermissionsResult: (Map<String, Boolean>) -> Unit): MultiplePermissionsState {
    return rememberMultiplePermissionsState(
        permissions = getRequiredPermissions(),
        onPermissionsResult = onPermissionsResult
    )
}