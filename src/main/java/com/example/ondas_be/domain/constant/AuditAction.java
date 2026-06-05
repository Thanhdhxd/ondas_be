package com.example.ondas_be.domain.constant;

public final class AuditAction {

    private AuditAction() {
        // Prevent instantiation
    }

    public static final String BAN_USER = "BAN_USER";
    public static final String UNBAN_USER = "UNBAN_USER";

    public static final String CREATE_SONG = "CREATE_SONG";
    public static final String UPDATE_SONG = "UPDATE_SONG";
    public static final String DELETE_SONG = "DELETE_SONG";

    public static final String ADD_SONG_TAGS = "ADD_SONG_TAGS";
    public static final String REMOVE_SONG_TAGS = "REMOVE_SONG_TAGS";
    public static final String REPLACE_SONG_TAGS = "REPLACE_SONG_TAGS";

    public static final String CREATE_ARTIST = "CREATE_ARTIST";
    public static final String UPDATE_ARTIST = "UPDATE_ARTIST";
    public static final String DELETE_ARTIST = "DELETE_ARTIST";

    public static final String CREATE_ALBUM = "CREATE_ALBUM";
    public static final String UPDATE_ALBUM = "UPDATE_ALBUM";
    public static final String DELETE_ALBUM = "DELETE_ALBUM";

    public static final String CREATE_SYSTEM_PLAYLIST = "CREATE_SYSTEM_PLAYLIST";
    public static final String UPDATE_SYSTEM_PLAYLIST = "UPDATE_SYSTEM_PLAYLIST";
    public static final String DELETE_SYSTEM_PLAYLIST = "DELETE_SYSTEM_PLAYLIST";
    public static final String ADD_SONG_TO_SYSTEM_PLAYLIST = "ADD_SONG_TO_SYSTEM_PLAYLIST";
    public static final String REMOVE_SONG_FROM_SYSTEM_PLAYLIST = "REMOVE_SONG_FROM_SYSTEM_PLAYLIST";
    public static final String REORDER_SYSTEM_PLAYLIST_SONGS = "REORDER_SYSTEM_PLAYLIST_SONGS";
}
