package com.movie.api.validation;

import com.movie.api.validation.impl.RoomKindValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoomKindValidation.class)
@Documented
public @interface RoomKindConstraint {
    boolean allowNull() default false;

    String message() default "Room kind is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
