package com.movie.api.validation;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.impl.UsernameValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidation.class)
@Documented
public @interface UsernameConstraint {
    boolean allowNull() default false;

    String pattern() default BaseConstant.USERNAME_PATTERN;

    String message() default "Username is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
