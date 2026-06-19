package com.movie.api.validation;

import com.movie.api.validation.impl.UserReportTypeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserReportTypeValidation.class)
@Documented
public @interface UserReportTypeConstraint {
    boolean allowNull() default false;

    String message() default "User report type is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
