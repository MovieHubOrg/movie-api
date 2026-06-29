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
import com.movie.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private SettingCacheService settingCacheService;

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

        Comment comment = commentRepository.findById(form.getCommentId())
                .orElseThrow(() -> new NotFoundException("[Comment] not found"));

        List<ToxicSpanForm> toxicSpans = applyToxicKeywordSettings(form.getToxicSpans(), comment.getContent());
        if (toxicSpans.isEmpty()) {
            log.info("Comment {} has no toxic spans after keyword filtering", form.getCommentId());
            return;
        }

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

        Review review = reviewRepository.findById(form.getCommentId())
                .orElseThrow(() -> new NotFoundException("[Review] not found"));

        List<ToxicSpanForm> toxicSpans = applyToxicKeywordSettings(form.getToxicSpans(), review.getContent());
        if (toxicSpans.isEmpty()) {
            log.info("Review {} has no toxic spans after keyword filtering", form.getCommentId());
            return;
        }

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

    private List<ToxicSpanForm> applyToxicKeywordSettings(List<ToxicSpanForm> toxicSpans, String content) {
        if (StringUtils.isNullOrEmpty(content)) return new ArrayList<>();
        Set<String> allowKeywords = getKeywordSet(BaseConstant.SETTING_KEY_ALLOW_TOXIC_KEYWORDS);
        Set<String> blacklistKeywords = getKeywordSet(BaseConstant.SETTING_KEY_BLACKLIST_TOXIC_KEYWORDS);

        List<ToxicSpanForm> candidates = new ArrayList<>();

        // Keep detector spans whose text is not in the allow list
        if (toxicSpans != null) {
            for (ToxicSpanForm span : toxicSpans) {
                String spanText = extractSpanText(content, span);
                if (!spanText.isEmpty() && !allowKeywords.contains(spanText)) {
                    candidates.add(span);
                }
            }
        }

        // Add all occurrences of every blacklist keyword found in content
        String lowerContent = content.toLowerCase();
        for (String keyword : blacklistKeywords) {
            int idx = 0;
            while ((idx = lowerContent.indexOf(keyword, idx)) != -1) {
                int end = idx + keyword.length();
                ToxicSpanForm newSpan = new ToxicSpanForm();
                newSpan.setStart(idx);
                newSpan.setEnd(end);
                candidates.add(newSpan);
                idx = end;
            }
        }

        // Sort and merge to eliminate duplicates and overlapping spans
        return mergeSpans(candidates);
    }

    /**
     * Sorts spans by start index, then merges any that overlap.
     * Adjacent spans [a,b) [b,c) are kept separate — only true overlaps are merged.
     * end is exclusive: span covers content[start..end-1].
     */
    private List<ToxicSpanForm> mergeSpans(List<ToxicSpanForm> spans) {
        List<ToxicSpanForm> valid = spans.stream()
                .filter(s -> s.getStart() != null && s.getEnd() != null && s.getStart() < s.getEnd())
                .sorted(Comparator.comparingInt(ToxicSpanForm::getStart).thenComparingInt(ToxicSpanForm::getEnd))
                .collect(Collectors.toList());

        if (valid.isEmpty()) return valid;

        List<ToxicSpanForm> merged = new ArrayList<>();
        int curStart = valid.get(0).getStart();
        int curEnd = valid.get(0).getEnd();

        for (int i = 1; i < valid.size(); i++) {
            ToxicSpanForm next = valid.get(i);
            if (next.getStart() < curEnd) {
                // Overlapping — extend current end
                curEnd = Math.max(curEnd, next.getEnd());
            } else {
                ToxicSpanForm span = new ToxicSpanForm();
                span.setStart(curStart);
                span.setEnd(curEnd);
                merged.add(span);
                curStart = next.getStart();
                curEnd = next.getEnd();
            }
        }
        ToxicSpanForm last = new ToxicSpanForm();
        last.setStart(curStart);
        last.setEnd(curEnd);
        merged.add(last);

        return merged;
    }

    private String extractSpanText(String content, ToxicSpanForm span) {
        if (span.getStart() == null || span.getEnd() == null) return "";
        int start = Math.max(0, span.getStart());
        int end = Math.min(content.length(), span.getEnd());
        if (start >= end) return "";
        return content.substring(start, end).toLowerCase();
    }

    private Set<String> getKeywordSet(String settingKey) {
        try {
            String value = settingCacheService.getValue(settingKey);
            if (value == null || value.isBlank()) return Set.of();
            return Arrays.stream(value.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } catch (NotFoundException e) {
            log.warn("Setting not found: {}", settingKey);
            return Set.of();
        }
    }
}
