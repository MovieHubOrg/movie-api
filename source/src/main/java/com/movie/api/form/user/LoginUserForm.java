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
public class LoginUserForm {
    @EmailConstraint
    @ApiModelProperty(required = true)
    private String email;

    @NotBlank(message = "password cannot be empty")
    @ApiModelProperty(name = "password", required = true)
    private String password;
}
