package ru.kduskov.annotations;

import ru.kduskov.annotations.enums.GenerationsRules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ru.kduskov.annotations.enums.GenerationsRules.DEFAULT;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratingRule {
    String regex() default "";

    GenerationsRules valueKey() default DEFAULT;

    /**
     * minLength - используется в связке с valueKey
     */
    int minLength() default -1;
    /**
     * maxLength - используется в связке с valueKey
     */
    int maxLength() default -1;
}
