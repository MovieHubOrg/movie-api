package com.movie.api.form.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SendNotificationForm {
    private String title;
    private String body;
    private Integer type;
    private Integer targetType;
    private String targetValue;
    private List<Long> accountIds;
}
