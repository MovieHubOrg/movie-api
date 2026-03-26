package com.movie.api.dto.review;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.LongToStringIfWebSerializer;
import com.movie.api.dto.account.AccountDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ReviewDto extends ABasicAdminDto {
    private AccountDto author;
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long movieId;
    private Integer rate;
    private String content;
    private Integer totalLike;
    private Integer totalDislike;
    private ReviewStatisticsDto statistics;
}
