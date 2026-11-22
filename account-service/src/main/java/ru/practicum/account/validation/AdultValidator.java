package ru.practicum.account.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {
    private int age;

    @Override
    public void initialize(Adult constraintAnnotation) {
        this.age = constraintAnnotation.age();
    }

    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        return birthdate == null || !birthdate.plusYears(age).isAfter(LocalDate.now());
    }
}
