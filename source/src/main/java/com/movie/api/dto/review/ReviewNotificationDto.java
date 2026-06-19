package com.movie.api.dto.review;

import com.movie.api.dto.account.AccountNotificationDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ReviewNotificationDto {
    private String id;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private Integer rate;
    private String content;
    private Integer reactionType;
    private AccountNotificationDto author;
    private String toxicSpans;
}
