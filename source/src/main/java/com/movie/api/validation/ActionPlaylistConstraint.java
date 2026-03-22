package com.movie.api.validation;

import com.movie.api.validation.impl.ActionPlaylistValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ActionPlaylistValidation.class)
@Documented
public @interface ActionPlaylistConstraint {
    boolean allowNull() default false;

    String message() default "Action is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
