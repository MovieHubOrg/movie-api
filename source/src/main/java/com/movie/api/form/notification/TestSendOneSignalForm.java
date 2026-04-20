package com.movie.api.form.notification;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestSendOneSignalForm {
    @NotBlank
    private String title;
    @NotBlank
    private String body;
    @NotEmpty
    @Size(max = 20000)
    private List<String> accountIds;
    private String data;
    private String largeIcon;
    private String smallIcon;
    private String bigPicture;
}
