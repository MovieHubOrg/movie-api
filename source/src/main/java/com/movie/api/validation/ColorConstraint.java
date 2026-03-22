package com.movie.api.validation;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.impl.ColorValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ColorValidation.class)
@Documented
public @interface ColorConstraint {
    boolean allowNull() default false;

    String pattern() default BaseConstant.COLOR_PATTERN;

    String message() default "Colors must be in hex format (e.g. #FFFFFF)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
