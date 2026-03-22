package com.movie.api.validation;

import com.movie.api.validation.impl.PersonKindValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PersonKindValidation.class)
@Documented
public @interface PersonKindConstraint {
    boolean allowNull() default false;

    String message() default "Person kind is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
