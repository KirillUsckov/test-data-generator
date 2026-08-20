package ru.kduskov.annotations.enums;

public enum MatchingCondition {
    LENGTH_LESS,        // строка короче, чем минимальная длина regex
    LENGTH_MORE,        // строка длиннее, чем максимальная длина regex
    RANDOM   // любая строка, не соответствующая regex
}