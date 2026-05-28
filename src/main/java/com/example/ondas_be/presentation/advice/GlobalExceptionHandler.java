package com.example.ondas_be.presentation.advice;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.exception.AccountLockedException;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.DuplicateSlugException;
import com.example.ondas_be.application.exception.EmailAlreadyExistsException;
import com.example.ondas_be.application.exception.FavoriteAlreadyExistsException;
import com.example.ondas_be.application.exception.FavoriteNotFoundException;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.exception.InvalidCredentialsException;
import com.example.ondas_be.application.exception.InvalidCurrentPasswordException;
import com.example.ondas_be.application.exception.InvalidTokenException;
import com.example.ondas_be.application.exception.LyricsAlreadyExistsException;
import com.example.ondas_be.application.exception.LyricsNotFoundException;
import com.example.ondas_be.application.exception.SyncedLyricsValidationException;
import com.example.ondas_be.application.exception.ErrorCodes;
import com.example.ondas_be.application.exception.PlaylistAccessDeniedException;
import com.example.ondas_be.application.exception.PlaylistNotFoundException;
import com.example.ondas_be.application.exception.PlaylistReorderInvalidException;
import com.example.ondas_be.application.exception.PlaylistSongAlreadyExistsException;
import com.example.ondas_be.application.exception.PlaylistSongNotFoundException;
import com.example.ondas_be.application.exception.PlayHistoryNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.exception.SystemPlaylistNotFoundException;
import com.example.ondas_be.application.exception.SystemPlaylistReorderInvalidException;
import com.example.ondas_be.application.exception.SystemPlaylistSongAlreadyExistsException;
import com.example.ondas_be.application.exception.SystemPlaylistSongNotFoundException;
import com.example.ondas_be.application.exception.TagNotFoundException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.util.Objects;
import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ErrorCodes.ERROR_EMAIL_EXISTS));
    }

    @ExceptionHandler(LyricsAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleLyricsAlreadyExists(LyricsAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ErrorCodes.ERROR_LYRICS_EXISTS));
    }

    @ExceptionHandler(DuplicateSlugException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateSlug(DuplicateSlugException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ErrorCodes.ERROR_SLUG_EXISTS));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(resolveDataIntegrityCode(ex)));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCodes.ERROR_INVALID_CREDENTIALS));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountLocked(AccountLockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(ApiResponse.error(ErrorCodes.ERROR_ACCOUNT_LOCKED));
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCurrentPassword(InvalidCurrentPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.ERROR_CURRENT_PASSWORD_INVALID));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCodes.ERROR_INVALID_TOKEN));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ErrorCodes.ERROR_USER_NOT_FOUND));
    }

    @ExceptionHandler({
            SongNotFoundException.class,
            ArtistNotFoundException.class,
            AlbumNotFoundException.class,
            GenreNotFoundException.class,
            PlayHistoryNotFoundException.class,
            PlaylistNotFoundException.class,
            PlaylistSongNotFoundException.class,
            SystemPlaylistNotFoundException.class,
            SystemPlaylistSongNotFoundException.class,
            FavoriteNotFoundException.class,
            LyricsNotFoundException.class,
            TagNotFoundException.class,
            SystemPlaylistNotFoundException.class,
            SystemPlaylistSongNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(resolveNotFoundCode(ex)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ErrorCodes.ERROR_FORBIDDEN));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCodes.ERROR_UNAUTHORIZED));
    }

    @ExceptionHandler(PlaylistSongAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlaylistConflict(PlaylistSongAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCodes.ERROR_PLAYLIST_SONG_EXISTS));
    }

    @ExceptionHandler(SystemPlaylistSongAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemPlaylistConflict(SystemPlaylistSongAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCodes.ERROR_SYSTEM_PLAYLIST_SONG_EXISTS));
    }

    @ExceptionHandler(FavoriteAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleFavoriteAlreadyExists(FavoriteAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ErrorCodes.ERROR_FAVORITE_EXISTS));
    }

    @ExceptionHandler(PlaylistAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlaylistAccessDenied(PlaylistAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCodes.ERROR_PLAYLIST_ACCESS_DENIED));
    }

    @ExceptionHandler(PlaylistReorderInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlaylistReorderInvalid(PlaylistReorderInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.ERROR_PLAYLIST_REORDER_INVALID));
    }

    @ExceptionHandler(SystemPlaylistReorderInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemPlaylistReorderInvalid(SystemPlaylistReorderInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.ERROR_SYSTEM_PLAYLIST_REORDER_INVALID));
    }

    @ExceptionHandler(SyncedLyricsValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSyncedLyricsValidation(SyncedLyricsValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.ERROR_LYRICS_SYNCED_INVALID));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildValidationMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        String code = isCode(ex.getMessage()) ? ex.getMessage() : ErrorCodes.ERROR_BAD_REQUEST;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(code));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String code = name == null || name.isBlank()
                ? ErrorCodes.ERROR_BAD_REQUEST_TYPE_MISMATCH
                : name + ": validation.type_mismatch";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(code));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(resolveMessageNotReadableCode(ex)));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPart(MissingServletRequestPartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getRequestPartName() + ": validation.required"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getParameterName() + ": validation.required"));
    }

    @ExceptionHandler(StorageOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorageError(StorageOperationException ex) {
        log.error("Storage operation failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.ERROR_STORAGE_OPERATION_FAILED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.ERROR_INTERNAL));
    }

    private String buildValidationMessage(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String resolveDataIntegrityCode(DataIntegrityViolationException ex) {
        String rawMessage = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        if (rawMessage == null) {
            return ErrorCodes.ERROR_CONFLICT;
        }

        String normalized = rawMessage.toLowerCase(Locale.ROOT);
        if (normalized.contains("favorites")) {
            return ErrorCodes.ERROR_FAVORITE_EXISTS;
        }
        if (normalized.contains("users") && normalized.contains("email")) {
            return ErrorCodes.ERROR_EMAIL_EXISTS;
        }

        return ErrorCodes.ERROR_CONFLICT;
    }

    private String resolveMessageNotReadableCode(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException formatEx && formatEx.getPath() != null) {
            String field = formatEx.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (field != null && !field.isBlank()) {
                return field + ": validation.invalid_format";
            }
        }
        return ErrorCodes.ERROR_BAD_REQUEST_INVALID_BODY;
    }

    private String resolveNotFoundCode(RuntimeException ex) {
        if (isCode(ex.getMessage())) {
            return ex.getMessage();
        }
        if (ex instanceof SongNotFoundException) {
            return ErrorCodes.ERROR_SONG_NOT_FOUND;
        }
        if (ex instanceof ArtistNotFoundException) {
            return ErrorCodes.ERROR_ARTIST_NOT_FOUND;
        }
        if (ex instanceof AlbumNotFoundException) {
            return ErrorCodes.ERROR_ALBUM_NOT_FOUND;
        }
        if (ex instanceof GenreNotFoundException) {
            return ErrorCodes.ERROR_GENRE_NOT_FOUND;
        }
        if (ex instanceof PlayHistoryNotFoundException) {
            return ErrorCodes.ERROR_PLAY_HISTORY_NOT_FOUND;
        }
        if (ex instanceof PlaylistNotFoundException) {
            return ErrorCodes.ERROR_PLAYLIST_NOT_FOUND;
        }
        if (ex instanceof PlaylistSongNotFoundException) {
            return ErrorCodes.ERROR_PLAYLIST_SONG_NOT_FOUND;
        }
        if (ex instanceof SystemPlaylistNotFoundException) {
            return ErrorCodes.ERROR_SYSTEM_PLAYLIST_NOT_FOUND;
        }
        if (ex instanceof SystemPlaylistSongNotFoundException) {
            return ErrorCodes.ERROR_SYSTEM_PLAYLIST_SONG_NOT_FOUND;
        }
        if (ex instanceof FavoriteNotFoundException) {
            return ErrorCodes.ERROR_FAVORITE_NOT_FOUND;
        }
        if (ex instanceof LyricsNotFoundException) {
            return ErrorCodes.ERROR_LYRICS_NOT_FOUND;
        }
        if (ex instanceof TagNotFoundException) {
            return ErrorCodes.ERROR_TAG_NOT_FOUND;
        }
        return ErrorCodes.ERROR_NOT_FOUND;
    }

    private boolean isCode(String message) {
        return message != null && (message.startsWith("error.") || message.startsWith("validation.")
                || message.startsWith("success."));
    }
}