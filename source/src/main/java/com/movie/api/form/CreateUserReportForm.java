package com.movie.api.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import com.movie.api.validation.UserReportTypeConstraint;

@Getter
@Setter
public class CreateUserReportForm {
    @NotNull
    private Long objectId;

    @UserReportTypeConstraint
    private Integer type; // 1: comment, 2: review

    private String content;
}
