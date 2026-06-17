package com.movie.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.comment.CommentNotificationDto;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.comment.DetectorCommentForm;
import com.movie.api.form.comment.DoneDetectorCommentForm;
import com.movie.api.form.comment.ToxicSpanForm;
import com.movie.api.mapper.CommentMapper;
import com.movie.api.service.rabbit.RabbitService;
import com.movie.api.storage.model.Comment;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.repository.CommentRepository;
import com.movie.api.storage.repository.MovieRepository;
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

    public void sendCommentToToxicDetector(Comment comment) {
        try {
            log.info("==> Sending comment to toxic detector: {}", comment.getId());
            DetectorCommentForm form = new DetectorCommentForm();
            form.setCommentId(comment.getId());
            form.setContent(comment.getContent());
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

    private void sendToxicCommentNotification(Comment comment) {
        Movie movie = movieRepository.findById(comment.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        CommentNotificationDto data = commentMapper.entityToCommentNotificationDto(comment);
        data.setMovieTitle(movie.getTitle());
        data.setMovieThumbnail(movie.getThumbnailUrl());

        String title = "Bình luận của bạn đã bị ẩn do chứa nội dung không phù hợp";
        notificationService.sendNotificationMessage(
                title,
                BaseConstant.CMD_TOXIC_COMMENT_LOCKED,
                data,
                BaseConstant.NOTIFICATION_TYPE_COMMUNITY,
                BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT,
                String.valueOf(comment.getAuthor().getId())
        );
    }
}
