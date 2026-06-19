package com.movie.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.comment.CommentDto;
import com.movie.api.dto.comment.CommentNotificationDto;
import com.movie.api.dto.reaction.VoteDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.exception.UnauthorizationException;
import com.movie.api.form.ChangeStatusForm;
import com.movie.api.form.comment.CreateCommentForm;
import com.movie.api.form.comment.PinnedCommentForm;
import com.movie.api.form.comment.UpdateCommentForm;
import com.movie.api.form.comment.UpdateToxicSpansForm;
import com.movie.api.form.reaction.CreateReactionForm;
import com.movie.api.mapper.AccountMapper;
import com.movie.api.mapper.CommentMapper;
import com.movie.api.service.CommentService;
import com.movie.api.service.MovieService;
import com.movie.api.service.NotificationService;
import com.movie.api.storage.criteria.CommentCriteria;
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
@RequestMapping("/v1/comment")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CommentController extends ABasicController {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovieItemRepository movieItemRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommentService commentService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_C')")
    public ApiMessageDto<CommentDto> create(@Valid @RequestBody CreateCommentForm form) throws JsonProcessingException {
        Account author = accountRepository.findById(getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Account] not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        Comment comment = commentMapper.fromCreateCommentFormToEntity(form);
        comment.setAuthor(author);
        comment.setAuthorInfo(objectMapper.writeValueAsString(accountMapper.entityToAccountDto(author)));
        Movie movie;

        if (form.getMovieItemId() != null) {
            MovieItem movieItem = movieItemRepository.findById(form.getMovieItemId())
                    .orElseThrow(() -> new NotFoundException("[MovieItem] not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));
            comment.setMovieItem(movieItem);
            comment.setMovieId(movieItem.getMovie().getId());

            movie = movieItem.getMovie();
        } else {
            movie = movieRepository.findById(form.getMovieId())
                    .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
            comment.setMovieId(movie.getId());
        }

        if (form.getParentId() != null) {
            if (form.getReplyToId() == null) {
                throw new BadRequestException("[Comment] reply invalid", ErrorCode.COMMENT_ERROR_REPLY_INVALID);
            }
            Comment parent = commentRepository.findById(form.getParentId())
                    .orElseThrow(() -> new NotFoundException("[Comment] not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));
            if (parent.getParent() != null) {
                throw new BadRequestException("[Comment] parent invalid", ErrorCode.COMMENT_ERROR_PARENT_INVALID);
            }
            Account replyTo = accountRepository.findById(form.getReplyToId())
                    .orElseThrow(() -> new NotFoundException("[Account] not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
            commentRepository.increaseTotalChild(parent.getId());
            comment.setReplyTo(replyTo);
            comment.setReplyToInfo(objectMapper.writeValueAsString(accountMapper.entityToAccountDto(replyTo)));
            comment.setParent(parent);
        }

        commentRepository.save(comment);
        commentService.sendCommentToToxicDetector(comment.getId(), comment.getContent(), BaseConstant.TOXIC_DETECT_TYPE_COMMENT);
        movieService.calculateComment(comment.getMovieId(), BaseConstant.ACTION_ADD);
        if (comment.getReplyTo() != null && !Objects.equals(author.getId(), comment.getReplyTo().getId())) {
            createReplyNotificationTemplate(comment, author, comment.getReplyTo(), movie);
        }
        return makeSuccessResponse(commentMapper.entityToCommentDto(comment), "Create comment success");
    }

    private void createReplyNotificationTemplate(Comment comment, Account author, Account replyTo, Movie movie) {
        CommentNotificationDto data = commentMapper.entityToCommentNotificationDto(comment);
        data.setMovieTitle(movie.getTitle());
        data.setMovieThumbnail(movie.getThumbnailUrl());
        String title = String.format("%s đã trả lời bình luận của bạn", author.getFullName());
        notificationService.sendNotificationMessage(
                title,
                BaseConstant.CMD_REPLY_COMMENT,
                data,
                BaseConstant.NOTIFICATION_TYPE_COMMUNITY,
                BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT,
                String.valueOf(replyTo.getId())
        );
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_V')")
    public ApiMessageDto<CommentDto> get(@PathVariable("id") Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Comment] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));
        return makeSuccessResponse(commentMapper.entityToCommentDto(comment), "Get comment success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CommentDto>>> list(CommentCriteria criteria, Pageable pageable) {
        Sort.Direction dateDirection = (criteria.getParentId() == null) ? Sort.Direction.DESC : Sort.Direction.ASC;
        pageable = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("isPinned"), new Sort.Order(dateDirection, "createdDate")));
        criteria.setIsParent(criteria.getParentId() == null);
        Page<Comment> comments = commentRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(comments, commentMapper::fromEntityToCommentDtoList), "Get list comment success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_L')")
    public ApiMessageDto<ResponseListDto<List<CommentDto>>> listAdmin(CommentCriteria criteria, Pageable pageable) {
        Sort.Direction dateDirection = (criteria.getParentId() == null) ? Sort.Direction.DESC : Sort.Direction.ASC;
        pageable = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("isPinned"), new Sort.Order(dateDirection, "createdDate")));
        criteria.setIsParent(criteria.getParentId() == null);
        Page<Comment> comments = commentRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(comments, commentMapper::fromEntityToCommentDtoList), "Get list comment success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateCommentForm form) {
        Comment comment = commentRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Comment] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));

        if (comment.getAuthor().getId() != getCurrentUser()) {
            throw new UnauthorizationException("Not allow");
        }

        comment.setContent(form.getContent());
        commentRepository.save(comment);
        return makeSuccessResponse("Update comment success");
    }

    @PutMapping(value = "/pin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_PIN')")
    public ApiMessageDto<Void> pin(@Valid @RequestBody PinnedCommentForm form) {
        if (isUser()) {
            throw new UnauthorizationException("Not allow");
        }

        Comment comment = commentRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Comment] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));

        comment.setIsPinned(form.getIsPinned());
        commentRepository.save(comment);
        return makeSuccessResponse("Pin comment success");
    }

    @Transactional
    @PutMapping(value = "/vote", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_VOTE')")
    public ApiMessageDto<Void> vote(@Valid @RequestBody CreateReactionForm form) {
        Long userId = getCurrentUser();
        Comment comment = commentRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Comment] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));

        Reaction reaction = reactionRepository.findFirstByCommentIdAndUserId(comment.getId(), userId).orElse(null);

        if (reaction == null) {
            reaction = new Reaction();
            reaction.setCommentId(comment.getId());
            reaction.setUserId(userId);
            reaction.setType(form.getType());
            reactionRepository.save(reaction);
            increaseCounter(comment.getId(), form.getType());
            if (!Objects.equals(comment.getAuthor().getId(), userId)) {
                Account voter = accountRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("[Account] not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
                createVoteNotificationTemplate(comment, voter, form.getType());
            }
        } else if (reaction.getType().equals(form.getType())) {
            reactionRepository.delete(reaction);
            decreaseCounter(comment.getId(), form.getType());
        } else {
            increaseCounter(comment.getId(), form.getType());
            decreaseCounter(comment.getId(), reaction.getType());
            reaction.setType(form.getType());
            reactionRepository.save(reaction);
        }
        return makeSuccessResponse("Vote success");
    }

    private void createVoteNotificationTemplate(Comment comment, Account voter, Integer reactionType) {
        if (Objects.equals(comment.getAuthor().getId(), voter.getId())) {
            return;
        }

        Movie movie = movieRepository.findById(comment.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
        CommentNotificationDto data = commentMapper.entityToCommentNotificationDto(comment);
        data.setAuthor(accountMapper.entityToAccountNotificationDto(voter));
        data.setReactionType(reactionType);
        data.setMovieTitle(movie.getTitle());
        data.setMovieThumbnail(movie.getThumbnailUrl());
        String title = Objects.equals(reactionType, BaseConstant.REACTION_TYPE_LIKE)
                ? String.format("%s đã thích bình luận của bạn: \"%s\"", voter.getFullName(), comment.getContent())
                : String.format("%s đã không thích bình luận của bạn: \"%s\"", voter.getFullName(), comment.getContent());
        notificationService.sendNotificationMessage(
                title,
                BaseConstant.CMD_VOTE_COMMENT,
                data,
                BaseConstant.NOTIFICATION_TYPE_COMMUNITY,
                BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT,
                String.valueOf(comment.getAuthor().getId())
        );
    }

    @GetMapping(value = "/vote-list/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<VoteDto>> voteList(@PathVariable("movieId") Long movieId) {
        return makeSuccessResponse(commentRepository.findVotesByMovieIdAndUserId(movieId, getCurrentUser()), "Get list vote success");
    }

    private void increaseCounter(Long commentId, Integer type) {
        if (Objects.equals(type, BaseConstant.REACTION_TYPE_LIKE)) {
            commentRepository.increaseTotalLike(commentId);
        } else {
            commentRepository.increaseTotalDislike(commentId);
        }
    }

    private void decreaseCounter(Long commentId, Integer type) {
        if (Objects.equals(type, BaseConstant.REACTION_TYPE_LIKE)) {
            commentRepository.decreaseTotalLike(commentId);
        } else {
            commentRepository.decreaseTotalDislike(commentId);
        }
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Comment] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));

        if (isUser() && comment.getAuthor().getId() != getCurrentUser()) {
            throw new UnauthorizationException("Not allow");
        }

        if (comment.getParent() != null) {
            commentRepository.decreaseTotalChild(comment.getParent().getId());
        } else {
            commentRepository.deleteByParentId(comment.getId());
        }
        movieService.calculateComment(comment.getMovieId(), BaseConstant.ACTION_DELETE);
        reactionRepository.deleteByCommentId(comment.getId());
        userReportRepository.deleteByTypeAndObjectId(BaseConstant.USER_REPORT_TYPE_COMMENT, comment.getId());
        commentRepository.delete(comment);
        return makeSuccessResponse("Delete comment success");
    }

    @Transactional
    @PutMapping(value = "/change-status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_D')")
    public ApiMessageDto<Void> changeStatus(@Valid @RequestBody ChangeStatusForm form) {
        Comment comment = commentRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Comment] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));
        if (comment.getParent() == null) {
            commentRepository.updateStatusByParentId(comment.getId(), form.getStatus());
        }
        comment.setStatus(form.getStatus());
        commentRepository.save(comment);
        return makeSuccessResponse("Change status success");
    }

    @Transactional
    @PutMapping(value = "/update-toxic-spans", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CMT_U_T')")
    public ApiMessageDto<Void> updateToxicSpans(@Valid @RequestBody UpdateToxicSpansForm form) {
        Comment comment = commentRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Comment] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));
        comment.setToxicSpans(form.getToxicSpans());
        commentRepository.save(comment);
        return makeSuccessResponse("Update toxic spans success");
    }
}
