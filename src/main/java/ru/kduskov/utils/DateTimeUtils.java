package ru.kduskov.utils;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@UtilityClass
public class DateTimeUtils {
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static long differenceWithCurrent(LocalDateTime utcTime) {
        ZonedDateTime serverTime = utcTime.atZone(UTC);
        ZonedDateTime currentTime = ZonedDateTime.now(UTC);
        return Math.abs(Duration.between(serverTime, currentTime).getSeconds());
    }
}
