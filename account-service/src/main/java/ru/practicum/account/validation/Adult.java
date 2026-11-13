package ru.practicum.account.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Дата рождения, при которой возраст не меньше {@link #age()} лет.
 * {@code null} считается валидным — обязательность проверяется через {@code @NotNull}.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdultValidator.class)
public @interface Adult {
    String message() default "Возраст должен быть старше 18 лет";

    int age() default 18;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
