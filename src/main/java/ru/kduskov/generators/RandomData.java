package ru.kduskov.generators;

import net.datafaker.shaded.curiousoddman.rgxgen.RgxGen;
import org.apache.commons.lang3.RandomStringUtils;
import ru.kduskov.enums.GenerationsRules;
import ru.kduskov.enums.MatchingCondition;
import ru.kduskov.utils.RegexLengthExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;


public final class RandomData {
    private static final RandomGenerator RANDOM = RandomGenerator.getDefault();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@$!%+&=^";
    private static final String[] VALID_GROUPS = {UPPER, LOWER, SPECIAL, DIGITS};

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    public static String getValidName() {
        return String.format(
                "%s %s",
                RandomStringUtils.randomAlphabetic(1, 20).toLowerCase(),
                RandomStringUtils.randomAlphabetic(1, 20).toLowerCase()
        );
    }

    public static String getAlphabetAndNumericString(int length) {
        var stringsNumber = length / 2;
        var digitsNumber = length - stringsNumber;
        return String.format(
                "%s %s",
                getAlphabeticString(stringsNumber),
                getNumericString(digitsNumber)
        );
    }

    public static String getNumericString(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    public static String getAlphabeticString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static String getNotMatchingRegexString(MatchingCondition condition, String regex) {
        switch (condition) {
            case LENGTH_LESS:
                return generateLengthLess(regex);
            case LENGTH_MORE:
                return generateLengthMore(regex);
            case RANDOM:
            default:
                return generateRandomNotMatching(regex);
        }
    }

    public static String getNotMatchingCondition(MatchingCondition condition, GenerationsRules generationsRule) {
        return switch (generationsRule) {
            case PASSWORD -> generatePassword(condition);
            default -> throw new IllegalArgumentException("Unknown generation rule: " + generationsRule);
        };
    }

    private static String generatePassword(MatchingCondition condition) {
        switch (condition) {
            case LENGTH_LESS:
                return generateSecurePassword(1, MIN_PASSWORD_LENGTH);
            case LENGTH_MORE:
                return generateSecurePassword(MAX_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH * 2);
            case RANDOM:
            default:
                return generateInsecurePassword(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
        }
    }

    private static String generateLengthLess(String regex) {
        int minLength = getMinLength(regex);
        if (minLength <= 0) {
            return EMPTY;
        }
        return generateStringOfLength(minLength - 1);
    }

    private static String generateLengthMore(String regex) {
        int maxLength = getMaxLength(regex);
        return generateStringOfLength(maxLength + 1 + RANDOM.nextInt(10));
    }

    private static String generateRandomNotMatching(String regex) {
        return RgxGen.parse(regex).generateNotMatching();
    }

    private static String generateStringOfLength(int length) {
        if (length <= 0) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) (32 + RANDOM.nextInt(95))); // печатные ASCII символы
        }
        return sb.toString();
    }

    private static int getMinLength(String regex) {
        return RegexLengthExtractor.extractTotalLengthConstraint(regex).getMinLength();
    }

    private static int getMaxLength(String regex) {
        return RegexLengthExtractor.extractTotalLengthConstraint(regex).getMaxLength();
    }

    /**
     * Метод генерирует безопасный пароль
     * Использует только валидные группы символов
     *
     * @param minLength минимальная длина (минимум 4)
     * @param maxLength максимальная длина
     * @return безопасный пароль
     */
    public static String generateSecurePassword(int minLength, int maxLength) {
        return generateFromGroups(VALID_GROUPS, minLength, maxLength);
    }

    /**
     * Метод 3: Генерирует небезопасный пароль
     * Рандомно берет валидные и невалидные группы символов
     *
     * @param minLength минимальная длина
     * @param maxLength максимальная длина
     * @return небезопасный пароль
     */
    public static String generateInsecurePassword(int minLength, int maxLength) {
        String[] invalidGroups = {",.", "_;:", "<>", "/\\|", "\\`\\~", "'", "()", "[]", "{}"};
        String[] allGroups = Stream.of(invalidGroups, VALID_GROUPS)
                .flatMap(Stream::of)
                .toArray(String[]::new);
        // Случайно выбираем от 2 до 4 групп
        int numGroups = RANDOM.nextInt(2, Math.min(5, allGroups.length + 1));

        // Перемешиваем группы и берем первые numGroups
        List<String> shuffledGroups = new ArrayList<>(List.of(allGroups));
        Collections.shuffle(shuffledGroups, RANDOM);
        String[] selectedGroups = shuffledGroups.subList(0, numGroups).toArray(new String[0]);

        return generateFromGroups(selectedGroups, minLength, maxLength);
    }

    /**
     * Метод генерирует строку из переданных групп элементов
     * Гарантирует, что будет хотя бы 1 символ из каждой группы
     *
     * @param groups    массив строк с группами символов
     * @param minLength минимальная длина (не может быть меньше количества групп)
     * @param maxLength максимальная длина
     * @return сгенерированная строка
     */
    private static String generateFromGroups(String[] groups, int minLength, int maxLength) {
        if (groups == null || groups.length == 0) {
            throw new IllegalArgumentException("Должна быть хотя бы одна группа символов");
        }

        // Генерируем длину
        int length = RANDOM.nextInt(minLength, maxLength);

        StringBuilder password = new StringBuilder(length);
        List<Character> allCharsList = new ArrayList<>();

        // 1. Добавляем по одному символу из каждой группы
        for (String group : groups) {
            if (group == null || group.isEmpty()) {
                throw new IllegalArgumentException("Группа символов не может быть пустой");
            }
            char ch = group.charAt(RANDOM.nextInt(group.length()));
            password.append(ch);

            // Добавляем все символы группы в общий список
            for (char c : group.toCharArray()) {
                allCharsList.add(c);
            }
        }

        // 2. Заполняем оставшиеся позиции случайными символами из всех групп
        while (password.length() < length) {
            char ch = allCharsList.get(RANDOM.nextInt(allCharsList.size()));
            password.append(ch);
        }
        return password.toString();
    }
}
