package com.movie.api.form.setting;

import lombok.Data;

import java.util.List;

@Data
public class FindByGroupNameForm {
    private List<String> groupNames;
}
