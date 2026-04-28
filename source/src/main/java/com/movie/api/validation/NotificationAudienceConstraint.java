package com.movie.api.validation;

import com.movie.api.validation.impl.NotificationAudienceValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotificationAudienceValidation.class)
@Documented
public @interface NotificationAudienceConstraint {
    boolean allowNull() default false;

    String message() default "Notification audience is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
