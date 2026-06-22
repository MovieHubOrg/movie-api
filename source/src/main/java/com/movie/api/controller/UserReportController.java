package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.userReport.UserReportDto;
import com.movie.api.dto.userReport.UserReportMetadataDto;
import com.movie.api.dto.userReport.UserReportNotificationDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.CreateUserReportForm;
import com.movie.api.mapper.UserReportMapper;
import com.movie.api.service.NotificationService;
import com.movie.api.storage.criteria.UserReportCriteria;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/user-report")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class UserReportController extends ABasicController {
    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private UserReportMapper userReportMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MovieRepository movieRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('URP_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateUserReportForm form) {
        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[Account] not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        String title = null;
        UserReportMetadataDto metadata = new UserReportMetadataDto();
        // Validate object existence and author kind for comment type
        if (Objects.equals(form.getType(), BaseConstant.USER_REPORT_TYPE_COMMENT)) {
            Comment comment = commentRepository.findById(form.getObjectId())
                    .orElseThrow(() -> new NotFoundException("[Comment] not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));
            Account author = comment.getAuthor();
            if (Objects.equals(author.getId(), user.getId())) {
                throw new BadRequestException("You cannot report your own comment");
            }
            if (!Objects.equals(author.getKind(), BaseConstant.ACCOUNT_KIND_USER)) {
                throw new BadRequestException("Only user comments can be reported");
            }
            title = String.format("%s đã báo cáo bình luận của %s", user.getFullName(), author.getFullName());

            Movie movie;
            if (comment.getMovieItem() != null) {
                movie = comment.getMovieItem().getMovie();
            } else {
                movie = movieRepository.findById(comment.getMovieId())
                        .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
            }
            if (comment.getParent() != null && comment.getParent().getId() != null) {
                metadata.setParentId(comment.getParent().getId().toString());
            }
            if (comment.getMovieItem() != null && comment.getMovieItem().getId() != null) {
                metadata.setMovieItemId(comment.getMovieItem().getId().toString());
            }
            if (movie != null && movie.getId() != null) {
                metadata.setMovieId(movie.getId().toString());
            }
            if (movie != null) {
                metadata.setMovieTitle(movie.getTitle());
                metadata.setMovieThumbnail(movie.getThumbnailUrl());
            }
        } else if (Objects.equals(form.getType(), BaseConstant.USER_REPORT_TYPE_REVIEW)) {
            Review review = reviewRepository.findById(form.getObjectId())
                    .orElseThrow(() -> new NotFoundException("[Review] not found", ErrorCode.REVIEW_ERROR_NOT_FOUND));
            Account author = review.getAuthor();
            if (Objects.equals(author.getId(), user.getId())) {
                throw new BadRequestException("You cannot report your own review");
            }
            title = String.format("%s đã báo cáo đánh giá của %s", user.getFullName(), author.getFullName());

            Movie movie = movieRepository.findById(review.getMovieId())
                    .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
            metadata.setMovieId(movie.getId().toString());
            metadata.setMovieTitle(movie.getTitle());
            metadata.setMovieThumbnail(movie.getThumbnailUrl());
        }

        if (userReportRepository.existsByUserIdAndObjectIdAndType(user.getId(), form.getObjectId(), form.getType())) {
            throw new BadRequestException("[UserReport] already existed", ErrorCode.USER_REPORT_ERROR_EXISTED);
        }

        UserReport report = userReportMapper.fromCreateFormToEntity(form);
        report.setUser(user);
        userReportRepository.save(report);

        if (title != null) {
            sendNotificationForUserReport(report, title, BaseConstant.CMD_NEW_USER_REPORT, metadata);
        }

        return makeSuccessResponse("Report success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('URP_V')")
    public ApiMessageDto<UserReportDto> get(@PathVariable Long id) {
        UserReport report = userReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[UserReport] Not found", ErrorCode.USER_REPORT_ERROR_NOT_FOUND));
        return makeSuccessResponse(userReportMapper.entityToDto(report), "Get user report success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('URP_L')")
    public ApiMessageDto<ResponseListDto<List<UserReportDto>>> list(UserReportCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createdDate")));
        Page<UserReport> page = userReportRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(page, userReportMapper::entityToDtoList), "Get list user report success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('URP_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        UserReport report = userReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[UserReport] Not found", ErrorCode.USER_REPORT_ERROR_NOT_FOUND));
        userReportRepository.delete(report);
        return makeSuccessResponse("Delete user report success");
    }

    private void sendNotificationForUserReport(UserReport userReport, String title, String cmd, UserReportMetadataDto metadata) {
        UserReportNotificationDto data = userReportMapper.entityToNotificationDto(userReport);
        userReportMapper.updateFromMetaDataToUserReportNotificationDto(metadata, data);
        notificationService.sendNotificationMessage(title, cmd, data, BaseConstant.NOTIFICATION_TYPE_COMMUNITY, BaseConstant.NOTIFICATION_TARGET_TYPE_APP, BaseConstant.APP_CMS);
    }
}
