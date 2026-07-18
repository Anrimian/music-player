package com.github.anrimian.musicplayer.ui.common.models.menu

import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.menu.AppMenu
import com.github.anrimian.musicplayer.ui.utils.compose.UiText

object AppMenuDefinitions {

    fun getMenuItems(menu: AppMenu): List<AppMenuItem> {
        return when(menu) {
            AppMenu.PLAYLIST -> PlaylistMenuItems
            AppMenu.PLAYLIST_ENTRY -> PlaylistEntryMenuItems
        }
    }

    val PlaylistMenuItems = listOf(
        AppMenuItem(
            id = MenuIds.PLAY,
            title = UiText.StringResource(R.string.play_action),
            iconRes = R.drawable.ic_play,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.PLAY_NEXT,
            title = UiText.StringResource(R.string.play_next),
            iconRes = R.drawable.ic_play_next,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.ADD_TO_QUEUE,
            title = UiText.StringResource(R.string.add_to_play_queue),
            iconRes = R.drawable.ic_add_to_queue,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.ADD_TO_PLAYLIST,
            title = UiText.StringResource(R.string.add_to_playlist),
            iconRes = R.drawable.ic_playlist_plus,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.EDIT_NAME,
            title = UiText.StringResource(R.string.edit_name),
            iconRes = R.drawable.ic_playlist_edit,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.EXPORT_PLAYLIST,
            title = UiText.StringResource(R.string.export_playlist),
            iconRes = R.drawable.ic_save,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.SHARE,
            title = UiText.StringResource(R.string.share),
            iconRes = R.drawable.ic_share_variant,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.DELETE,
            title = UiText.StringResource(R.string.delete_playlist),
            iconRes = R.drawable.ic_delete_outline,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.MENU_CONFIG,
            title = UiText.StringResource(R.string.configure_menu),
            iconRes = R.drawable.ic_settings,
            groupId = 1
        )
    )

    val PlaylistEntryMenuItems = listOf(
        AppMenuItem(
            id = MenuIds.PLAY,
            title = UiText.StringResource(R.string.play_action),
            iconRes = R.drawable.ic_play,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.PLAY_NEXT,
            title = UiText.StringResource(R.string.play_next),
            iconRes = R.drawable.ic_play_next,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.ADD_TO_QUEUE,
            title = UiText.StringResource(R.string.add_to_play_queue),
            iconRes = R.drawable.ic_add_to_queue,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.ADD_TO_PLAYLIST,
            title = UiText.StringResource(R.string.add_to_playlist),
            iconRes = R.drawable.ic_playlist_plus,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.EDIT_TAGS,
            title = UiText.StringResource(R.string.edit_tags),
            iconRes = R.drawable.ic_circle_edit_outline,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.SHOW_IN_FOLDERS,
            title = UiText.StringResource(R.string.show_in_folders),
            iconRes = R.drawable.ic_show_in_folders,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.SHARE,
            title = UiText.StringResource(R.string.share),
            iconRes = R.drawable.ic_share_variant,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.DELETE_FROM_PLAYLIST,
            title = UiText.StringResource(R.string.delete_from_play_list),
            iconRes = R.drawable.ic_playlist_remove,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.DELETE,
            title = UiText.StringResource(R.string.delete),
            iconRes = R.drawable.ic_delete_outline,
            groupId = 0
        ),
        AppMenuItem(
            id = MenuIds.MENU_CONFIG,
            title = UiText.StringResource(R.string.configure_menu),
            iconRes = R.drawable.ic_settings,
            groupId = 1
        )
    )

}