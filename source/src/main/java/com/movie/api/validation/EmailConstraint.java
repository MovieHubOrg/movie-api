package com.movie.api.validation;

import com.movie.api.validation.impl.EmailValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidation.class)
@Documented
public @interface EmailConstraint {
    boolean allowNull() default false;
    String message() default "Email invalid format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
