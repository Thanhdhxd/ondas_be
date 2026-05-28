package com.example.ondas_be.application.exception;

public final class ErrorCodes {

    public static final String SUCCESS_OK = "success.ok";

    public static final String ERROR_INTERNAL = "error.internal";
    public static final String ERROR_BAD_REQUEST = "error.bad_request";
    public static final String ERROR_BAD_REQUEST_INVALID_PARAMETER = "error.bad_request.invalid_parameter";
    public static final String ERROR_BAD_REQUEST_INVALID_BODY = "error.bad_request.invalid_body";
    public static final String ERROR_BAD_REQUEST_MISSING_PARAM = "error.bad_request.missing_param";
    public static final String ERROR_BAD_REQUEST_MISSING_PART = "error.bad_request.missing_part";
    public static final String ERROR_BAD_REQUEST_TYPE_MISMATCH = "error.bad_request.type_mismatch";

    public static final String ERROR_UNAUTHORIZED = "error.unauthorized";
    public static final String ERROR_INVALID_CREDENTIALS = "error.unauthorized.invalid_credentials";
    public static final String ERROR_INVALID_TOKEN = "error.unauthorized.invalid_token";
    public static final String ERROR_ACCOUNT_LOCKED = "error.account_locked";

    public static final String ERROR_FORBIDDEN = "error.forbidden";
    public static final String ERROR_PLAYLIST_ACCESS_DENIED = "error.forbidden.playlist_access";

    public static final String ERROR_USER_NOT_FOUND = "error.not_found.user";
    public static final String ERROR_SONG_NOT_FOUND = "error.not_found.song";
    public static final String ERROR_ARTIST_NOT_FOUND = "error.not_found.artist";
    public static final String ERROR_ALBUM_NOT_FOUND = "error.not_found.album";
    public static final String ERROR_GENRE_NOT_FOUND = "error.not_found.genre";
    public static final String ERROR_PLAY_HISTORY_NOT_FOUND = "error.not_found.play_history";
    public static final String ERROR_PLAYLIST_NOT_FOUND = "error.not_found.playlist";
    public static final String ERROR_PLAYLIST_SONG_NOT_FOUND = "error.not_found.playlist_song";
    public static final String ERROR_SYSTEM_PLAYLIST_NOT_FOUND = "error.not_found.system_playlist";
    public static final String ERROR_SYSTEM_PLAYLIST_SONG_NOT_FOUND = "error.not_found.system_playlist_song";
    public static final String ERROR_FAVORITE_NOT_FOUND = "error.not_found.favorite";
    public static final String ERROR_LYRICS_NOT_FOUND = "error.not_found.lyrics";
    public static final String ERROR_TAG_NOT_FOUND = "error.not_found.tag";
    public static final String ERROR_NOT_FOUND = "error.not_found";

    public static final String ERROR_CONFLICT = "error.conflict";
    public static final String ERROR_EMAIL_EXISTS = "error.conflict.email_exists";
    public static final String ERROR_FAVORITE_EXISTS = "error.conflict.favorite_exists";
    public static final String ERROR_PLAYLIST_SONG_EXISTS = "error.conflict.playlist_song_exists";
    public static final String ERROR_SYSTEM_PLAYLIST_SONG_EXISTS = "error.conflict.system_playlist_song_exists";
    public static final String ERROR_LYRICS_EXISTS = "error.conflict.lyrics_exists";
    public static final String ERROR_SLUG_EXISTS = "error.conflict.slug_exists";

    public static final String ERROR_STORAGE_OPERATION_FAILED = "error.storage.operation_failed";

    public static final String ERROR_PLAYLIST_REORDER_INVALID = "error.playlist.reorder.invalid";
    public static final String ERROR_SYSTEM_PLAYLIST_REORDER_INVALID = "error.system_playlist.reorder.invalid";
    public static final String ERROR_LYRICS_SYNCED_INVALID = "error.lyrics.synced.invalid";

    public static final String ERROR_CURRENT_PASSWORD_INVALID = "error.auth.current_password_invalid";

    public static final String ERROR_SONG_AUDIO_REQUIRED = "error.song.audio_required";
    public static final String ERROR_SONG_AUDIO_SOURCE_NOT_FOUND = "error.song.audio_source_not_found";
    public static final String ERROR_TAG_IDS_REQUIRED = "error.tag.ids_required";
    public static final String ERROR_QUERY_REQUIRED = "error.query.required";
    public static final String ERROR_PLAYLIST_VISIBILITY_INVALID = "error.playlist.visibility_invalid";
    public static final String ERROR_PLAYLIST_NAME_REQUIRED = "error.playlist.name_required";
    public static final String ERROR_PLAYLIST_NAME_TOO_LONG = "error.playlist.name_too_long";
    public static final String ERROR_TAG_NAME_EXISTS = "error.tag.name_exists";
    public static final String ERROR_TAG_NAME_REQUIRED = "error.tag.name_required";
    public static final String ERROR_TAG_TYPE_REQUIRED = "error.tag.type_required";
    public static final String ERROR_TAG_TYPE_INVALID = "error.tag.type_invalid";

    private ErrorCodes() {
    }
}
