package com.example.ondas_be.infrastructure.persistence.jparepo;

import java.time.LocalDate;

public interface DailyPlayCountProjection {

    LocalDate getDay();

    Long getPlayCount();
}
