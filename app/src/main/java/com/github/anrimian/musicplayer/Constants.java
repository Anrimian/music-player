package com.github.anrimian.musicplayer;

public interface Constants {

    String PLAYLIST_MIME_TYPE = "audio/x-mpegurl";
    long EDIT_DIALOG_DELAY_MILLIS = 600;

    interface Actions {
        int PLAY = 1;
        int PAUSE = 2;
        int SKIP_TO_NEXT = 3;
        int SKIP_TO_PREVIOUS = 4;
        int CHANGE_SHUFFLE_NODE = 5;
        int CHANGE_REPEAT_MODE = 6;
        int REWIND = 7;
        int FAST_FORWARD = 8;
    }

    interface Arguments {
        String ORDER_ARG = "order_arg";
        String ORDERS_ARG = "orders_arg";
        String PLAY_LIST_ID_ARG = "play_list_id_arg";
        String ID_ARG = "id_arg";//we can replace other `id_arg`
        String STATUS_BAR_COLOR_ATTR_ARG = "status_bar_color_attr";
        String OPEN_PLAYER_PANEL_ARG = "open_player_panel_arg";
        String COMPOSITION_ID_ARG = "composition_id_arg";
        String ALBUM_ID_ARG = "album_id_arg";
        String TITLE_ARG = "title_arg";
        String POSITIVE_BUTTON_ARG = "positive_button_arg";
        String NEGATIVE_BUTTON_ARG = "negative_button_arg";
        String EDIT_TEXT_HINT = "edit_text_hint";
        String EDIT_TEXT_VALUE = "edit_text_value";
        String CAN_BE_EMPTY_ARG = "can_be_empty_arg";
        String COMPLETE_ON_ENTER_ARG = "complete_on_enter_arg";
        String EXTRA_DATA_ARG = "extra_data_arg";
        String HINTS_ARG = "hints_arg";
        String FILE_NAME_SETTING_ARG = "file_name_setting_arg";
        String HIGHLIGHT_COMPOSITION_ID = "highlight_composition_id";
        String IDS_ARG = "ids_arg";
        String NAME_ARG = "name_arg";
        String LAUNCH_PREPARE_ARG = "launch_prepare_arg";
        String INPUT_TYPE_ARG = "input_type_arg";
        String DIGITS_ARG = "digits_arg";
        String CLOSE_MULTISELECT_ARG = "close_multiselect_arg";
        String PLAYLIST_IMPORT_ARG = "playlist_import_arg";
    }

    interface Tags {
        String ORDER_TAG = "order_tag";
        String SELECT_PLAYLIST_TAG = "select_playlist_tag";
        String SELECT_PLAYLIST_FOR_FOLDER_TAG = "select_playlist_for_folder_tag";
        String CREATE_PLAYLIST_TAG = "create_playlist_tag";
        String AUTHOR_TAG = "author_tag";
        String NAME_TAG = "name_tag";
        String TITLE_TAG = "title_tag";
        String FILE_NAME_TAG = "file_name_tag";
        String ALBUM_TAG = "album_tag";
        String ALBUM_ARTIST_TAG = "album_artist_tag";
        String TRACK_NUMBER_TAG = "track_number_tag";
        String DISC_NUMBER_TAG = "disc_number_tag";
        String COMMENT_TAG = "comment_tag";
        String ADD_GENRE_TAG = "add_genre_tag";
        String EDIT_GENRE_TAG = "edit_genre_tag";
        String NEW_FOLDER_NAME_TAG = "new_folder_name_tag";
        String MESSAGE_ARG = "message_arg";
        String MESSAGE_RES_ARG = "message_res_arg";
        String PROGRESS_DIALOG_TAG = "progress_dialog_arg";
        String EDIT_COVER_TAG = "edit_cover_tag";
        String ENABLED_MEDIA_PLAYERS = "enabled_media_players";
        String SPEED_SELECTOR_TAG = "speed_selector_tag";
    }

    interface Animation {
        long TOOLBAR_ARROW_ANIMATION_TIME = 200;
    }

    interface RemoteViewPlayerState {
        int PLAY = 1;
        int PLAY_LOADING = 2;
        int PAUSE = 3;
    }
}
