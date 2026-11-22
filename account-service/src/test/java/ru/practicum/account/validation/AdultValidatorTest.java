package ru.practicum.account.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdultValidatorTest {
    private final AdultValidator validator = new AdultValidator();

    @BeforeEach
    void setUp() {
        Adult adult = mock(Adult.class);
        when(adult.age()).thenReturn(18);
        validator.initialize(adult);
    }

    @Test
    void acceptsPersonWhoTurnedEighteenToday() {
        assertThat(validator.isValid(LocalDate.now().minusYears(18), null)).isTrue();
    }

    @Test
    void rejectsPersonYoungerThanEighteen() {
        assertThat(validator.isValid(LocalDate.now().minusYears(18).plusDays(1), null)).isFalse();
    }

    @Test
    void leavesNullToNotNullConstraint() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
