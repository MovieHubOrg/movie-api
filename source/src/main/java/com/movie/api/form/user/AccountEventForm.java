package com.movie.api.form.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountEventForm {
    private Long id;
    private Integer kind;
    private String username;
    private String phone;
    private String email;
    private String fullName;
    private String avatarPath;
    private Integer gender;
    private Integer status;
}
