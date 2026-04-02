package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.reaction.VoteDto;
import com.movie.api.dto.review.ReviewDto;
import com.movie.api.dto.review.ReviewStatisticsDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.exception.UnauthorizationException;
import com.movie.api.form.ChangeStatusForm;
import com.movie.api.form.reaction.CreateReactionForm;
import com.movie.api.form.review.CreateReviewForm;
import com.movie.api.form.review.UpdateReviewForm;
import com.movie.api.mapper.ReviewMapper;
import com.movie.api.service.MovieService;
import com.movie.api.storage.criteria.ReviewCriteria;
import com.movie.api.storage.model.*;
import com.movie.api.storage.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/review")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ReviewController extends ABasicController {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private MovieService movieService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ReviewDto> create(@Valid @RequestBody CreateReviewForm form) {
        Movie movie = movieRepository.findById(form.getMovieId())
                .orElseThrow(() -> new BadRequestException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[User] not found", ErrorCode.USER_ERROR_NOT_FOUND));

        if (reviewRepository.existsByAuthorIdAndMovieId(user.getId(), movie.getId())) {
            throw new BadRequestException("[Review] already existed", ErrorCode.REVIEW_ERROR_EXISTED);
        }

        Review review = reviewMapper.fromCreateReviewFormToEntity(form);
        review.setAuthor(user);
        review.setMovieId(movie.getId());
        reviewRepository.save(review);

        ReviewStatisticsDto statistics = movieService.calculateReview(movie.getId(), review.getRate(), BaseConstant.ACTION_ADD);
        ReviewDto reviewDto = reviewMapper.entityToReviewDto(review);
        reviewDto.setStatistics(statistics);
        return makeSuccessResponse(reviewDto, "Create review success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_V')")
    public ApiMessageDto<ReviewDto> get(@PathVariable("id") Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));
        return makeSuccessResponse(reviewMapper.entityToReviewDto(review), "Get review success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ReviewDto>>> list(ReviewCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("rate"), Sort.Order.desc("createdDate")));

//        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        Page<Review> reviews = reviewRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(reviews, reviewMapper::fromEntityToReviewDtoList), "Get list review success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_L')")
    public ApiMessageDto<ResponseListDto<List<ReviewDto>>> listAdmin(ReviewCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("rate"), Sort.Order.desc("createdDate")));

        Page<Review> reviews = reviewRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(reviews, reviewMapper::fromEntityToReviewDtoList), "Get list review success");
    }

    @PatchMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateReviewForm form) {
        Review review = reviewRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));

        if (!Objects.equals(review.getAuthor().getId(), getCurrentUser())) {
            throw new UnauthorizationException("Not allow");
        }

        reviewMapper.fromUpdateReviewFormToEntity(form, review);
        reviewRepository.save(review);
        return makeSuccessResponse("Update review success");
    }

    @Transactional
    @PatchMapping(value = "/vote", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_VOTE')")
    public ApiMessageDto<Void> vote(@Valid @RequestBody CreateReactionForm form) {
        Review review = reviewRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));

        Long userId = getCurrentUser();
        Reaction reaction = reactionRepository.findFirstByReviewIdAndUserId(review.getId(), userId).orElse(null);

        if (reaction == null) {
            reaction = new Reaction();
            reaction.setReviewId(review.getId());
            reaction.setUserId(userId);
            reaction.setType(form.getType());
            reactionRepository.save(reaction);
            increaseCounter(review.getId(), form.getType());
        } else if (reaction.getType().equals(form.getType())) {
            reactionRepository.delete(reaction);
            decreaseCounter(review.getId(), form.getType());
        } else {
            increaseCounter(review.getId(), form.getType());
            decreaseCounter(review.getId(), reaction.getType());
            reaction.setType(form.getType());
            reactionRepository.save(reaction);
        }
        return makeSuccessResponse("Vote success");
    }

    private void increaseCounter(Long reviewId, Integer type) {
        if (Objects.equals(type, BaseConstant.REACTION_TYPE_LIKE)) {
            reviewRepository.increaseTotalLike(reviewId);
        } else {
            reviewRepository.increaseTotalDislike(reviewId);
        }
    }

    private void decreaseCounter(Long reviewId, Integer type) {
        if (Objects.equals(type, BaseConstant.REACTION_TYPE_LIKE)) {
            reviewRepository.decreaseTotalLike(reviewId);
        } else {
            reviewRepository.decreaseTotalDislike(reviewId);
        }
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_D')")
    public ApiMessageDto<ReviewStatisticsDto> delete(@PathVariable("id") Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));

        if (isUser() && !Objects.equals(review.getAuthor().getId(), getCurrentUser())) {
            throw new UnauthorizationException("Not allow");
        }

        reactionRepository.deleteByReviewId(review.getId());
        reviewRepository.delete(review);

        ReviewStatisticsDto statistics = movieService.calculateReview(review.getMovieId(), review.getRate(), BaseConstant.ACTION_DELETE);
        return makeSuccessResponse(statistics, "Delete review success");
    }

    @GetMapping(value = "/vote-list/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<VoteDto>> voteList(@PathVariable("movieId") Long movieId) {
        return makeSuccessResponse(reviewRepository.findVotesByMovieIdAndUserId(movieId, getCurrentUser()), "Get list vote success");
    }

    @Transactional
    @PutMapping(value = "/change-status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_C_S')")
    public ApiMessageDto<Void> changeStatus(@Valid @RequestBody ChangeStatusForm form) {
        Review review = reviewRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));
        review.setStatus(form.getStatus());
        reviewRepository.save(review);
        return makeSuccessResponse("Change status success");
    }

    @GetMapping(value = "/check/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ReviewDto> check(@PathVariable Long movieId) {
        Review review = reviewRepository.findByAuthorIdAndMovieId(getCurrentUser(), movieId).orElse(null);
        return makeSuccessResponse(reviewMapper.entityToReviewDto(review), "Get review success");
    }
}
