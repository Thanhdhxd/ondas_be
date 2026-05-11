package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.SyncedLyricsLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "synced_lyrics_lines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncedLyricsLineModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lyrics_id", nullable = false)
    private UUID lyricsId;

    @Column(name = "start_ms", nullable = false)
    private Integer startMs;

    @Column(name = "end_ms")
    private Integer endMs;

    @Column(name = "line_text", nullable = false, columnDefinition = "TEXT")
    private String lineText;

    @Column(name = "line_index", nullable = false)
    private Short lineIndex;

    // -------------------------------------------------------
    // Converters
    // -------------------------------------------------------

    public SyncedLyricsLine toDomain() {
        return new SyncedLyricsLine(id, lyricsId, startMs, endMs, lineText, lineIndex);
    }

    public static SyncedLyricsLineModel fromDomain(SyncedLyricsLine line) {
        return SyncedLyricsLineModel.builder()
                .id(line.getId())
                .lyricsId(line.getLyricsId())
                .startMs(line.getStartMs())
                .endMs(line.getEndMs())
                .lineText(line.getLineText())
                .lineIndex(line.getLineIndex())
                .build();
    }
}
