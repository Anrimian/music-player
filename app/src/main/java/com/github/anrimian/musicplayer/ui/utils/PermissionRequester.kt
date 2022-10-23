package com.github.anrimian.musicplayer.ui.utils

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class PermissionRequester {

    private val permissionLauncher: ActivityResultLauncher<String>

    constructor(fragment: Fragment, callback: ActivityResultCallback<Boolean>) {
        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            callback
        )

    }

    constructor(activity: AppCompatActivity, callback: ActivityResultCallback<Boolean>) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            callback
        )
    }

    fun request(permission: String) {
        permissionLauncher.launch(permission)
    }

}