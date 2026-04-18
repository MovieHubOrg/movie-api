package com.movie.api.form.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TestSendNotificationForm {
    private String title;
    private String body;
    private Integer type;
    private Integer targetType;
    private String targetValue;
    private Date scheduleAt;
}
