package com.example.eventhubapi.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Arrays;
import java.util.Objects;

public class ExclusiveFieldsValidator implements ConstraintValidator<ExclusiveFields, Object> {

    private String[] fieldNames;

    @Override
    public void initialize(ExclusiveFields constraintAnnotation) {
        this.fieldNames = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }


        long nonNullFieldsCount = Arrays.stream(fieldNames)
                .map(fieldName -> new BeanWrapperImpl(value).getPropertyValue(fieldName))
                .filter(Objects::nonNull)
                .count();


        return nonNullFieldsCount <= 1;
    }
}