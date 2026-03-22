package com.movie.api.form.user;

import com.movie.api.validation.EmailConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class RequestForgotPasswordForm {
    @EmailConstraint
    @ApiModelProperty(required = true)
    private String email;
}
