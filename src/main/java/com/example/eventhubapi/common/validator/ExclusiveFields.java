package com.example.eventhubapi.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom validation annotation to ensure that at most one of the specified fields
 * in a class is non-null.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExclusiveFieldsValidator.class)
public @interface ExclusiveFields {
    String message() default "Only one of the specified fields can be present";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String[] value();
}