package com.movie.api.dto.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OmdbRatingResponseDto {
    @JsonProperty("Response")
    private String response;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Error")
    private String error;
}
