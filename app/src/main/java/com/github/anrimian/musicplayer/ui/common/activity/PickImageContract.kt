package com.github.anrimian.musicplayer.ui.common.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class PickImageContract: ActivityResultContract<Unit?, Uri?>() {

    override fun createIntent(context: Context, input: Unit?): Intent {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        return Intent.createChooser(intent, "Select Picture")
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = intent?.data

}