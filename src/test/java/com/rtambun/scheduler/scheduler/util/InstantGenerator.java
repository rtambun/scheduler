package com.rtambun.scheduler.scheduler.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class InstantGenerator {

    public static Instant generateInstantUTC(int year, int month, int day, int hour, int minute, int second) {
        return ZonedDateTime.now(ZoneId.of("UTC"))
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(second)
                .withNano(0)
                .toInstant();
    }

    public static Instant generateInstantUTCBeforeMinutes(long minutes) {
        return ZonedDateTime.now(ZoneId.of("UTC"))
                .withNano(0)
                .minusSeconds(60 * minutes)
                .toInstant();
    }

}
