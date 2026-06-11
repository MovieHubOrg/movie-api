package com.movie.api.dto.comment;

import com.movie.api.dto.account.AccountNotificationDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CommentNotificationDto {
    private String id;
    private String movieItemId;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private String content;
    private String parentId;
    private Integer reactionType;
    private AccountNotificationDto author;
    private String toxicSpans;
}
