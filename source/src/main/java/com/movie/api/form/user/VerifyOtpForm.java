package com.movie.api.form.user;

import com.movie.api.validation.EmailConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class VerifyOtpForm {
    @EmailConstraint
    @ApiModelProperty(required = true)
    private String email;

    @NotBlank
    @ApiModelProperty(required = true)
    private String otp;
}
