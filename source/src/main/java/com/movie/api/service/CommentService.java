package com.movie.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.comment.CommentNotificationDto;
import com.movie.api.dto.review.ReviewNotificationDto;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.comment.DetectorCommentForm;
import com.movie.api.form.comment.DoneDetectorCommentForm;
import com.movie.api.form.comment.ToxicSpanForm;
import com.movie.api.mapper.CommentMapper;
import com.movie.api.mapper.ReviewMapper;
import com.movie.api.service.rabbit.RabbitService;
import com.movie.api.storage.model.Comment;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.Review;
import com.movie.api.storage.repository.CommentRepository;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.storage.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CommentService {
    @Value("${rabbitmq.app}")
    private String appName;

    @Value("${rabbitmq.toxic.comment.detector.queue}")
    private String toxicCommentDetectorQueue;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    public void sendCommentToToxicDetector(Long objectId, String content, Integer type) {
        try {
            log.info("==> Sending comment to toxic detector: {} type : {}", objectId, type);
            DetectorCommentForm form = new DetectorCommentForm();
            form.setCommentId(objectId);
            form.setContent(content);
            form.setType(type);
            rabbitService.handleSendMsg(appName, toxicCommentDetectorQueue, form, BaseConstant.CMD_DETECTOR_COMMENT);
        } catch (Exception e) {
            log.error("Failed to send comment to toxic detector: {}", e.getMessage());
        }
    }

    @Transactional
    public void handleDoneDetectorComment(DoneDetectorCommentForm form) throws JsonProcessingException {
        if (form == null || form.getCommentId() == null) {
            log.warn("Skip done detector comment because payload is invalid");
            return;
        }

        List<ToxicSpanForm> toxicSpans = form.getToxicSpans();
        if (toxicSpans == null || toxicSpans.isEmpty()) {
            log.info("Comment {} has no toxic spans", form.getCommentId());
            return;
        }

        Comment comment = commentRepository.findById(form.getCommentId())
                .orElseThrow(() -> new NotFoundException("[Comment] not found"));
        comment.setStatus(BaseConstant.STATUS_LOCK);
        comment.setToxicSpans(objectMapper.writeValueAsString(toxicSpans));
        commentRepository.save(comment);

        sendToxicCommentNotification(comment);
    }

    public void sendToxicCommentNotification(Comment comment) {
        sendCommentStatusNotification(
                comment,
                "Bình luận của bạn đã bị ẩn do chứa nội dung không phù hợp",
                BaseConstant.CMD_TOXIC_COMMENT_LOCKED
        );
    }

    public void sendCommentUnlockedNotification(Comment comment) {
        sendCommentStatusNotification(
                comment,
                "Bình luận của bạn đã được xem xét lại và mở khóa",
                BaseConstant.CMD_COMMENT_UNLOCKED
        );
    }

    private void sendCommentStatusNotification(Comment comment, String title, String cmd) {
        Movie movie = movieRepository.findById(comment.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        CommentNotificationDto data = commentMapper.entityToCommentNotificationDto(comment);
        data.setMovieTitle(movie.getTitle());
        data.setMovieThumbnail(movie.getThumbnailUrl());

        notificationService.sendNotificationMessage(
                title,
                cmd,
                data,
                BaseConstant.NOTIFICATION_TYPE_COMMUNITY,
                BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT,
                String.valueOf(comment.getAuthor().getId())
        );
    }

    @Transactional
    public void handleDoneDetectorReview(DoneDetectorCommentForm form) throws JsonProcessingException {
        if (form == null || form.getCommentId() == null) {
            log.warn("Skip done detector review because payload is invalid");
            return;
        }

        List<ToxicSpanForm> toxicSpans = form.getToxicSpans();
        if (toxicSpans == null || toxicSpans.isEmpty()) {
            log.info("Review {} has no toxic spans", form.getCommentId());
            return;
        }

        Review review = reviewRepository.findById(form.getCommentId())
                .orElseThrow(() -> new NotFoundException("[Review] not found"));
        review.setStatus(BaseConstant.STATUS_LOCK);
        review.setToxicSpans(objectMapper.writeValueAsString(toxicSpans));
        reviewRepository.save(review);

        sendToxicReviewNotification(review);
    }

    public void sendToxicReviewNotification(Review review) {
        sendReviewStatusNotification(
                review,
                "Đánh giá của bạn đã bị ẩn do chứa nội dung không phù hợp",
                BaseConstant.CMD_TOXIC_REVIEW_LOCKED
        );
    }

    public void sendReviewUnlockedNotification(Review review) {
        sendReviewStatusNotification(
                review,
                "Đánh giá của bạn đã được xem xét lại và mở khóa",
                BaseConstant.CMD_REVIEW_UNLOCKED
        );
    }

    private void sendReviewStatusNotification(Review review, String title, String cmd) {
        Movie movie = movieRepository.findById(review.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        ReviewNotificationDto data = reviewMapper.entityToReviewNotificationDto(review);
        data.setMovieTitle(movie.getTitle());
        data.setMovieThumbnail(movie.getThumbnailUrl());

        notificationService.sendNotificationMessage(
                title,
                cmd,
                data,
                BaseConstant.NOTIFICATION_TYPE_COMMUNITY,
                BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT,
                String.valueOf(review.getAuthor().getId())
        );
    }
}
