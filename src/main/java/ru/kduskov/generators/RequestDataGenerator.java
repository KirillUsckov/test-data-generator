package ru.kduskov.generators;

import com.github.curiousoddman.rgxgen.RgxGen;
import net.datafaker.Faker;
import ru.kduskov.annotations.GeneratingRule;
import ru.kduskov.enums.GenerationsRules;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public final class RequestDataGenerator {
    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public static <T> T generateFilledObject(Class<T> clazz) {
        try {
            var instance = clazz.getDeclaredConstructor().newInstance();
            fillFields(instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private static Object generateFromRegex(String regex) {
        String reg = RgxGen.parse(regex).generate();
        return reg;
    }

    private static Object generateFromValueKey(GenerationsRules rule, int minLength, int maxLength) {
        return switch (rule) {
            case DEPOSIT_BALANCE -> Double.parseDouble(DF.format(new Random().nextDouble(0.01, 5_001)));
            case TRANSFER_AMOUNT -> Double.parseDouble(DF.format(new Random().nextDouble(0.01, 10_001)));
            case PASSWORD -> RandomData.generateSecurePassword(minLength, maxLength);
            default -> null;
        };
    }

    private static Object generateByType(Class<?> type) {
        if (type == String.class) {
            return FAKER.lorem().word();
        } else if (type == int.class || type == Integer.class) {
            return FAKER.number().numberBetween(1, 99999999);
        } else if (type == long.class || type == Long.class) {
            return FAKER.number().randomNumber();
        } else if (type == double.class || type == Double.class) {
            return FAKER.number().randomDouble(2, 1, 999999999);
        } else if (type == boolean.class || type == Boolean.class) {
            return FAKER.bool().bool();
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (type.isEnum()) {
            return generateEnumValue(type);
        } else if (type == List.class) {
            return new ArrayList<>();
        } else if (type == Map.class) {
            return new HashMap<>();
        }

        if (type.getPackage() != null) {
            try {
                Object nestedInstance = type.getDeclaredConstructor().newInstance();
                fillFields(nestedInstance);
                return nestedInstance;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // Остальные методы остаются без изменений
    private static Object generateEnumValue(Class<?> enumClass) {
        var enumConstants = enumClass.getEnumConstants();
        return enumConstants.length > 0
                ? enumConstants[RANDOM.nextInt(enumConstants.length)] : null;
    }

    private static void fillFields(Object obj) throws IllegalAccessException {
        var clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(obj) == null) {
                    var value = generateValueForField(field);
                    field.set(obj, value);
                }
            }
            clazz = clazz.getSuperclass();
            if (clazz == Object.class) {
                break;
            }
        }
    }

    private static Object generateValueForField(Field field) {
        var rule = field.getAnnotation(GeneratingRule.class);
        if (rule != null) {
            if (!rule.regex().isEmpty()) {
                return generateFromRegex(rule.regex());
            } else if (rule.valueKey() != GenerationsRules.DEFAULT) {
                return generateFromValueKey(rule.valueKey(), rule.minLength(), rule.maxLength());
            }
        }

        return generateByType(field.getType());
    }
}