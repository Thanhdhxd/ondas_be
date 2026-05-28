# Error Codes (Backend -> FE)

Tat ca API tra ve `ApiResponse.message` la **code** (khong phai text). FE map code sang vi/en.

## Response format

**Success:**
```json
{
  "success": true,
  "message": "success.ok",
  "data": { }
}
```

**Error:**
```json
{
  "success": false,
  "message": "error.not_found.song",
  "data": null
}
```

## Quy tac format cho validation

- **MethodArgumentNotValidException**: message la danh sach `field: validation.*` (noi bang dau phay).
- **Missing param/part**: `param: validation.required`.
- **Type mismatch**: `param: validation.type_mismatch` (hoac `error.bad_request.type_mismatch` neu khong co ten param).
- **Body khong doc duoc**: `error.bad_request.invalid_body`.
- **Invalid format**: `field: validation.invalid_format`.

## Danh sach code

### Success
- success.ok

### Auth / Permission
- error.unauthorized
- error.unauthorized.invalid_credentials
- error.unauthorized.invalid_token
- error.account_locked
- error.auth.current_password_invalid
- error.forbidden
- error.forbidden.playlist_access

### Not found
- error.not_found
- error.not_found.user
- error.not_found.song
- error.not_found.artist
- error.not_found.album
- error.not_found.genre
- error.not_found.play_history
- error.not_found.playlist
- error.not_found.playlist_song
- error.not_found.system_playlist
- error.not_found.system_playlist_song
- error.not_found.favorite
- error.not_found.lyrics
- error.not_found.tag

### Conflict
- error.conflict
- error.conflict.email_exists
- error.conflict.favorite_exists
- error.conflict.playlist_song_exists
- error.conflict.system_playlist_song_exists
- error.conflict.lyrics_exists
- error.conflict.slug_exists

### Bad request / Business rule
- error.bad_request
- error.bad_request.invalid_body
- error.bad_request.type_mismatch
- error.query.required
- error.song.audio_required
- error.song.audio_source_not_found
- error.tag.ids_required
- error.playlist.visibility_invalid
- error.playlist.name_required
- error.playlist.name_too_long
- error.tag.name_exists
- error.tag.name_required
- error.tag.type_required
- error.tag.type_invalid
- error.playlist.reorder.invalid
- error.system_playlist.reorder.invalid
- error.lyrics.synced.invalid

### System / Storage
- error.storage.operation_failed
- error.internal

### Validation codes
- validation.not_blank
- validation.not_null
- validation.not_empty
- validation.size.min
- validation.size.max
- validation.size.range
- validation.email
- validation.pattern
- validation.positive_or_zero
- validation.invalid_format
- validation.type_mismatch
- validation.required
