package com.movie.api.controller;

import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.recommendation.MovieSimilarityDto;
import com.movie.api.service.RecommendationDataService;
import com.movie.api.service.RecommendationPythonClient;
import com.movie.api.storage.model.MovieSimilarity;
import com.movie.api.storage.repository.MovieSimilarityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.stream.Collectors;

@ApiIgnore
@RestController
@RequestMapping("/v1/recommendation-admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RecommendationAdminController extends ABasicController {
    @Autowired
    private RecommendationDataService recommendationDataService;

    @Autowired
    private RecommendationPythonClient recommendationPythonClient;

    @Autowired
    private MovieSimilarityRepository movieSimilarityRepository;

    @PostMapping(value = "/rebuild-user-movie", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> rebuildUserMovie() {
        recommendationDataService.rebuildUserMovieData();
        return makeSuccessResponse("Rebuild user movie data success");
    }

    @PostMapping(value = "/rebuild-score", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> rebuildScore() {
        recommendationDataService.rebuildUserMovieScores();
        return makeSuccessResponse("Rebuild user movie score success");
    }

    @PostMapping(value = "/rebuild-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> rebuildAll() {
        recommendationDataService.rebuildUserMovieData();
        recommendationDataService.rebuildUserMovieScores();
        recommendationPythonClient.triggerItemKnnRebuild();
        recommendationPythonClient.triggerHybridRebuild();
        return makeSuccessResponse("Rebuild recommendation data success");
    }

    @GetMapping(value = "/movies/{movieId}/similar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<MovieSimilarityDto>> similarMovies(@PathVariable Long movieId,
                                                                 @RequestParam(value = "k", defaultValue = "20") Integer k) {
        int limit = k == null || k <= 0 ? 20 : Math.min(k, 100);
        List<MovieSimilarityDto> similarities = movieSimilarityRepository
                .findByMovieIdOrderByScoreDesc(movieId, PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return makeSuccessResponse(similarities, "List similar movies success");
    }

    private MovieSimilarityDto toDto(MovieSimilarity movieSimilarity) {
        MovieSimilarityDto dto = new MovieSimilarityDto();
        dto.setMovieId(movieSimilarity.getMovieId());
        dto.setSimilarMovieId(movieSimilarity.getSimilarMovieId());
        dto.setScore(movieSimilarity.getScore());
        dto.setReason(movieSimilarity.getReason());
        dto.setModelVersion(movieSimilarity.getModelVersion());
        return dto;
    }
}
