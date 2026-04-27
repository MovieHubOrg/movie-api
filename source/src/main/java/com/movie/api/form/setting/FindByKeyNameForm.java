package com.movie.api.form.setting;

import lombok.Data;

import java.util.List;

@Data
public class FindByKeyNameForm {
    private List<String> keyNames;
}
