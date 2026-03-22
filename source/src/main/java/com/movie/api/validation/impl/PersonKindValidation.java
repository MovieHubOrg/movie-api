package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.PersonKindConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PersonKindValidation implements ConstraintValidator<PersonKindConstraint, Object> {
    private boolean allowNull;

    @Override
    public void initialize(PersonKindConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        if (value instanceof Integer) {
            return checkValid((Integer) value);
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;

            // check empty
            if (list.isEmpty()) {
                return false;
            }

            Set<Integer> unique = new HashSet<>();
            for (Object o : list) {
                if (!(o instanceof Integer)) {
                    return false;
                }
                Integer kind = (Integer) o;
                // check valid
                if (!checkValid(kind)) {
                    return false;
                }
                // check unique
                if (!unique.add(kind)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkValid(Integer value) {
        return Objects.equals(value, BaseConstant.PERSON_KIND_ACTOR)
                || Objects.equals(value, BaseConstant.PERSON_KIND_DIRECTOR);
    }
}