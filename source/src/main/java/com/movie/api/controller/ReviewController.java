package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.reaction.VoteDto;
import com.movie.api.dto.review.ReviewDto;
import com.movie.api.dto.review.ReviewNotificationDto;
import com.movie.api.dto.review.ReviewStatisticsDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.exception.UnauthorizationException;
import com.movie.api.form.ChangeStatusForm;
import com.movie.api.form.comment.UpdateToxicSpansForm;
import com.movie.api.form.reaction.CreateReactionForm;
import com.movie.api.form.review.CreateReviewForm;
import com.movie.api.form.review.UpdateReviewForm;
import com.movie.api.mapper.AccountMapper;
import com.movie.api.mapper.ReviewMapper;
import com.movie.api.service.CommentService;
import com.movie.api.service.MovieService;
import com.movie.api.service.NotificationService;
import com.movie.api.storage.criteria.ReviewCriteria;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.Reaction;
import com.movie.api.storage.model.Review;
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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserReportRepository userReportRepository;

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

        commentService.sendCommentToToxicDetector(review.getId(), review.getContent(), BaseConstant.TOXIC_DETECT_TYPE_REVIEW);

        ReviewStatisticsDto statistics = movieService.calculateReview(movie.getId(), review.getRate(), BaseConstant.ACTION_ADD);
        movieService.applyReviewRatingPreference(user.getId(), movie.getId(), review.getRate());
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
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), criteria.getSort());

//        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        Page<Review> reviews = reviewRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(reviews, reviewMapper::fromEntityToReviewDtoList), "Get list review success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_L')")
    public ApiMessageDto<ResponseListDto<List<ReviewDto>>> listAdmin(ReviewCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), criteria.getSort());

        Page<Review> reviews = reviewRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(reviews, reviewMapper::fromEntityToReviewDtoList), "Get list review success");
    }

    @Transactional
    @PatchMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateReviewForm form) {
        Review review = reviewRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));

        if (!Objects.equals(review.getAuthor().getId(), getCurrentUser())) {
            throw new UnauthorizationException("Not allow");
        }

        Integer oldRate = review.getRate();
        reviewMapper.fromUpdateReviewFormToEntity(form, review);
        reviewRepository.save(review);

        Integer newRate = review.getRate();
        if (!Objects.equals(oldRate, newRate)) {
            if (oldRate != null) {
                movieService.calculateReview(review.getMovieId(), oldRate, BaseConstant.ACTION_DELETE);
            }
            if (newRate != null) {
                movieService.calculateReview(review.getMovieId(), newRate, BaseConstant.ACTION_ADD);
            }
        }
        movieService.applyReviewRatingPreference(review.getAuthor().getId(), review.getMovieId(), newRate);
        return makeSuccessResponse("Update review success");
    }

    @Transactional
    @PatchMapping(value = "/vote", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_VOTE')")
    public ApiMessageDto<Void> vote(@Valid @RequestBody CreateReactionForm form) {
        Review review = reviewRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));

        Long userId = getCurrentUser();
        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[Account] not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        Reaction reaction = reactionRepository.findFirstByReviewIdAndUserId(review.getId(), userId).orElse(null);

        if (reaction == null) {
            reaction = new Reaction();
            reaction.setReviewId(review.getId());
            reaction.setUserId(userId);
            reaction.setType(form.getType());
            reactionRepository.save(reaction);
            increaseCounter(review.getId(), form.getType());
            if (!Objects.equals(review.getAuthor().getId(), userId)) {
                createVoteNotificationTemplate(review, user, form.getType());
            }
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

    private void createVoteNotificationTemplate(Review review, Account voter, Integer reactionType) {
        if (Objects.equals(review.getAuthor().getId(), voter.getId())) {
            return;
        }

        ReviewNotificationDto data = reviewMapper.entityToReviewNotificationDto(review);
        Movie movie = movieRepository.findById(review.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
        data.setMovieTitle(movie.getTitle());
        data.setMovieThumbnail(movie.getThumbnailUrl());
        data.setAuthor(accountMapper.entityToAccountNotificationDto(voter));
        data.setReactionType(reactionType);
        String title = Objects.equals(reactionType, BaseConstant.REACTION_TYPE_LIKE)
                ? String.format("%s đã thích đánh giá của bạn", voter.getFullName())
                : String.format("%s đã không thích đánh giá của bạn", voter.getFullName());
        notificationService.sendNotificationMessage(
                title,
                BaseConstant.CMD_VOTE_REVIEW,
                data,
                BaseConstant.NOTIFICATION_TYPE_COMMUNITY,
                BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT,
                String.valueOf(review.getAuthor().getId())
        );
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
        userReportRepository.deleteByTypeAndObjectId(BaseConstant.USER_REPORT_TYPE_REVIEW, review.getId());
        reviewRepository.delete(review);
        movieService.deleteReviewRatingPreference(review.getAuthor().getId(), review.getMovieId());

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
        Integer oldStatus = review.getStatus();
        if (Objects.equals(form.getStatus(), oldStatus)) {
            return makeSuccessResponse("Change status success");
        }
        review.setStatus(form.getStatus());
        reviewRepository.save(review);
        if (Objects.equals(oldStatus, BaseConstant.STATUS_ACTIVE) && Objects.equals(form.getStatus(), BaseConstant.STATUS_LOCK)) {
            commentService.sendToxicReviewNotification(review);
        } else if (Objects.equals(oldStatus, BaseConstant.STATUS_LOCK) && Objects.equals(form.getStatus(), BaseConstant.STATUS_ACTIVE)) {
            commentService.sendReviewUnlockedNotification(review);
        }
        return makeSuccessResponse("Change status success");
    }

    @Transactional
    @PutMapping(value = "/update-toxic-spans", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REV_U_T')")
    public ApiMessageDto<Void> updateToxicSpans(@Valid @RequestBody UpdateToxicSpansForm form) {
        Review review = reviewRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Review] Not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));
        review.setToxicSpans(form.getToxicSpans());
        reviewRepository.save(review);
        return makeSuccessResponse("Update toxic spans success");
    }

    @GetMapping(value = "/check/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ReviewDto> check(@PathVariable Long movieId) {
        Review review = reviewRepository.findByAuthorIdAndMovieId(getCurrentUser(), movieId).orElse(null);
        return makeSuccessResponse(reviewMapper.entityToReviewDto(review), "Get review success");
    }
}
