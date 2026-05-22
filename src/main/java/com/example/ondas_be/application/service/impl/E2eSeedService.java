package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.service.port.E2eSeedServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@Profile("e2e")
@RequiredArgsConstructor
public class E2eSeedService implements E2eSeedServicePort {

    private static final UUID ADMIN_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SONG_ONE_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID SONG_TWO_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    private static final String AUDIO_OBJECT_ONE = "e2e/track-one.wav";
    private static final String AUDIO_OBJECT_TWO = "e2e/track-two.wav";

    private static final int AUDIO_DURATION_SECONDS = 5;
    private static final int AUDIO_SAMPLE_RATE = 8000;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final StoragePort storagePort;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.e2e.seed-script:classpath:e2e/seed-e2e.sql}")
    private String seedScript;

    @Value("${app.e2e.seed-password:E2ePass123!}")
    private String seedPassword;

    @Value("${app.e2e.seed-on-startup:true}")
    private boolean seedOnStartup;

    @Value("${app.e2e.reset-on-startup:true}")
    private boolean resetOnStartup;

    @Value("${storage.minio.bucket-audio}")
    private String audioBucket;

    @Override
    public void resetAndSeed() {
        log.info("E2E seed: resetting and seeding data");
        runSeedScript();
        updateSeedUsers();
        uploadSeedAudio();
        log.info("E2E seed: done");
    }

    @Override
    public void seedIfEmpty() {
        if (hasSeedData()) {
            log.info("E2E seed: data already present, skipping");
            return;
        }
        resetAndSeed();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!seedOnStartup) {
            return;
        }
        if (resetOnStartup) {
            resetAndSeed();
        } else {
            seedIfEmpty();
        }
    }

    private boolean hasSeedData() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM songs", Integer.class);
        return count != null && count > 0;
    }

    private void runSeedScript() {
        Resource resource = resourceLoader.getResource(seedScript);
        if (!resource.exists()) {
            throw new IllegalStateException("E2E seed script not found: " + seedScript);
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.execute(dataSource);
    }

    private void updateSeedUsers() {
        String hash = passwordEncoder.encode(seedPassword);
        jdbcTemplate.update(
                "UPDATE users SET password_hash = ? WHERE id IN (?, ?)",
                hash,
                ADMIN_USER_ID,
                USER_ID
        );
    }

    private void uploadSeedAudio() {
        byte[] audioBytes = buildSilentWavBytes(AUDIO_DURATION_SECONDS, AUDIO_SAMPLE_RATE);
        upsertSongAudio(SONG_ONE_ID, AUDIO_OBJECT_ONE, audioBytes);
        upsertSongAudio(SONG_TWO_ID, AUDIO_OBJECT_TWO, audioBytes);
    }

    private void upsertSongAudio(UUID songId, String objectName, byte[] audioBytes) {
        String audioUrl = storagePort.upload(
                audioBucket,
                objectName,
                new ByteArrayInputStream(audioBytes),
                audioBytes.length,
                "audio/wav"
        );
        jdbcTemplate.update(
                "UPDATE songs SET audio_url = ?, audio_size_bytes = ?, audio_format = ? WHERE id = ?",
                audioUrl,
                (long) audioBytes.length,
                "wav",
                songId
        );
    }

    private static byte[] buildSilentWavBytes(int durationSeconds, int sampleRate) {
        int numChannels = 1;
        int bitsPerSample = 16;
        int bytesPerSample = bitsPerSample / 8;
        int dataSize = sampleRate * durationSeconds * numChannels * bytesPerSample;
        int chunkSize = 36 + dataSize;

        ByteBuffer buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put("RIFF".getBytes(StandardCharsets.US_ASCII));
        buffer.putInt(chunkSize);
        buffer.put("WAVE".getBytes(StandardCharsets.US_ASCII));
        buffer.put("fmt ".getBytes(StandardCharsets.US_ASCII));
        buffer.putInt(16);
        buffer.putShort((short) 1);
        buffer.putShort((short) numChannels);
        buffer.putInt(sampleRate);
        buffer.putInt(sampleRate * numChannels * bytesPerSample);
        buffer.putShort((short) (numChannels * bytesPerSample));
        buffer.putShort((short) bitsPerSample);
        buffer.put("data".getBytes(StandardCharsets.US_ASCII));
        buffer.putInt(dataSize);
        return buffer.array();
    }
}
