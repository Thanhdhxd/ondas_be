package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.CreateLyricsRequest;
import com.example.ondas_be.application.dto.request.PatchLyricsRequest;
import com.example.ondas_be.application.dto.request.SyncedLyricsLineDto;
import com.example.ondas_be.application.dto.response.LyricsResponse;
import com.example.ondas_be.application.dto.response.SyncedLyricsLineResponse;
import com.example.ondas_be.application.exception.ErrorCodes;
import com.example.ondas_be.application.exception.LyricsAlreadyExistsException;
import com.example.ondas_be.application.exception.LyricsNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.SyncedLyricsValidationException;
import com.example.ondas_be.application.mapper.SyncedLyricsMapper;
import com.example.ondas_be.application.service.port.LyricsServicePort;
import com.example.ondas_be.domain.entity.Lyrics;
import com.example.ondas_be.domain.entity.SyncedLyricsLine;
import com.example.ondas_be.domain.repoport.LyricsRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.SyncedLyricsLineRepoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LyricsService implements LyricsServicePort {

    private final LyricsRepoPort lyricsRepoPort;
    private final SyncedLyricsLineRepoPort syncedLyricsLineRepoPort;
    private final SongRepoPort songRepoPort;
    private final SyncedLyricsMapper syncedLyricsMapper;

    @Override
    @Transactional(readOnly = true)
    public LyricsResponse getLyricsBySongId(UUID songId) {
        Lyrics lyrics = lyricsRepoPort.findBySongId(songId)
                .orElseThrow(() -> new LyricsNotFoundException(ErrorCodes.ERROR_LYRICS_NOT_FOUND));

        List<SyncedLyricsLineResponse> syncedLines = null;
        if (lyrics.isHasSynced()) {
            List<SyncedLyricsLine> lines = syncedLyricsLineRepoPort.findByLyricsId(lyrics.getId());
            syncedLines = syncedLyricsMapper.toResponseList(lines);
        }

        return buildResponse(lyrics, syncedLines);
    }

    @Override
    @Transactional
    public LyricsResponse createLyrics(UUID songId, CreateLyricsRequest request) {
        ensureSongExists(songId);

        // Lyrics là quan hệ 1-1 với song → không cho phép tạo trùng
        if (lyricsRepoPort.findBySongId(songId).isPresent()) {
            throw new LyricsAlreadyExistsException(ErrorCodes.ERROR_LYRICS_EXISTS);
        }

        // Tạo lyrics mới
        Lyrics lyrics = lyricsRepoPort.save(Lyrics.builder()
                .songId(songId)
                .plainText(request.getPlainText())
                .hasSynced(request.getSyncedLines() != null && !request.getSyncedLines().isEmpty())
                .language(request.getLanguage())
                .build());

        // Nếu có synced lines đi kèm → validate & lưu
        List<SyncedLyricsLineResponse> syncedLines = null;
        if (request.getSyncedLines() != null && !request.getSyncedLines().isEmpty()) {
            validateSyncedLines(request.getSyncedLines());
            List<SyncedLyricsLine> lines = toDomainLines(lyrics.getId(), request.getSyncedLines());
            syncedLyricsLineRepoPort.replaceLines(lyrics.getId(), lines);
            syncedLines = syncedLyricsMapper.toResponseList(lines);
        }

        return buildResponse(lyrics, syncedLines);
    }

    @Override
    @Transactional
    public LyricsResponse patchLyrics(UUID songId, PatchLyricsRequest request) {
        Lyrics lyrics = lyricsRepoPort.findBySongId(songId)
                .orElseThrow(() -> new LyricsNotFoundException(ErrorCodes.ERROR_LYRICS_NOT_FOUND));

        boolean dirty = false;

        // 1. Cập nhật language nếu có
        if (request.getLanguage() != null) {
            lyricsRepoPort.updateStaticLyrics(lyrics.getId(), null, request.getLanguage());
            dirty = true;
        }

        // 2. Cập nhật plainText nếu có (có thể là null để xoá plain text)
        if (request.getPlainText() != null) {
            lyricsRepoPort.updateStaticLyrics(lyrics.getId(), request.getPlainText(), null);
            dirty = true;
        }

        // 3. Xử lý syncedLines
        //    - null          → không đụng đến synced
        //    - empty list    → xoá synced lines
        //    - non-empty     → thay thế toàn bộ synced lines
        List<SyncedLyricsLineResponse> syncedLines = null;
        if (request.getSyncedLines() != null) {
            if (request.getSyncedLines().isEmpty()) {
                // Xoá synced
                syncedLyricsLineRepoPort.deleteByLyricsId(lyrics.getId());
                lyricsRepoPort.updateHasSynced(lyrics.getId(), false);
                syncedLines = Collections.emptyList();
            } else {
                validateSyncedLines(request.getSyncedLines());
                List<SyncedLyricsLine> lines = toDomainLines(lyrics.getId(), request.getSyncedLines());
                syncedLyricsLineRepoPort.replaceLines(lyrics.getId(), lines);
                lyricsRepoPort.updateHasSynced(lyrics.getId(), true);
                syncedLines = syncedLyricsMapper.toResponseList(lines);
            }
            dirty = true;
        } else {
            // Không thay đổi synced — lấy data hiện tại để trả về response
            if (lyrics.isHasSynced()) {
                syncedLines = syncedLyricsMapper.toResponseList(
                        syncedLyricsLineRepoPort.findByLyricsId(lyrics.getId()));
            }
        }

        // Refresh entity chỉ khi có thay đổi
        Lyrics updated = dirty
                ? lyricsRepoPort.findById(lyrics.getId()).orElseThrow()
                : lyrics;

        return buildResponse(updated, syncedLines);
    }

    @Override
    @Transactional
    public void deleteLyrics(UUID songId) {
        Lyrics lyrics = lyricsRepoPort.findBySongId(songId)
                .orElseThrow(() -> new LyricsNotFoundException(ErrorCodes.ERROR_LYRICS_NOT_FOUND));

        lyricsRepoPort.deleteById(lyrics.getId());
        // synced_lyrics_lines sẽ tự xoá nhờ ON DELETE CASCADE
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private void ensureSongExists(UUID songId) {
        if (!songRepoPort.existsById(songId)) {
            throw new SongNotFoundException(ErrorCodes.ERROR_SONG_NOT_FOUND);
        }
    }

    private List<SyncedLyricsLine> toDomainLines(UUID lyricsId, List<SyncedLyricsLineDto> dtos) {
        return dtos.stream()
                .map(dto -> new SyncedLyricsLine(null, lyricsId,
                        dto.getStartMs(), dto.getEndMs(),
                        dto.getLineText().trim(), dto.getLineIndex()))
                .toList();
    }

    private LyricsResponse buildResponse(Lyrics lyrics, List<SyncedLyricsLineResponse> syncedLines) {
        return LyricsResponse.builder()
                .id(lyrics.getId())
                .songId(lyrics.getSongId())
                .plainText(lyrics.getPlainText())
                .hasSynced(lyrics.isHasSynced())
                .language(lyrics.getLanguage())
                .createdAt(lyrics.getCreatedAt())
                .updatedAt(lyrics.getUpdatedAt())
                .syncedLines(syncedLines)
                .build();
    }

    /**
     * Validate danh sách synced lyric lines trước khi lưu:
     * - lineIndex phải tuần tự từ 0, không trùng
     * - endMs > startMs nếu endMs được cung cấp
     * - Các dòng không chồng lặp thời gian
     */
    private void validateSyncedLines(List<SyncedLyricsLineDto> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new SyncedLyricsValidationException(ErrorCodes.ERROR_LYRICS_SYNCED_INVALID);
        }

        // Sắp xếp theo lineIndex để validate tuần tự
        List<SyncedLyricsLineDto> sorted = new ArrayList<>(lines);
        sorted.sort((a, b) -> Short.compare(a.getLineIndex(), b.getLineIndex()));

        // Kiểm tra lineIndex tuần tự từ 0
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getLineIndex() != i) {
                throw new SyncedLyricsValidationException(ErrorCodes.ERROR_LYRICS_SYNCED_INVALID);
            }
        }

        // Kiểm tra từng dòng
        for (SyncedLyricsLineDto line : sorted) {
            if (line.getEndMs() != null && line.getEndMs() <= line.getStartMs()) {
                throw new SyncedLyricsValidationException(ErrorCodes.ERROR_LYRICS_SYNCED_INVALID);
            }
        }

        // Kiểm tra không chồng lặp thời gian giữa các dòng liền kề
        for (int i = 0; i < sorted.size() - 1; i++) {
            SyncedLyricsLineDto current = sorted.get(i);
            SyncedLyricsLineDto next = sorted.get(i + 1);

            // Nếu current có endMs, kiểm tra không vượt quá startMs của dòng tiếp theo
            if (current.getEndMs() != null && current.getEndMs() > next.getStartMs()) {
                throw new SyncedLyricsValidationException(ErrorCodes.ERROR_LYRICS_SYNCED_INVALID);
            }

            // Đảm bảo startMs tăng dần (không giảm)
            if (current.getStartMs() > next.getStartMs()) {
                throw new SyncedLyricsValidationException(ErrorCodes.ERROR_LYRICS_SYNCED_INVALID);
            }
        }
    }
}
