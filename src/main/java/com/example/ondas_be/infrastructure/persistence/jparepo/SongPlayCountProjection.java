package com.example.ondas_be.infrastructure.persistence.jparepo;

import java.util.UUID;

public interface SongPlayCountProjection {

    UUID getSongId();

    Long getPlayCount();
}
