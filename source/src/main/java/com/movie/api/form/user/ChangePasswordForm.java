package com.movie.api.form.user;

import com.movie.api.validation.PasswordConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class ChangePasswordForm {
    @NotBlank(message = "oldPassword cannot be empty")
    @ApiModelProperty(required = true)
    private String oldPassword;

    @PasswordConstraint(message = "newPassword invalid format")
    @ApiModelProperty(required = true)
    private String newPassword;
}
