-- E2E reset + seed data (PostgreSQL)

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

-- Users (password_hash will be updated by E2E seed service)
INSERT INTO users (
  id, email, password_hash, display_name, avatar_url, is_active,
  ban_reason, banned_at, last_login_at, role, created_at, updated_at
) VALUES
  ('11111111-1111-1111-1111-111111111111', 'admin@e2e.local', 'PENDING', 'E2E Admin', NULL, true,
   NULL, NULL, NULL, 'ADMIN', NOW(), NOW()),
  ('22222222-2222-2222-2222-222222222222', 'user@e2e.local', 'PENDING', 'E2E User', NULL, true,
   NULL, NULL, NULL, 'USER', NOW(), NOW());

-- Artists
INSERT INTO artists (
  id, name, slug, bio, avatar_url, country, created_by, created_at, updated_at
) VALUES
  ('33333333-3333-3333-3333-333333333333', 'E2E Artist One', 'e2e-artist-one',
   'Seed artist for e2e tests', NULL, 'VN', '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('44444444-4444-4444-4444-444444444444', 'E2E Artist Two', 'e2e-artist-two',
   'Second seed artist for e2e tests', NULL, 'US', '11111111-1111-1111-1111-111111111111', NOW(), NOW());

-- Albums
INSERT INTO albums (
  id, title, slug, cover_url, release_date, album_type, description,
  total_tracks, created_by, created_at, updated_at
) VALUES
  ('55555555-5555-5555-5555-555555555555', 'E2E Album One', 'e2e-album-one',
   NULL, DATE '2024-01-01', 'album', 'Seed album for e2e tests',
   2, '11111111-1111-1111-1111-111111111111', NOW(), NOW());

-- Genres
INSERT INTO genres (id, name, slug, description, cover_url, created_at) VALUES
  (1, 'Pop', 'pop', 'Pop genre', NULL, NOW()),
  (2, 'Electronic', 'electronic', 'Electronic genre', NULL, NOW());

-- Tags
INSERT INTO tags (id, name, type, color_hex, created_at) VALUES
  (1, 'Chill', 'mood', '#88C0D0', NOW()),
  (2, 'Workout', 'mood', '#EBCB8B', NOW());

-- Songs (audio_url will be updated by E2E seed service)
INSERT INTO songs (
  id, title, slug, duration_seconds, audio_url, audio_format, audio_size_bytes,
  cover_url, album_id, track_number, release_date, play_count, is_active,
  created_by, created_at, updated_at
) VALUES
  ('66666666-6666-6666-6666-666666666666', 'E2E Track One', 'e2e-track-one', 5,
   'PENDING', 'wav', NULL, NULL, '55555555-5555-5555-5555-555555555555', 1,
   DATE '2024-01-01', 0, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW()),
  ('77777777-7777-7777-7777-777777777777', 'E2E Track Two', 'e2e-track-two', 5,
   'PENDING', 'wav', NULL, NULL, NULL, NULL,
   DATE '2024-02-01', 0, true, '11111111-1111-1111-1111-111111111111', NOW(), NOW());

-- Album <-> Artist
INSERT INTO album_artists (album_id, artist_id, is_primary) VALUES
  ('55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', true);

-- Song <-> Artist
INSERT INTO song_artists (song_id, artist_id, role) VALUES
  ('66666666-6666-6666-6666-666666666666', '33333333-3333-3333-3333-333333333333', 'main'),
  ('77777777-7777-7777-7777-777777777777', '44444444-4444-4444-4444-444444444444', 'main');

-- Song <-> Genre
INSERT INTO song_genres (song_id, genre_id) VALUES
  ('66666666-6666-6666-6666-666666666666', 1),
  ('77777777-7777-7777-7777-777777777777', 2);

-- Song <-> Tag
INSERT INTO song_tags (song_id, tag_id) VALUES
  ('66666666-6666-6666-6666-666666666666', 1),
  ('77777777-7777-7777-7777-777777777777', 2);

-- Playlists
INSERT INTO playlists (
  id, user_id, name, description, cover_url, is_public, total_songs, created_at, updated_at
) VALUES
  ('88888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222',
   'E2E Playlist', 'Seed playlist for e2e tests', NULL, true, 2, NOW(), NOW());

-- Playlist songs
INSERT INTO playlist_songs (playlist_id, song_id, position, added_at) VALUES
  ('88888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666', 1, NOW()),
  ('88888888-8888-8888-8888-888888888888', '77777777-7777-7777-7777-777777777777', 2, NOW());

-- Favorites
INSERT INTO favorites (user_id, song_id, created_at) VALUES
  ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666666', NOW()),
  ('22222222-2222-2222-2222-222222222222', '77777777-7777-7777-7777-777777777777', NOW());
