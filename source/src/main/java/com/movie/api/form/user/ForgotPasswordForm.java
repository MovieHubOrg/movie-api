package com.movie.api.form.user;

import com.movie.api.validation.EmailConstraint;
import com.movie.api.validation.PasswordConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class ForgotPasswordForm {
    @EmailConstraint
    @ApiModelProperty(required = true)
    private String email;

    @NotBlank
    @ApiModelProperty(required = true)
    private String otp;

    @PasswordConstraint
    @ApiModelProperty(required = true)
    private String password;

    @NotBlank
    @ApiModelProperty(required = true)
    private String confirmPassword;
}
