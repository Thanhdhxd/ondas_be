-- E2E reset + seed data (PostgreSQL)
-- Supports all test cases: auth, favorites, music_streaming, playlist, search

TRUNCATE TABLE
  favorites,
  playlist_songs,
  playlists,
  play_histories,
  search_histories,
  otp_codes,
  refresh_tokens,
  synced_lyrics_lines,
  lyrics,
  song_tags,
  song_genres,
  song_artists,
  album_artists,
  songs,
  albums,
  artists,
  tags,
  genres,
  users
RESTART IDENTITY CASCADE;

-- ============================================================
-- USERS (5)
-- password_hash will be updated by E2eSeedService
-- ============================================================
INSERT INTO users (
  id, email, password_hash, display_name, avatar_url, is_active,
  ban_reason, banned_at, last_login_at, role, created_at, updated_at
) VALUES
  -- Admin user
  ('11111111-1111-1111-1111-111111111111', 'admin@e2e.local', 'PENDING',
   'E2E Admin', NULL, true,
   NULL, NULL, NULL, 'ADMIN', NOW(), NOW()),
  -- Normal active user (primary test user)
  ('22222222-2222-2222-2222-222222222222', 'user@e2e.local', 'PENDING',
   'E2E User', NULL, true,
   NULL, NULL, NULL, 'USER', NOW(), NOW()),
  -- Inactive user (Login TC#24: chưa kích hoạt)
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'inactive@e2e.local', 'PENDING',
   'Inactive User', NULL, false,
   NULL, NULL, NULL, 'USER', NOW(), NOW()),
  -- Disabled/banned user (Login TC#25: bị vô hiệu hóa)
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'disabled@e2e.local', 'PENDING',
   'Disabled User', NULL, true,
   'E2E test: account disabled', NOW(), NULL, 'USER', NOW(), NOW()),
  -- Second normal user (Playlist TC#34: cross-user access test)
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'user2@e2e.local', 'PENDING',
   'E2E User Two', NULL, true,
   NULL, NULL, NULL, 'USER', NOW(), NOW());

-- ============================================================
-- ARTISTS (7)
-- ============================================================
INSERT INTO artists (
  id, name, slug, bio, avatar_url, country, created_by, created_at, updated_at
) VALUES
  ('33333333-3333-3333-3333-333333333333', 'E2E Artist One', 'e2e-artist-one',
   'Seed artist for e2e tests', NULL, 'VN', '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('44444444-4444-4444-4444-444444444444', 'E2E Artist Two', 'e2e-artist-two',
   'Second seed artist for e2e tests', NULL, 'US', '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('33333333-3333-3333-3333-333333333334', 'E2E Artist Three', 'e2e-artist-three',
   'Third seed artist', NULL, 'UK', '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('33333333-3333-3333-3333-333333333335', 'E2E Artist Four', 'e2e-artist-four',
   'Fourth seed artist', NULL, 'KR', '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('33333333-3333-3333-3333-333333333336', 'E2E Artist Five', 'e2e-artist-five',
   'Fifth seed artist', NULL, 'JP', '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  -- Empty artist (Search TC#45: artist không có bài hát)
  ('33333333-3333-3333-3333-333333333337', 'Empty Artist', 'empty-artist',
   'Artist with no songs for boundary testing', NULL, 'VN',
   '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  -- Long name artist (Search TC#40: tên ≥ 50 ký tự)
  ('33333333-3333-3333-3333-333333333338',
   'A Very Long Artist Name That Exceeds Fifty Characters For Display Testing',
   'a-very-long-artist-name',
   'Artist with extremely long name for ellipsis testing', NULL, 'US',
   '11111111-1111-1111-1111-111111111111', NOW(), NOW());

-- ============================================================
-- ALBUMS (3)
-- ============================================================
INSERT INTO albums (
  id, title, slug, cover_url, release_date, album_type, description,
  total_tracks, created_by, created_at, updated_at
) VALUES
  ('55555555-5555-5555-5555-555555555555', 'E2E Album One', 'e2e-album-one',
   NULL, DATE '2024-01-01', 'album', 'Seed album for e2e tests',
   8, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  -- Empty album (Search TC#46, 83: album không có bài hát)
  ('55555555-5555-5555-5555-555555555556', 'Empty Album', 'empty-album',
   NULL, DATE '2024-06-01', 'album', 'Album with no songs for boundary testing',
   0, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  -- Long name album (Search TC#41: tên ≥ 50 ký tự)
  ('55555555-5555-5555-5555-555555555557',
   'A Very Long Album Title That Exceeds Normal Display Limits For Boundary Testing',
   'a-very-long-album-title',
   NULL, DATE '2024-03-01', 'album',
   'Album with extremely long title for ellipsis testing',
   5, '11111111-1111-1111-1111-111111111111', NOW(), NOW());

-- ============================================================
-- GENRES (5)
-- ============================================================
INSERT INTO genres (id, name, slug, description, cover_url, created_at) VALUES
  (1, 'Pop', 'pop', 'Pop genre', NULL, NOW()),
  (2, 'Electronic', 'electronic', 'Electronic genre', NULL, NOW()),
  (3, 'Rock', 'rock', 'Rock genre', NULL, NOW()),
  (4, 'Jazz', 'jazz', 'Jazz genre', NULL, NOW()),
  (5, 'R&B', 'rnb', 'R&B genre', NULL, NOW());

-- ============================================================
-- TAGS (4)
-- ============================================================
INSERT INTO tags (id, name, type, color_hex, created_at) VALUES
  (1, 'Chill', 'mood', '#88C0D0', NOW()),
  (2, 'Workout', 'mood', '#EBCB8B', NOW()),
  (3, 'Party', 'mood', '#BF616A', NOW()),
  (4, 'Study', 'mood', '#A3BE8C', NOW());

-- ============================================================
-- SONGS (30)
-- audio_url will be updated by E2eSeedService
-- ============================================================
INSERT INTO songs (
  id, title, slug, duration_seconds, audio_url, audio_format, audio_size_bytes,
  cover_url, album_id, track_number, release_date, play_count, is_active,
  created_by, created_at, updated_at
) VALUES
  -- Songs 01-02: Original (in Album One)
  -- Song 01: dual-genre (Pop + Electronic) — Search TC#4: artist filter, Search TC#82: multi-section
  ('66666666-6666-6666-6666-666666666601', 'E2E Track One', 'e2e-track-one', 210,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 1,
   DATE '2024-01-01', 120, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666602', 'E2E Track Two', 'e2e-track-two', 180,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 2,
   DATE '2024-01-01', 85, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Songs 03-07: "Love" themed (Search keyword test: "love")
  ('66666666-6666-6666-6666-666666666603', 'Love Song One', 'love-song-one', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 3,
   DATE '2024-01-15', 200, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666604', 'Love Song Two', 'love-song-two', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 4,
   DATE '2024-01-20', 150, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666605', 'Love Is All Around', 'love-is-all-around', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 5,
   DATE '2024-02-01', 95, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666606', 'Endless Love', 'endless-love', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 6,
   DATE '2024-02-10', 110, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666607', 'Love Me Tender', 'love-me-tender', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 7,
   DATE '2024-02-15', 75, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Songs 08-12: "Summer" themed (in Album One + Long Name Album)
  ('66666666-6666-6666-6666-666666666608', 'Summer Vibes', 'summer-vibes', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 8,
   DATE '2024-03-01', 180, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666609', 'Summer Nights', 'summer-nights', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555557', 1,
   DATE '2024-03-10', 160, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666610', 'Summer Rain', 'summer-rain', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555557', 2,
   DATE '2024-03-15', 90, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666611', 'Summer Breeze', 'summer-breeze', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555557', 3,
   DATE '2024-03-20', 70, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666612', 'Summer Dreams', 'summer-dreams', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555557', 4,
   DATE '2024-03-25', 65, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Songs 13-17: "Rock" themed (no album)
  ('66666666-6666-6666-6666-666666666613', 'Rock Anthem One', 'rock-anthem-one', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-04-01', 300, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666614', 'Rock Anthem Two', 'rock-anthem-two', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-04-05', 250, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666615', 'Rock Anthem Three', 'rock-anthem-three', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-04-10', 190, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666616', 'Rock Anthem Four', 'rock-anthem-four', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-04-15', 140, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666617', 'Rock Anthem Five', 'rock-anthem-five', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-04-20', 100, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Songs 18-22: "Jazz" themed (in Long Name Album)
  ('66666666-6666-6666-6666-666666666618', 'Jazz Night One', 'jazz-night-one', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555557', 5,
   DATE '2024-05-01', 60, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666619', 'Jazz Night Two', 'jazz-night-two', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-05-05', 55, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666620', 'Jazz Night Three', 'jazz-night-three', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-05-10', 45, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666621', 'Jazz Night Four', 'jazz-night-four', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-05-15', 35, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666622', 'Jazz Night Five', 'jazz-night-five', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-05-20', 30, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Songs 23-25: "Electronic" themed
  ('66666666-6666-6666-6666-666666666623', 'Electronic Beat One', 'electronic-beat-one', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-06-01', 80, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666624', 'Electronic Beat Two', 'electronic-beat-two', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-06-05', 70, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666625', 'Electronic Beat Three', 'electronic-beat-three', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-06-10', 50, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Song 26: Long title (>100 chars) — Favorites TC#20, Player TC#33, Search TC#40
  ('66666666-6666-6666-6666-666666666626',
   'A Very Long Song Title That Exceeds One Hundred Characters For Testing Ellipsis And Text Overflow In Various UI Components',
   'a-very-long-song-title', 240,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-07-01', 10, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Song 27: Multi-artist (5 artists) — Favorites TC#21, Player TC#34, Search TC#85
  ('66666666-6666-6666-6666-666666666627', 'Multi Artist Collab', 'multi-artist-collab', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-07-10', 15, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Song 28: Duration = 0 — Player TC#20
  ('66666666-6666-6666-6666-666666666628', 'Zero Duration Track', 'zero-duration-track', 0,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-07-15', 5, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Song 29: Duration = 10800 (3 hours) — Player TC#21
  ('66666666-6666-6666-6666-666666666629', 'Three Hour Marathon Mix', 'three-hour-marathon-mix', 10800,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-07-20', 3, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),

  -- Song 30: Has lyrics — Player TC#16
  ('66666666-6666-6666-6666-666666666630', 'Lyrics Track', 'lyrics-track', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-07-25', 25, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW());

-- ============================================================
-- ALBUM <-> ARTIST
-- ============================================================
INSERT INTO album_artists (album_id, artist_id, is_primary) VALUES
  ('55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', true),
  ('55555555-5555-5555-5555-555555555556', '33333333-3333-3333-3333-333333333337', true),
  ('55555555-5555-5555-5555-555555555557', '44444444-4444-4444-4444-444444444444', true),
  ('55555555-5555-5555-5555-555555555557', '33333333-3333-3333-3333-333333333334', false);

-- ============================================================
-- SONG <-> ARTIST
-- ============================================================
INSERT INTO song_artists (song_id, artist_id, role) VALUES
  -- Songs 01-08: Artist One (main)
  ('66666666-6666-6666-6666-666666666601', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666602', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666603', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666604', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666605', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666606', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666607', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666608', '33333333-3333-3333-3333-333333333333', 'main'),
  -- Songs 09-12: Artist Two (main)
  ('66666666-6666-6666-6666-666666666609', '44444444-4444-4444-4444-444444444444', 'main'),
  ('66666666-6666-6666-6666-666666666610', '44444444-4444-4444-4444-444444444444', 'main'),
  ('66666666-6666-6666-6666-666666666611', '44444444-4444-4444-4444-444444444444', 'main'),
  ('66666666-6666-6666-6666-666666666612', '44444444-4444-4444-4444-444444444444', 'main'),
  -- Songs 13-17: Artist Three (main) — Rock songs
  ('66666666-6666-6666-6666-666666666613', '33333333-3333-3333-3333-333333333334', 'main'),
  ('66666666-6666-6666-6666-666666666614', '33333333-3333-3333-3333-333333333334', 'main'),
  ('66666666-6666-6666-6666-666666666615', '33333333-3333-3333-3333-333333333334', 'main'),
  ('66666666-6666-6666-6666-666666666616', '33333333-3333-3333-3333-333333333334', 'main'),
  ('66666666-6666-6666-6666-666666666617', '33333333-3333-3333-3333-333333333334', 'main'),
  -- Songs 18-22: Artist Four (main) — Jazz songs
  ('66666666-6666-6666-6666-666666666618', '33333333-3333-3333-3333-333333333335', 'main'),
  ('66666666-6666-6666-6666-666666666619', '33333333-3333-3333-3333-333333333335', 'main'),
  ('66666666-6666-6666-6666-666666666620', '33333333-3333-3333-3333-333333333335', 'main'),
  ('66666666-6666-6666-6666-666666666621', '33333333-3333-3333-3333-333333333335', 'main'),
  ('66666666-6666-6666-6666-666666666622', '33333333-3333-3333-3333-333333333335', 'main'),
  -- Songs 23-25: Artist Five (main) — Electronic songs
  ('66666666-6666-6666-6666-666666666623', '33333333-3333-3333-3333-333333333336', 'main'),
  ('66666666-6666-6666-6666-666666666624', '33333333-3333-3333-3333-333333333336', 'main'),
  ('66666666-6666-6666-6666-666666666625', '33333333-3333-3333-3333-333333333336', 'main'),
  -- Song 26: Long title — Long Name Artist
  ('66666666-6666-6666-6666-666666666626', '33333333-3333-3333-3333-333333333338', 'main'),
  -- Song 27: Multi-artist (5 artists!) — collab
  ('66666666-6666-6666-6666-666666666627', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666627', '44444444-4444-4444-4444-444444444444', 'featured'),
  ('66666666-6666-6666-6666-666666666627', '33333333-3333-3333-3333-333333333334', 'featured'),
  ('66666666-6666-6666-6666-666666666627', '33333333-3333-3333-3333-333333333335', 'featured'),
  ('66666666-6666-6666-6666-666666666627', '33333333-3333-3333-3333-333333333336', 'featured'),
  -- Songs 28-30: Artist One (main)
  ('66666666-6666-6666-6666-666666666628', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666629', '33333333-3333-3333-3333-333333333333', 'main'),
  ('66666666-6666-6666-6666-666666666630', '33333333-3333-3333-3333-333333333333', 'main');

-- ============================================================
-- SONG <-> GENRE
-- ============================================================
INSERT INTO song_genres (song_id, genre_id) VALUES
  -- Pop songs (01-08)
  ('66666666-6666-6666-6666-666666666601', 1),
  ('66666666-6666-6666-6666-666666666602', 1),
  ('66666666-6666-6666-6666-666666666603', 1),
  ('66666666-6666-6666-6666-666666666604', 1),
  ('66666666-6666-6666-6666-666666666605', 1),
  ('66666666-6666-6666-6666-666666666606', 5), -- R&B
  ('66666666-6666-6666-6666-666666666607', 1),
  ('66666666-6666-6666-6666-666666666608', 1),
  -- Summer songs (09-12) — Pop
  ('66666666-6666-6666-6666-666666666609', 1),
  ('66666666-6666-6666-6666-666666666610', 1),
  ('66666666-6666-6666-6666-666666666611', 1),
  ('66666666-6666-6666-6666-666666666612', 1),
  -- Rock songs (13-17)
  ('66666666-6666-6666-6666-666666666613', 3),
  ('66666666-6666-6666-6666-666666666614', 3),
  ('66666666-6666-6666-6666-666666666615', 3),
  ('66666666-6666-6666-6666-666666666616', 3),
  ('66666666-6666-6666-6666-666666666617', 3),
  -- Jazz songs (18-22)
  ('66666666-6666-6666-6666-666666666618', 4),
  ('66666666-6666-6666-6666-666666666619', 4),
  ('66666666-6666-6666-6666-666666666620', 4),
  ('66666666-6666-6666-6666-666666666621', 4),
  ('66666666-6666-6666-6666-666666666622', 4),
  -- Electronic songs (23-25)
  ('66666666-6666-6666-6666-666666666623', 2),
  ('66666666-6666-6666-6666-666666666624', 2),
  ('66666666-6666-6666-6666-666666666625', 2),
  -- Special songs (26-30)
  ('66666666-6666-6666-6666-666666666626', 1),
  ('66666666-6666-6666-6666-666666666627', 1),
  ('66666666-6666-6666-6666-666666666628', 2),
  ('66666666-6666-6666-6666-666666666629', 2),
  ('66666666-6666-6666-6666-666666666630', 1);

-- ============================================================
-- SONG <-> TAG
-- ============================================================
INSERT INTO song_tags (song_id, tag_id) VALUES
  ('66666666-6666-6666-6666-666666666601', 1), -- Chill
  ('66666666-6666-6666-6666-666666666602', 1),
  ('66666666-6666-6666-6666-666666666603', 1),
  ('66666666-6666-6666-6666-666666666608', 3), -- Party
  ('66666666-6666-6666-6666-666666666609', 3),
  ('66666666-6666-6666-6666-666666666613', 2), -- Workout
  ('66666666-6666-6666-6666-666666666614', 2),
  ('66666666-6666-6666-6666-666666666618', 4), -- Study
  ('66666666-6666-6666-6666-666666666619', 4),
  ('66666666-6666-6666-6666-666666666623', 3), -- Party
  ('66666666-6666-6666-6666-666666666630', 1); -- Chill

-- ============================================================
-- PLAYLISTS (5)
-- ============================================================
INSERT INTO playlists (
  id, user_id, name, description, cover_url, is_public, total_songs, created_at, updated_at
) VALUES
  -- E2E Playlist (primary test playlist — 2 songs)
  ('88888888-8888-8888-8888-888888888881', '22222222-2222-2222-2222-222222222222',
   'E2E Playlist', 'Seed playlist for e2e tests', NULL, true, 2, NOW(), NOW()),
  -- Rock Classics (5 songs — for swipe delete, reorder, Play All tests)
  ('88888888-8888-8888-8888-888888888882', '22222222-2222-2222-2222-222222222222',
   'Rock Classics', 'Rock songs collection', NULL, true, 5, NOW(), NOW()),
  -- Pop Hits (3 songs)
  ('88888888-8888-8888-8888-888888888883', '22222222-2222-2222-2222-222222222222',
   'Pop Hits', 'Popular pop songs', NULL, true, 3, NOW(), NOW()),
  -- Jazz Collection (1 song — for TC#21: xóa bài cuối cùng)
  ('88888888-8888-8888-8888-888888888884', '22222222-2222-2222-2222-222222222222',
   'Jazz Collection', 'Jazz music', NULL, true, 1, NOW(), NOW()),
  -- Admin Private Playlist (belongs to admin — for TC#34: cross-user 403 test)
  ('88888888-8888-8888-8888-888888888885', '11111111-1111-1111-1111-111111111111',
   'Admin Private Playlist', 'Private playlist of admin', NULL, false, 2, NOW(), NOW()),
  -- Empty Playlist (belongs to E2E User, zero songs — Playlist TC#22, TC#54)
  ('88888888-8888-8888-8888-888888888886', '22222222-2222-2222-2222-222222222222',
   'Empty Playlist', 'Playlist with zero songs for boundary testing', NULL, true, 0, NOW(), NOW());

-- ============================================================
-- PLAYLIST SONGS
-- ============================================================
INSERT INTO playlist_songs (playlist_id, song_id, position, added_at) VALUES
  -- E2E Playlist (2 songs)
  ('88888888-8888-8888-8888-888888888881', '66666666-6666-6666-6666-666666666601', 1, NOW()),
  ('88888888-8888-8888-8888-888888888881', '66666666-6666-6666-6666-666666666602', 2, NOW()),
  -- Rock Classics (5 songs — for reorder + swipe delete)
  ('88888888-8888-8888-8888-888888888882', '66666666-6666-6666-6666-666666666613', 1, NOW()),
  ('88888888-8888-8888-8888-888888888882', '66666666-6666-6666-6666-666666666614', 2, NOW()),
  ('88888888-8888-8888-8888-888888888882', '66666666-6666-6666-6666-666666666615', 3, NOW()),
  ('88888888-8888-8888-8888-888888888882', '66666666-6666-6666-6666-666666666616', 4, NOW()),
  ('88888888-8888-8888-8888-888888888882', '66666666-6666-6666-6666-666666666617', 5, NOW()),
  -- Pop Hits (3 songs)
  ('88888888-8888-8888-8888-888888888883', '66666666-6666-6666-6666-666666666603', 1, NOW()),
  ('88888888-8888-8888-8888-888888888883', '66666666-6666-6666-6666-666666666604', 2, NOW()),
  ('88888888-8888-8888-8888-888888888883', '66666666-6666-6666-6666-666666666605', 3, NOW()),
  -- Jazz Collection (1 song)
  ('88888888-8888-8888-8888-888888888884', '66666666-6666-6666-6666-666666666618', 1, NOW()),
  -- Admin Private Playlist (2 songs)
  ('88888888-8888-8888-8888-888888888885', '66666666-6666-6666-6666-666666666601', 1, NOW()),
  ('88888888-8888-8888-8888-888888888885', '66666666-6666-6666-6666-666666666602', 2, NOW());

-- ============================================================
-- FAVORITES (25 for E2E User — covers pagination with pageSize=20)
-- ============================================================
INSERT INTO favorites (user_id, song_id, created_at) VALUES
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666601', NOW() - INTERVAL '25 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666602', NOW() - INTERVAL '24 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666603', NOW() - INTERVAL '23 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666604', NOW() - INTERVAL '22 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666605', NOW() - INTERVAL '21 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666606', NOW() - INTERVAL '20 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666607', NOW() - INTERVAL '19 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666608', NOW() - INTERVAL '18 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666609', NOW() - INTERVAL '17 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666610', NOW() - INTERVAL '16 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666611', NOW() - INTERVAL '15 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666612', NOW() - INTERVAL '14 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666613', NOW() - INTERVAL '13 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666614', NOW() - INTERVAL '12 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666615', NOW() - INTERVAL '11 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666616', NOW() - INTERVAL '10 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666617', NOW() - INTERVAL '9 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666618', NOW() - INTERVAL '8 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666619', NOW() - INTERVAL '7 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666620', NOW() - INTERVAL '6 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666621', NOW() - INTERVAL '5 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666622', NOW() - INTERVAL '4 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666623', NOW() - INTERVAL '3 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666624', NOW() - INTERVAL '2 minutes'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666625', NOW() - INTERVAL '1 minute');

-- Favorites for song 26 (long title — Favorites TC#20) and song 27 (multi-artist — TC#21)
INSERT INTO favorites (user_id, song_id, created_at) VALUES
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666626', NOW() - INTERVAL '30 seconds'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666627', NOW());

-- Favorites for User2 (for Favorite TC#13: multiple buttons on same screen)
INSERT INTO favorites (user_id, song_id, created_at) VALUES
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '66666666-6666-6666-6666-666666666601', NOW()),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '66666666-6666-6666-6666-666666666603', NOW()),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '66666666-6666-6666-6666-666666666608', NOW());

-- ============================================================
-- LYRICS (for Song 30: Lyrics Track + Song 01: static-only)
-- ============================================================
-- Static-only lyrics for Song 01 (Player TC#16: hasSynced=false, plainText only)
INSERT INTO lyrics (
  id, song_id, plain_text, has_synced, language, created_by, created_at, updated_at
) VALUES (
  'dddddddd-1111-1111-1111-dddddddddddd',
  '66666666-6666-6666-6666-666666666601',
  E'E2E Track One lyrics\nLine two of the song\nLine three goes here\nThis is line four\nFinal line five',
  false, 'en', '11111111-1111-1111-1111-111111111111', NOW(), NOW()
);

-- Synced lyrics for Song 30 (Player TC#16: hasSynced=true, synced lines with timestamps)
INSERT INTO lyrics (
  id, song_id, plain_text, has_synced, language, created_by, created_at, updated_at
) VALUES (
  'dddddddd-dddd-dddd-dddd-dddddddddddd',
  '66666666-6666-6666-6666-666666666630',
  E'This is the first line of the lyrics\nThe melody flows through the night\nEvery word tells a story\nOf love and of light\nThe music will never fade away',
  true, 'en', '11111111-1111-1111-1111-111111111111', NOW(), NOW()
);

-- Synced lyrics lines
INSERT INTO synced_lyrics_lines (lyrics_id, start_ms, end_ms, line_text, line_index) VALUES
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 0, 1000, 'This is the first line of the lyrics', 0),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 1000, 2000, 'The melody flows through the night', 1),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 2000, 3000, 'Every word tells a story', 2),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 3000, 4000, 'Of love and of light', 3),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 4000, 5000, 'The music will never fade away', 4);

-- ============================================================
-- SEARCH HISTORY (for E2E User — Search suggestions TC#06)
-- ============================================================
INSERT INTO search_histories (user_id, query, searched_at) VALUES
  ('22222222-2222-2222-2222-222222222222', 'pop', NOW() - INTERVAL '3 hours'),
  ('22222222-2222-2222-2222-222222222222', 'rock', NOW() - INTERVAL '2 hours'),
  ('22222222-2222-2222-2222-222222222222', 'jazz', NOW() - INTERVAL '1 hour');

-- ============================================================
-- PLAY HISTORY (for trending data — Search suggestions TC#05)
-- ============================================================
INSERT INTO play_histories (user_id, song_id, played_at, source) VALUES
  -- E2E User play history
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666601', NOW() - INTERVAL '5 hours', 'search'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666603', NOW() - INTERVAL '4 hours', 'search'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666608', NOW() - INTERVAL '3 hours', 'favorites'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666613', NOW() - INTERVAL '2 hours', 'playlist'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666618', NOW() - INTERVAL '1 hour', 'search'),
  -- User2 play history (for cross-user verification)
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '66666666-6666-6666-6666-666666666601', NOW() - INTERVAL '30 minutes', 'search'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '66666666-6666-6666-6666-666666666613', NOW() - INTERVAL '15 minutes', 'search'),
  -- Additional trending data — repeat plays for trending weight
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666608', NOW() - INTERVAL '50 minutes', 'search'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666608', NOW() - INTERVAL '45 minutes', 'search'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666613', NOW() - INTERVAL '40 minutes', 'playlist'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666613', NOW() - INTERVAL '35 minutes', 'playlist'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666623', NOW() - INTERVAL '28 minutes', 'search'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666624', NOW() - INTERVAL '18 minutes', 'home'),
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666625', NOW() - INTERVAL '8 minutes', 'search');
