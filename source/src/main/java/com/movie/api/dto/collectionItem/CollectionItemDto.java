package com.movie.api.dto.collectionItem;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.LongToStringIfWebSerializer;
import com.movie.api.dto.movie.MovieDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CollectionItemDto extends ABasicAdminDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long collectionId;
    private MovieDto movie;
    private Integer ordering;
}
