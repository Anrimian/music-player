package com.github.anrimian.musicplayer.lite.ui

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.lite.R
import com.github.anrimian.musicplayer.ui.about.AboutAppFragment
import com.github.anrimian.musicplayer.ui.about.AboutTextBinder
import com.github.anrimian.musicplayer.ui.utils.linkify
import com.github.anrimian.musicplayer.ui.utils.views.text_view.CustomURLSpan.Companion.onLinkClick

class AboutTextBinderImpl : AboutTextBinder {

    override fun bind(fragment: AboutAppFragment, textView: TextView) {
        val context = fragment.requireContext()
        val aboutText = context.getString(
            R.string.about_app_text,
            context.linkify("", R.string.main_version, R.string.main_app_play_store_link),
            context.linkify("mailto:", R.string.about_app_text_write, R.string.feedback_email),
            context.linkify("", R.string.privacy_policy, R.string.privacy_policy_link),
            context.linkify("", R.string.about_app_text_crowdin, R.string.crowdin_link),
            context.linkify("", R.string.about_app_text_here_link, R.string.source_code_link),
            context.linkify("", R.string.patchnotes, R.string.patch_notes_link),
            context.linkify(AppConstants.Links.IN_APP_LINK_PREFIX, R.string.about_app_text_here_link, AppConstants.Links.PLAYER_SETTINGS)
        )
        textView.text = HtmlCompat.fromHtml(aboutText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.onLinkClick(
            shouldIntercept = { url -> url.startsWith(AppConstants.Links.IN_APP_LINK_PREFIX) },
            onInterceptedClick = { url -> when(url.substringAfterLast(AppConstants.Links.IN_APP_LINK_PREFIX)) {
                AppConstants.Links.PLAYER_SETTINGS -> fragment.openPlayerSettings()
            } }
        )
    }
}