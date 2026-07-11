package com.movie.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.account.AccountDto;
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
import com.movie.api.jwt.BaseJwt;
import com.movie.api.mapper.AccountMapper;
import com.movie.api.mapper.CommentMapper;
import com.movie.api.service.CommentService;
import com.movie.api.service.MovieService;
import com.movie.api.service.NotificationService;
import com.movie.api.service.impl.UserServiceImpl;
import com.movie.api.storage.criteria.CommentCriteria;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.model.Comment;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.MovieItem;
import com.movie.api.storage.model.Reaction;
import com.movie.api.storage.repository.AccountRepository;
import com.movie.api.storage.repository.CommentRepository;
import com.movie.api.storage.repository.MovieItemRepository;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.storage.repository.ReactionRepository;
import com.movie.api.storage.repository.UserReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovieItemRepository movieItemRepository;

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private UserReportRepository userReportRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieService movieService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommentService commentService;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private CommentController commentController;

    private BaseJwt jwtFor(long accountId) {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(accountId);
        return jwt;
    }

    // ---------- create ----------

    @Test
    void create_whenAuthorNotFound_throwsNotFoundException() {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(1L);
        form.setContent("hello");

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenMovieItemIdProvidedButNotFound_throwsNotFoundException() {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieItemId(5L);
        form.setContent("hello");

        Account author = new Account();
        author.setId(1L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(new Comment());
        when(accountMapper.entityToAccountDto(author)).thenReturn(new AccountDto());
        when(movieItemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenMovieIdProvidedButNotFound_throwsNotFoundException() {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(9L);
        form.setContent("hello");

        Account author = new Account();
        author.setId(1L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(new Comment());
        when(accountMapper.entityToAccountDto(author)).thenReturn(new AccountDto());
        when(movieRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenReplyToIdMissingWithParentId_throwsBadRequestException() {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(9L);
        form.setContent("hello");
        form.setParentId(3L);

        Account author = new Account();
        author.setId(1L);
        Movie movie = new Movie();
        movie.setId(9L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(new Comment());
        when(accountMapper.entityToAccountDto(author)).thenReturn(new AccountDto());
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));

        assertThatThrownBy(() -> commentController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_REPLY_INVALID);
    }

    @Test
    void create_whenParentCommentNotFound_throwsNotFoundException() {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(9L);
        form.setContent("hello");
        form.setParentId(3L);
        form.setReplyToId(4L);

        Account author = new Account();
        author.setId(1L);
        Movie movie = new Movie();
        movie.setId(9L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(new Comment());
        when(accountMapper.entityToAccountDto(author)).thenReturn(new AccountDto());
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(commentRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenParentIsAlreadyAReply_throwsBadRequestException() {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(9L);
        form.setContent("hello");
        form.setParentId(3L);
        form.setReplyToId(4L);

        Account author = new Account();
        author.setId(1L);
        Movie movie = new Movie();
        movie.setId(9L);
        Comment parent = new Comment();
        parent.setId(3L);
        parent.setParent(new Comment());

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(new Comment());
        when(accountMapper.entityToAccountDto(author)).thenReturn(new AccountDto());
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(commentRepository.findById(3L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> commentController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_PARENT_INVALID);
    }

    @Test
    void create_whenReplyToAccountNotFound_throwsNotFoundException() {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(9L);
        form.setContent("hello");
        form.setParentId(3L);
        form.setReplyToId(4L);

        Account author = new Account();
        author.setId(1L);
        Movie movie = new Movie();
        movie.setId(9L);
        Comment parent = new Comment();
        parent.setId(3L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(new Comment());
        when(accountMapper.entityToAccountDto(author)).thenReturn(new AccountDto());
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(commentRepository.findById(3L)).thenReturn(Optional.of(parent));
        when(accountRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenTopLevelCommentOnMovie_savesAndSendsToDetector() throws Exception {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(9L);
        form.setContent("hello");

        Account author = new Account();
        author.setId(1L);
        Movie movie = new Movie();
        movie.setId(9L);
        Comment mappedComment = new Comment();

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(mappedComment);
        when(accountMapper.entityToAccountDto(author)).thenReturn(new AccountDto());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(commentMapper.entityToCommentDto(mappedComment)).thenReturn(new CommentDto());

        ApiMessageDto<CommentDto> response = commentController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(commentRepository, times(1)).save(mappedComment);
        verify(commentService, times(1)).sendCommentToToxicDetector(any(), eq("hello"), eq(BaseConstant.TOXIC_DETECT_TYPE_COMMENT), any());
        verify(movieService, times(1)).calculateComment(9L, BaseConstant.ACTION_ADD);
        verify(notificationService, never()).sendNotificationMessage(any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_whenReplyToOtherUser_sendsReplyNotification() throws Exception {
        CreateCommentForm form = new CreateCommentForm();
        form.setMovieId(9L);
        form.setContent("hello");
        form.setParentId(3L);
        form.setReplyToId(4L);

        Account author = new Account();
        author.setId(1L);
        author.setFullName("Author");
        Movie movie = new Movie();
        movie.setId(9L);
        movie.setTitle("Movie Title");
        Comment parent = new Comment();
        parent.setId(3L);
        Account replyTo = new Account();
        replyTo.setId(4L);
        Comment mappedComment = new Comment();

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateCommentFormToEntity(form)).thenReturn(mappedComment);
        when(accountMapper.entityToAccountDto(any(Account.class))).thenReturn(new AccountDto());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(commentRepository.findById(3L)).thenReturn(Optional.of(parent));
        when(accountRepository.findById(4L)).thenReturn(Optional.of(replyTo));
        when(commentMapper.entityToCommentNotificationDto(mappedComment)).thenReturn(new CommentNotificationDto());
        when(commentMapper.entityToCommentDto(mappedComment)).thenReturn(new CommentDto());

        ApiMessageDto<CommentDto> response = commentController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(commentRepository, times(1)).increaseTotalChild(3L);
        verify(notificationService, times(1)).sendNotificationMessage(
                any(), eq(BaseConstant.CMD_REPLY_COMMENT), any(), eq(BaseConstant.NOTIFICATION_TYPE_COMMUNITY),
                eq(BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT), eq("4"));
    }

    // ---------- get ----------

    @Test
    void get_whenFound_returnsSuccessResponse() {
        Comment comment = new Comment();
        comment.setId(1L);
        CommentDto dto = new CommentDto();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentMapper.entityToCommentDto(comment)).thenReturn(dto);

        ApiMessageDto<CommentDto> response = commentController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    // ---------- list / listAdmin ----------

    @Test
    void list_returnsSuccessResponseWrappingList() {
        CommentCriteria criteria = new CommentCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment = new Comment();
        comment.setId(1L);
        Page<Comment> page = new PageImpl<>(Collections.singletonList(comment));
        List<CommentDto> dtoList = Collections.singletonList(new CommentDto());

        when(commentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(commentMapper.fromEntityToCommentDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CommentDto>>> response = commentController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getIsParent()).isTrue();
    }

    @Test
    void listAdmin_returnsSuccessResponseWrappingList() {
        CommentCriteria criteria = new CommentCriteria();
        criteria.setParentId(5L);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> page = new PageImpl<>(Collections.emptyList());

        when(commentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(commentMapper.fromEntityToCommentDtoList(page.getContent())).thenReturn(Collections.emptyList());

        ApiMessageDto<ResponseListDto<List<CommentDto>>> response = commentController.listAdmin(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(criteria.getIsParent()).isFalse();
    }

    // ---------- update ----------

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdateCommentForm form = new UpdateCommentForm();
        form.setId(99L);
        form.setContent("new content");

        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenNotAuthor_throwsUnauthorizationException() {
        UpdateCommentForm form = new UpdateCommentForm();
        form.setId(1L);
        form.setContent("new content");

        Account author = new Account();
        author.setId(2L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));

        assertThatThrownBy(() -> commentController.update(form))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void update_whenContentUnchanged_doesNotReScanAndReturnsSuccess() {
        UpdateCommentForm form = new UpdateCommentForm();
        form.setId(1L);
        form.setContent("same content");

        Account author = new Account();
        author.setId(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setContent("same content");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));

        ApiMessageDto<Void> response = commentController.update(form);

        assertThat(response.getResult()).isTrue();
        verify(commentRepository, never()).save(any());
        verify(commentService, never()).sendCommentToToxicDetector(any(), any(), any(), any());
    }

    @Test
    void update_whenContentChangedAndNotLocked_setsStatusPendingAndReScans() {
        UpdateCommentForm form = new UpdateCommentForm();
        form.setId(1L);
        form.setContent("new content");

        Account author = new Account();
        author.setId(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setContent("old content");
        comment.setStatus(BaseConstant.STATUS_ACTIVE);
        comment.setDetectVersion(1);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));

        ApiMessageDto<Void> response = commentController.update(form);

        assertThat(response.getResult()).isTrue();
        assertThat(comment.getStatus()).isEqualTo(BaseConstant.STATUS_PENDING);
        assertThat(comment.getToxicSpans()).isNull();
        assertThat(comment.getDetectVersion()).isEqualTo(2L);
        verify(commentRepository, times(1)).save(comment);
        verify(commentService, times(1)).sendCommentToToxicDetector(eq(1L), eq("new content"), eq(BaseConstant.TOXIC_DETECT_TYPE_COMMENT), eq(2));
    }

    @Test
    void update_whenContentChangedAndLocked_keepsLockStatus() {
        UpdateCommentForm form = new UpdateCommentForm();
        form.setId(1L);
        form.setContent("new content");

        Account author = new Account();
        author.setId(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setContent("old content");
        comment.setStatus(BaseConstant.STATUS_LOCK);
        comment.setDetectVersion(1);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));

        commentController.update(form);

        assertThat(comment.getStatus()).isEqualTo(BaseConstant.STATUS_LOCK);
    }

    // ---------- pin ----------

    @Test
    void pin_whenIsUser_throwsUnauthorizationException() {
        PinnedCommentForm form = new PinnedCommentForm();
        form.setId(1L);
        form.setIsPinned(true);

        BaseJwt jwt = jwtFor(1L);
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_USER);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);

        assertThatThrownBy(() -> commentController.pin(form))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void pin_whenNotFound_throwsNotFoundException() {
        PinnedCommentForm form = new PinnedCommentForm();
        form.setId(99L);
        form.setIsPinned(true);

        BaseJwt jwt = jwtFor(1L);
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_ADMIN);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.pin(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    @Test
    void pin_whenAdminAndFound_pinsCommentSuccessfully() {
        PinnedCommentForm form = new PinnedCommentForm();
        form.setId(1L);
        form.setIsPinned(true);

        Comment comment = new Comment();
        comment.setId(1L);

        BaseJwt jwt = jwtFor(1L);
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_ADMIN);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ApiMessageDto<Void> response = commentController.pin(form);

        assertThat(response.getResult()).isTrue();
        assertThat(comment.getIsPinned()).isTrue();
        verify(commentRepository, times(1)).save(comment);
    }

    // ---------- vote ----------

    @Test
    void vote_whenCommentNotFound_throwsNotFoundException() {
        CreateReactionForm form = new CreateReactionForm();
        form.setId(99L);
        form.setType(BaseConstant.REACTION_TYPE_LIKE);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.vote(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    @Test
    void vote_whenNewLikeFromDifferentUser_increasesCounterAndNotifies() {
        CreateReactionForm form = new CreateReactionForm();
        form.setId(1L);
        form.setType(BaseConstant.REACTION_TYPE_LIKE);

        Account author = new Account();
        author.setId(2L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setMovieId(9L);
        Movie movie = new Movie();
        movie.setId(9L);
        Account voter = new Account();
        voter.setId(1L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(reactionRepository.findFirstByCommentIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(voter));
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(commentMapper.entityToCommentNotificationDto(comment)).thenReturn(new CommentNotificationDto());
        when(accountMapper.entityToAccountNotificationDto(voter)).thenReturn(new com.movie.api.dto.account.AccountNotificationDto());

        ApiMessageDto<Void> response = commentController.vote(form);

        assertThat(response.getResult()).isTrue();
        verify(reactionRepository, times(1)).save(any(Reaction.class));
        verify(commentRepository, times(1)).increaseTotalLike(1L);
        verify(notificationService, times(1)).sendNotificationMessage(
                any(), eq(BaseConstant.CMD_VOTE_COMMENT), any(), any(), any(), eq("2"));
    }

    @Test
    void vote_whenNewLikeFromCommentAuthor_doesNotNotify() {
        CreateReactionForm form = new CreateReactionForm();
        form.setId(1L);
        form.setType(BaseConstant.REACTION_TYPE_LIKE);

        Account author = new Account();
        author.setId(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setMovieId(9L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(reactionRepository.findFirstByCommentIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        ApiMessageDto<Void> response = commentController.vote(form);

        assertThat(response.getResult()).isTrue();
        verify(commentRepository, times(1)).increaseTotalLike(1L);
        verify(notificationService, never()).sendNotificationMessage(any(), any(), any(), any(), any(), any());
    }

    @Test
    void vote_whenSameReactionExists_removesReactionAndDecreasesCounter() {
        CreateReactionForm form = new CreateReactionForm();
        form.setId(1L);
        form.setType(BaseConstant.REACTION_TYPE_LIKE);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(new Account());
        Reaction reaction = new Reaction();
        reaction.setId(5L);
        reaction.setType(BaseConstant.REACTION_TYPE_LIKE);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(reactionRepository.findFirstByCommentIdAndUserId(1L, 1L)).thenReturn(Optional.of(reaction));

        ApiMessageDto<Void> response = commentController.vote(form);

        assertThat(response.getResult()).isTrue();
        verify(reactionRepository, times(1)).delete(reaction);
        verify(commentRepository, times(1)).decreaseTotalLike(1L);
    }

    @Test
    void vote_whenSwitchingReactionType_updatesCountersAndSavesReaction() {
        CreateReactionForm form = new CreateReactionForm();
        form.setId(1L);
        form.setType(BaseConstant.REACTION_TYPE_DISLIKE);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(new Account());
        Reaction reaction = new Reaction();
        reaction.setId(5L);
        reaction.setType(BaseConstant.REACTION_TYPE_LIKE);

        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(reactionRepository.findFirstByCommentIdAndUserId(1L, 1L)).thenReturn(Optional.of(reaction));

        ApiMessageDto<Void> response = commentController.vote(form);

        assertThat(response.getResult()).isTrue();
        verify(commentRepository, times(1)).increaseTotalDislike(1L);
        verify(commentRepository, times(1)).decreaseTotalLike(1L);
        assertThat(reaction.getType()).isEqualTo(BaseConstant.REACTION_TYPE_DISLIKE);
        verify(reactionRepository, times(1)).save(reaction);
    }

    // ---------- voteList ----------

    @Test
    void voteList_returnsSuccessResponse() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));
        List<VoteDto> votes = Collections.singletonList(new VoteDto(1L, 1));
        when(commentRepository.findVotesByMovieIdAndUserId(9L, 1L)).thenReturn(votes);

        ApiMessageDto<List<VoteDto>> response = commentController.voteList(9L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(votes);
    }

    // ---------- delete ----------

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenUserAndNotOwner_throwsUnauthorizationException() {
        Account author = new Account();
        author.setId(2L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);

        BaseJwt jwt = jwtFor(1L);
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_USER);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userService.getAddInfoFromToken()).thenReturn(jwt);

        assertThatThrownBy(() -> commentController.delete(1L))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void delete_whenTopLevelComment_deletesChildrenAndComment() {
        Account author = new Account();
        author.setId(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setMovieId(9L);

        BaseJwt jwt = jwtFor(1L);
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_USER);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userService.getAddInfoFromToken()).thenReturn(jwt);

        ApiMessageDto<Void> response = commentController.delete(1L);

        assertThat(response.getResult()).isTrue();
        verify(commentRepository, times(1)).deleteByParentId(1L);
        verify(commentRepository, never()).decreaseTotalChild(any());
        verify(movieService, times(1)).calculateComment(9L, BaseConstant.ACTION_DELETE);
        verify(reactionRepository, times(1)).deleteByCommentId(1L);
        verify(userReportRepository, times(1)).deleteByTypeAndObjectId(BaseConstant.USER_REPORT_TYPE_COMMENT, 1L);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void delete_whenReplyComment_decreasesParentChildCount() {
        Account author = new Account();
        author.setId(1L);
        Comment parent = new Comment();
        parent.setId(2L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setMovieId(9L);
        comment.setParent(parent);

        BaseJwt jwt = jwtFor(1L);
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_ADMIN);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userService.getAddInfoFromToken()).thenReturn(jwt);

        commentController.delete(1L);

        verify(commentRepository, times(1)).decreaseTotalChild(2L);
        verify(commentRepository, never()).deleteByParentId(any());
    }

    // ---------- changeStatus ----------

    @Test
    void changeStatus_whenNotFound_throwsNotFoundException() {
        ChangeStatusForm form = new ChangeStatusForm();
        form.setId(99L);
        form.setStatus(BaseConstant.STATUS_LOCK);

        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.changeStatus(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    @Test
    void changeStatus_whenStatusUnchanged_returnsSuccessWithoutSaving() {
        ChangeStatusForm form = new ChangeStatusForm();
        form.setId(1L);
        form.setStatus(BaseConstant.STATUS_ACTIVE);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setStatus(BaseConstant.STATUS_ACTIVE);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ApiMessageDto<Void> response = commentController.changeStatus(form);

        assertThat(response.getResult()).isTrue();
        verify(commentRepository, never()).save(any());
    }

    @Test
    void changeStatus_whenActiveToLock_sendsToxicNotification() {
        ChangeStatusForm form = new ChangeStatusForm();
        form.setId(1L);
        form.setStatus(BaseConstant.STATUS_LOCK);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setStatus(BaseConstant.STATUS_ACTIVE);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ApiMessageDto<Void> response = commentController.changeStatus(form);

        assertThat(response.getResult()).isTrue();
        assertThat(comment.getStatus()).isEqualTo(BaseConstant.STATUS_LOCK);
        verify(commentRepository, times(1)).save(comment);
        verify(commentService, times(1)).sendToxicCommentNotification(comment);
        verify(commentService, never()).sendCommentUnlockedNotification(any());
    }

    @Test
    void changeStatus_whenLockToActive_sendsUnlockedNotification() {
        ChangeStatusForm form = new ChangeStatusForm();
        form.setId(1L);
        form.setStatus(BaseConstant.STATUS_ACTIVE);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setStatus(BaseConstant.STATUS_LOCK);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ApiMessageDto<Void> response = commentController.changeStatus(form);

        assertThat(response.getResult()).isTrue();
        verify(commentService, times(1)).sendCommentUnlockedNotification(comment);
        verify(commentService, never()).sendToxicCommentNotification(any());
    }

    // ---------- updateToxicSpans ----------

    @Test
    void updateToxicSpans_whenNotFound_throwsNotFoundException() {
        UpdateToxicSpansForm form = new UpdateToxicSpansForm();
        form.setId(99L);

        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentController.updateToxicSpans(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COMMENT_ERROR_NOT_FOUND);
    }

    @Test
    void updateToxicSpans_whenFound_updatesAndSaves() {
        UpdateToxicSpansForm form = new UpdateToxicSpansForm();
        form.setId(1L);
        form.setToxicSpans("[{\"start\":0,\"end\":3}]");

        Comment comment = new Comment();
        comment.setId(1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ApiMessageDto<Void> response = commentController.updateToxicSpans(form);

        assertThat(response.getResult()).isTrue();
        assertThat(comment.getToxicSpans()).isEqualTo("[{\"start\":0,\"end\":3}]");
        verify(commentRepository, times(1)).save(comment);
    }
}
