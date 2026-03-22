package com.movie.api.dto.comment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.LongToStringIfWebSerializer;
import com.movie.api.dto.movieItem.MovieItemDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CommentDto extends ABasicAdminDto {
    private MovieItemDto movieItem;
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long movieId;
    private String content;
    private Integer totalLike;
    private Integer totalDislike;
    private Integer totalChildren;
    private Boolean isPinned;
    private CommentDto parent;
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long authorId;
    private String authorInfo;
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long replyToId;
    private String replyToInfo;
}
