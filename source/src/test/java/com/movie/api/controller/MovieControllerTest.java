package com.movie.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.movie.ImdbRatingsSyncDto;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.movie.RecentWatchedCategoryRecommendationDto;
import com.movie.api.dto.movie.SuggestByWatchedDto;
import com.movie.api.dto.movieItem.MovieItemDto;
import com.movie.api.dto.watchHistory.WatchHistoryDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.movie.CreateMovieForm;
import com.movie.api.form.movie.MakeSurveyForm;
import com.movie.api.form.movie.UpdateMovieForm;
import com.movie.api.form.notification.SendNotificationConfigForm;
import com.movie.api.jwt.BaseJwt;
import com.movie.api.mapper.MovieItemMapper;
import com.movie.api.mapper.MovieMapper;
import com.movie.api.mapper.WatchHistoryMapper;
import com.movie.api.service.ImdbService;
import com.movie.api.service.MediaService;
import com.movie.api.service.MovieService;
import com.movie.api.service.NotificationService;
import com.movie.api.service.feign.FeignAccountAuthService;
import com.movie.api.service.impl.UserServiceImpl;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.criteria.MovieCriteria;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.model.Category;
import com.movie.api.storage.model.Collection;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.MovieItem;
import com.movie.api.storage.model.UserMovie;
import com.movie.api.storage.model.WatchHistory;
import com.movie.api.storage.repository.AccountRepository;
import com.movie.api.storage.repository.CategoryRepository;
import com.movie.api.storage.repository.CollectionRepository;
import com.movie.api.storage.repository.MovieItemRepository;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.storage.repository.UserMovieRepository;
import com.movie.api.storage.repository.WatchHistoryRepository;
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
import java.util.Date;
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
class MovieControllerTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieMapper movieMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private MovieItemRepository movieItemRepository;
    @Mock
    private MovieItemMapper movieItemMapper;
    @Mock
    private MediaService mediaService;
    @Mock
    private RedisService redisService;
    @Mock
    private WatchHistoryRepository watchHistoryRepository;
    @Mock
    private WatchHistoryMapper watchHistoryMapper;
    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private MovieService movieService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserMovieRepository userMovieRepository;
    @Mock
    private FeignAccountAuthService feignAccountAuthService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ImdbService imdbService;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private MovieController movieController;

    // ---------- create ----------

    @Test
    void create_withoutImdbIdOrCategoriesOrNotification_savesMovie() {
        CreateMovieForm form = new CreateMovieForm();
        form.setTitle("Movie Title");
        form.setImdbId("");
        form.setCategoryIds(null);
        SendNotificationConfigForm notificationConfig = new SendNotificationConfigForm();
        notificationConfig.setIsSendNotification(false);
        form.setSendNotificationConfig(notificationConfig);

        Movie mappedEntity = new Movie();
        when(movieMapper.fromCreateMovieFormToEntity(form)).thenReturn(mappedEntity);

        ApiMessageDto<Void> response = movieController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(mappedEntity.getSlug()).isNotBlank();
        verify(movieRepository, times(1)).save(mappedEntity);
        verify(imdbService, never()).fetchRating(any());
        verify(notificationService, never()).createNotificationTemplate(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_withImdbIdAndCategories_setsRatingAndCategories() {
        CreateMovieForm form = new CreateMovieForm();
        form.setTitle("Movie Title");
        form.setImdbId("tt1234567");
        form.setCategoryIds(Collections.singletonList(1L));
        SendNotificationConfigForm notificationConfig = new SendNotificationConfigForm();
        notificationConfig.setIsSendNotification(false);
        form.setSendNotificationConfig(notificationConfig);

        Movie mappedEntity = new Movie();
        List<Category> categories = Collections.singletonList(new Category());
        when(movieMapper.fromCreateMovieFormToEntity(form)).thenReturn(mappedEntity);
        when(imdbService.fetchRating("tt1234567")).thenReturn(8.5);
        when(categoryRepository.findAllById(form.getCategoryIds())).thenReturn(categories);

        ApiMessageDto<Void> response = movieController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(mappedEntity.getImdbRating()).isEqualTo(8.5);
        assertThat(mappedEntity.getCategories()).isEqualTo(categories);
        verify(movieRepository, times(1)).save(mappedEntity);
    }

    @Test
    void create_withSendNotificationForAllUsers_createsNotificationTemplate() {
        CreateMovieForm form = new CreateMovieForm();
        form.setTitle("Movie Title");
        form.setImdbId(null);
        form.setCategoryIds(null);
        SendNotificationConfigForm notificationConfig = new SendNotificationConfigForm();
        notificationConfig.setIsSendNotification(true);
        notificationConfig.setSendFor(BaseConstant.SEND_NOTIFICATION_FOR_ALL_USERS);
        notificationConfig.setTitle("Custom title");
        notificationConfig.setScheduleAt(new Date());
        form.setSendNotificationConfig(notificationConfig);

        Movie mappedEntity = new Movie();
        mappedEntity.setReleaseDate(new Date());
        when(movieMapper.fromCreateMovieFormToEntity(form)).thenReturn(mappedEntity);
        when(movieService.resolveScheduleAt(any(), any())).thenReturn(new Date());

        ApiMessageDto<Void> response = movieController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(notificationService, times(1)).createNotificationTemplate(
                eq("Custom title"), eq(BaseConstant.CMD_NEW_MOVIE), any(), eq(BaseConstant.NOTIFICATION_TYPE_MOVIE),
                eq(BaseConstant.NOTIFICATION_TARGET_TYPE_APP), eq(BaseConstant.APP_MOVIE), any());
    }

    @Test
    void create_withSendNotificationForInterestedUsers_noInterestedUsers_skipsNotification() {
        CreateMovieForm form = new CreateMovieForm();
        form.setTitle("Movie Title");
        form.setImdbId(null);
        form.setCategoryIds(null);
        SendNotificationConfigForm notificationConfig = new SendNotificationConfigForm();
        notificationConfig.setIsSendNotification(true);
        notificationConfig.setSendFor(BaseConstant.SEND_NOTIFICATION_FOR_INTERESTED_USERS);
        notificationConfig.setScheduleAt(new Date());
        form.setSendNotificationConfig(notificationConfig);

        Movie mappedEntity = new Movie();
        mappedEntity.setReleaseDate(new Date());
        when(movieMapper.fromCreateMovieFormToEntity(form)).thenReturn(mappedEntity);
        when(movieService.resolveScheduleAt(any(), any())).thenReturn(new Date());
        when(movieService.findInterestedUserIds(mappedEntity)).thenReturn(Collections.emptyList());

        ApiMessageDto<Void> response = movieController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(notificationService, never()).createNotificationTemplate(any(), any(), any(), any(), any(), any(), any());
    }

    // ---------- adminGet ----------

    @Test
    void adminGet_whenFound_returnsSuccessResponse() {
        Movie movie = new Movie();
        movie.setId(1L);
        MovieDto dto = new MovieDto();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieMapper.entityToMovieDto(movie)).thenReturn(dto);

        ApiMessageDto<MovieDto> response = movieController.adminGet(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void adminGet_whenNotFound_throwsNotFoundException() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.adminGet(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ERROR_NOT_FOUND);
    }

    // ---------- get ----------

    @Test
    void get_whenCached_returnsCachedDto() {
        MovieDto cachedDto = new MovieDto();
        when(redisService.buildKey("movie", "1")).thenReturn("movie::1");
        when(redisService.get("movie::1", MovieDto.class)).thenReturn(cachedDto);

        ApiMessageDto<MovieDto> response = movieController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(cachedDto);
        verify(movieRepository, never()).findByIdAndStatus(any(), any());
    }

    @Test
    void get_whenNotCachedAndNotFound_throwsNotFoundException() {
        when(redisService.buildKey("movie", "99")).thenReturn("movie::99");
        when(redisService.get("movie::99", MovieDto.class)).thenReturn(null);
        when(movieRepository.findByIdAndStatus(99L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ERROR_NOT_FOUND);
    }

    @Test
    void get_whenNotCachedAndFound_buildsAndCachesDto() {
        Movie movie = new Movie();
        movie.setId(1L);
        MovieDto dto = new MovieDto();
        when(redisService.buildKey("movie", "1")).thenReturn("movie::1");
        when(redisService.get("movie::1", MovieDto.class)).thenReturn(null);
        when(movieRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(movie));
        when(movieItemRepository.findByMovieIdAndStatusWithParent(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Collections.emptyList());
        when(movieMapper.entityToMovieDto(movie)).thenReturn(dto);

        ApiMessageDto<MovieDto> response = movieController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
        verify(redisService, times(1)).put(eq("movie::1"), eq(dto), eq(5 * 60));
    }

    // ---------- list / listForAdmin / autoComplete ----------

    @Test
    void list_returnsSuccessResponseWrappingList() {
        MovieCriteria criteria = new MovieCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Movie movie = new Movie();
        movie.setId(1L);
        Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));
        List<MovieDto> dtoList = Collections.singletonList(new MovieDto());

        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(movieMapper.fromEntityToMovieAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = movieController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getStatus()).isEqualTo(BaseConstant.STATUS_ACTIVE);
    }

    @Test
    void listForAdmin_returnsSuccessResponseWrappingList() {
        MovieCriteria criteria = new MovieCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(Collections.emptyList());
        List<MovieDto> dtoList = Collections.emptyList();

        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(movieMapper.fromEntityToMovieAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = movieController.listForAdmin(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
    }

    @Test
    void autoComplete_returnsSuccessResponseWrappingList() {
        MovieCriteria criteria = new MovieCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(Collections.emptyList());
        List<MovieDto> dtoList = Collections.emptyList();

        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(movieMapper.fromEntityToMovieAutoCompleteShortDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = movieController.autoComplete(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(criteria.getStatus()).isEqualTo(BaseConstant.STATUS_ACTIVE);
    }

    // ---------- update ----------

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdateMovieForm form = new UpdateMovieForm();
        form.setId(99L);
        form.setTitle("New title");

        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenFoundBasicMovieType_updatesAndClearsCaches() {
        UpdateMovieForm form = new UpdateMovieForm();
        form.setId(1L);
        form.setTitle("New title");
        form.setThumbnailUrl("thumb-new");
        form.setPosterUrl("poster-new");
        form.setImageTitleUrl(null);
        form.setImdbId(null);
        form.setDuration(null);

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Old title");
        movie.setThumbnailUrl("thumb-old");
        movie.setPosterUrl("poster-old");
        movie.setImageTitleUrl(null);
        movie.setImdbId(null);
        movie.setType(BaseConstant.MOVIE_TYPE_SINGLE);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(redisService.buildKey(eq("movie"), any())).thenReturn("k");

        ApiMessageDto<Void> response = movieController.update(form);

        assertThat(response.getResult()).isTrue();
        assertThat(movie.getSlug()).isNotBlank();
        verify(movieRepository, times(1)).save(movie);
        verify(imdbService, never()).fetchRating(any());
    }

    @Test
    void update_withNewCategoryIds_replacesCategories() {
        UpdateMovieForm form = new UpdateMovieForm();
        form.setId(1L);
        form.setTitle("Same Title");
        form.setCategoryIds(Collections.singletonList(2L));

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Same Title");
        movie.setType(BaseConstant.MOVIE_TYPE_SINGLE);

        List<Category> categories = Collections.singletonList(new Category());
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(categoryRepository.findAllById(form.getCategoryIds())).thenReturn(categories);
        when(redisService.buildKey(eq("movie"), any())).thenReturn("k");

        ApiMessageDto<Void> response = movieController.update(form);

        assertThat(response.getResult()).isTrue();
        assertThat(movie.getCategories()).isEqualTo(categories);
    }

    @Test
    void update_whenImdbIdChanged_fetchesNewRating() {
        UpdateMovieForm form = new UpdateMovieForm();
        form.setId(1L);
        form.setTitle("Same Title");
        form.setImdbId("tt7654321");

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Same Title");
        movie.setImdbId(null);
        movie.setType(BaseConstant.MOVIE_TYPE_SINGLE);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(redisService.buildKey(eq("movie"), any())).thenReturn("k");
        // simulate mapper setting imdbId onto entity as it would in real mapping
        org.mockito.Mockito.doAnswer(invocation -> {
            movie.setImdbId("tt7654321");
            return null;
        }).when(movieMapper).fromUpdateMovieFormToEntity(form, movie);
        when(imdbService.fetchRating("tt7654321")).thenReturn(9.0);

        ApiMessageDto<Void> response = movieController.update(form);

        assertThat(response.getResult()).isTrue();
        assertThat(movie.getImdbRating()).isEqualTo(9.0);
    }

    // ---------- syncImdbRatings ----------

    @Test
    void syncImdbRatings_whenSucceeds_returnsSuccessResponse() {
        ImdbRatingsSyncDto dto = new ImdbRatingsSyncDto();
        when(imdbService.syncAllMovieRatings()).thenReturn(dto);

        ApiMessageDto<ImdbRatingsSyncDto> response = movieController.syncImdbRatings();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void syncImdbRatings_whenServiceThrows_throwsBadRequestException() {
        when(imdbService.syncAllMovieRatings()).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> movieController.syncImdbRatings())
                .isInstanceOf(BadRequestException.class);
    }

    // ---------- delete ----------

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenFound_deletesMovieAndRelatedData() {
        Movie movie = new Movie();
        movie.setId(1L);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieItemRepository.findThumbnailsByMovieId(1L)).thenReturn(Collections.emptyList());
        when(redisService.buildKey(eq("movie"), any())).thenReturn("k");

        ApiMessageDto<Void> response = movieController.delete(1L);

        assertThat(response.getResult()).isTrue();
        verify(movieRepository, times(1)).delete(movie);
    }

    // ---------- suggestion ----------

    @Test
    void suggestion_returnsSuccessResponse() {
        List<MovieDto> movies = Collections.singletonList(new MovieDto());
        when(movieService.getCachedSuggestedMovies(1L)).thenReturn(movies);

        ApiMessageDto<List<MovieDto>> response = movieController.suggestion(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(movies);
    }

    // ---------- listWatched ----------

    @Test
    void listWatched_whenUserNotFound_throwsNotFoundException() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.listWatched())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void listWatched_whenUserFound_returnsWatchedMovies() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        List<Movie> watched = Collections.singletonList(new Movie());
        List<MovieDto> dtoList = Collections.singletonList(new MovieDto());

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(watchHistoryRepository.findWatchedMoviesByUserOrderByDate(eq(1L), any())).thenReturn(watched);
        when(movieMapper.fromEntityToMovieAutoCompleteShortDtoList(watched)).thenReturn(dtoList);

        ApiMessageDto<List<MovieDto>> response = movieController.listWatched();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dtoList);
    }

    // ---------- suggestByWatched ----------

    @Test
    void suggestByWatched_whenPageOutOfRange_returnsMessageOnlyResponse() {
        ApiMessageDto<SuggestByWatchedDto> response = movieController.suggestByWatched(5);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("Page must be between 0 and 1");
    }

    @Test
    void suggestByWatched_whenUserNotFound_throwsNotFoundException() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.suggestByWatched(0))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void suggestByWatched_whenNoWatchHistory_returnsMessageOnlyResponse() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(watchHistoryRepository.findWatchedMoviesByUserOrderByDate(eq(1L), any())).thenReturn(Collections.emptyList());

        ApiMessageDto<SuggestByWatchedDto> response = movieController.suggestByWatched(0);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("Not enough watch history");
    }

    @Test
    void suggestByWatched_whenWatchHistoryExists_returnsSuggestion() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        Movie watchedMovie = new Movie();
        watchedMovie.setId(2L);
        MovieDto watchedMovieDto = new MovieDto();
        List<MovieDto> suggestions = Collections.singletonList(new MovieDto());

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(watchHistoryRepository.findWatchedMoviesByUserOrderByDate(eq(1L), any()))
                .thenReturn(Collections.singletonList(watchedMovie));
        when(movieMapper.fromEntityToMovieAutoCompleteShortDto(watchedMovie)).thenReturn(watchedMovieDto);
        when(movieService.getCachedSuggestedMovies(2L)).thenReturn(suggestions);

        ApiMessageDto<SuggestByWatchedDto> response = movieController.suggestByWatched(0);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getWatchedMovie()).isEqualTo(watchedMovieDto);
        assertThat(response.getData().getSuggestedMovies()).isEqualTo(suggestions);
    }

    // ---------- recommendation ----------

    @Test
    void recommendation_whenUserNotFound_throwsNotFoundException() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.recommendation())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void recommendation_whenUserFound_returnsRecommendations() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        List<MovieDto> recommendations = Collections.singletonList(new MovieDto());

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(movieService.getRecommendationsForUser(1L, 20)).thenReturn(recommendations);

        ApiMessageDto<List<MovieDto>> response = movieController.recommendation();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(recommendations);
    }

    // ---------- recentWatchedCategoryRecommendation ----------

    @Test
    void recentWatchedCategoryRecommendation_whenUserNotFound_throwsNotFoundException() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.recentWatchedCategoryRecommendation())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void recentWatchedCategoryRecommendation_whenUserFound_returnsRecommendation() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        RecentWatchedCategoryRecommendationDto dto = new RecentWatchedCategoryRecommendationDto();

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(movieService.getRecentWatchedCategoryRecommendationsForUser(1L, 10)).thenReturn(dto);

        ApiMessageDto<RecentWatchedCategoryRecommendationDto> response = movieController.recentWatchedCategoryRecommendation();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    // ---------- recommendationByKnn ----------

    @Test
    void recommendationByKnn_whenUserNotFound_throwsNotFoundException() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.recommendationByKnn(20))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void recommendationByKnn_whenUserFound_returnsHybridRecommendations() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        Page<Movie> page = new PageImpl<>(Collections.emptyList());
        List<MovieDto> dtoList = Collections.emptyList();

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(movieService.getHybridRecommendationsForUser(eq(1L), any())).thenReturn(page);
        when(movieMapper.fromEntityToMovieAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = movieController.recommendationByKnn(50);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
    }

    // ---------- history ----------

    @Test
    void history_returnsSuccessResponse() {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        List<WatchHistory> histories = Collections.singletonList(new WatchHistory());
        List<WatchHistoryDto> dtoList = Collections.singletonList(new WatchHistoryDto());

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(watchHistoryRepository.findLatestInProgressGroupedByMovie(1L)).thenReturn(histories);
        when(watchHistoryMapper.fromEntityToWatchHistoryDetailsDtoList(histories)).thenReturn(dtoList);

        ApiMessageDto<List<WatchHistoryDto>> response = movieController.history();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dtoList);
    }

    // ---------- topViews ----------

    @Test
    void topViews_returnsSuccessResponseWrappingList() {
        MovieCriteria criteria = new MovieCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(Collections.emptyList());
        List<MovieDto> dtoList = Collections.emptyList();

        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movieMapper.fromEntityToMovieAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = movieController.topViews(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(criteria.getStatus()).isEqualTo(BaseConstant.STATUS_ACTIVE);
    }

    // ---------- collectionFilter ----------

    @Test
    void collectionFilter_whenCollectionNotFound_throwsNotFoundException() {
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.collectionFilter(99L, null, PageRequest.of(0, 10)))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
    }

    @Test
    void collectionFilter_whenCollectionFound_returnsMovieList() {
        Collection collection = new Collection();
        collection.setId(1L);
        collection.setFilter("{}");
        MovieCriteria criteria = new MovieCriteria();
        Page<Movie> page = new PageImpl<>(Collections.emptyList());
        List<MovieDto> dtoList = Collections.emptyList();

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(movieService.parseFilterMovie(collection.getFilter())).thenReturn(new com.movie.api.form.movie.FilterMovieForm());
        when(movieMapper.fromFilterMovieFromToMovieCriteria(any())).thenReturn(criteria);
        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movieMapper.fromEntityToMovieAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response =
                movieController.collectionFilter(1L, "title-filter", PageRequest.of(0, 10));

        assertThat(response.getResult()).isTrue();
        assertThat(criteria.getCollectionId()).isEqualTo(1L);
        assertThat(criteria.getTitle()).isEqualTo("title-filter");
    }

    // ---------- schedule ----------

    @Test
    void schedule_returnsSuccessResponse() {
        List<MovieItem> movieItems = Collections.emptyList();
        List<MovieItemDto> dtoList = Collections.emptyList();

        when(movieItemRepository.findAll(any(Specification.class))).thenReturn(movieItems);
        when(movieItemMapper.entityToMovieItemDtoWithMovieList(movieItems)).thenReturn(dtoList);

        ApiMessageDto<List<MovieItemDto>> response = movieController.schedule(new Date());

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dtoList);
    }

    // ---------- getNextEpisode ----------

    @Test
    void getNextEpisode_whenMovieNotFound_throwsNotFoundException() {
        when(movieRepository.findByIdAndStatus(99L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.getNextEpisode(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ERROR_NOT_FOUND);
    }

    @Test
    void getNextEpisode_whenMovieIsNotSeries_returnsMessageOnlyResponse() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setType(BaseConstant.MOVIE_TYPE_SINGLE);
        when(movieRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(movie));

        ApiMessageDto<MovieItemDto> response = movieController.getNextEpisode(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("Movie is not series");
    }

    @Test
    void getNextEpisode_whenSeriesHasNextEpisode_returnsEpisode() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setType(BaseConstant.MOVIE_TYPE_SERIES);
        MovieItem nextEpisode = new MovieItem();
        nextEpisode.setId(10L);
        MovieItemDto dto = new MovieItemDto();

        when(movieRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(movie));
        when(movieItemRepository.findNextEpisode(eq(1L), eq(BaseConstant.MOVIE_ITEM_KIND_EPISODE), any(), any()))
                .thenReturn(Collections.singletonList(nextEpisode));
        when(movieItemMapper.entityToMovieItemMetadataDto(nextEpisode)).thenReturn(dto);

        ApiMessageDto<MovieItemDto> response = movieController.getNextEpisode(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    // ---------- listSurvey ----------

    @Test
    void listSurvey_returnsSuccessResponse() {
        MovieCriteria criteria = new MovieCriteria();
        Page<Movie> page = new PageImpl<>(Collections.emptyList());
        List<MovieDto> dtoList = Collections.emptyList();

        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movieMapper.fromEntityToMovieSurveyDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<List<MovieDto>> response = movieController.listSurvey(criteria);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dtoList);
        assertThat(criteria.getIsFeatured()).isTrue();
        assertThat(criteria.getStatus()).isEqualTo(BaseConstant.STATUS_ACTIVE);
    }

    // ---------- makeSurvey ----------

    @Test
    void makeSurvey_whenUserNotFound_throwsNotFoundException() {
        MakeSurveyForm form = new MakeSurveyForm();
        form.setMovieIds(java.util.Arrays.asList(1L, 2L, 3L));

        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.makeSurvey(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void makeSurvey_whenUserAlreadyMadeSurvey_returnsMessageOnlyResponse() {
        MakeSurveyForm form = new MakeSurveyForm();
        form.setMovieIds(java.util.Arrays.asList(1L, 2L, 3L));

        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        account.setIsMakeSurvey(true);

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));

        ApiMessageDto<Void> response = movieController.makeSurvey(form);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("User has already made survey");
        verify(userMovieRepository, never()).saveAll(any());
    }

    @Test
    void makeSurvey_whenFewerThanThreeValidMovies_throwsBadRequestException() {
        MakeSurveyForm form = new MakeSurveyForm();
        form.setMovieIds(java.util.Arrays.asList(1L, 2L, 3L));

        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        account.setIsMakeSurvey(false);

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(movieRepository.findAllById(form.getMovieIds())).thenReturn(Collections.singletonList(new Movie()));

        assertThatThrownBy(() -> movieController.makeSurvey(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SURVEY_ERROR_MIN_MOVIES);
    }

    @Test
    void makeSurvey_whenValid_savesUserMoviesAndUpdatesSurveyStatus() {
        MakeSurveyForm form = new MakeSurveyForm();
        form.setMovieIds(java.util.Arrays.asList(1L, 2L, 3L));

        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        account.setIsMakeSurvey(false);

        Movie movie1 = new Movie();
        movie1.setId(1L);
        Movie movie2 = new Movie();
        movie2.setId(2L);
        Movie movie3 = new Movie();
        movie3.setId(3L);

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(movieRepository.findAllById(form.getMovieIds())).thenReturn(java.util.Arrays.asList(movie1, movie2, movie3));
        when(userMovieRepository.findByUserIdAndType(1L, BaseConstant.USER_MOVIE_TYPE_SURVEY)).thenReturn(Collections.emptyList());

        ApiMessageDto<Void> response = movieController.makeSurvey(form);

        assertThat(response.getResult()).isTrue();
        verify(userMovieRepository, times(1)).saveAll(any());
        verify(accountRepository, times(1)).save(account);
        assertThat(account.getIsMakeSurvey()).isTrue();
    }

    @Test
    void makeSurvey_whenFeignThrows_throwsBadRequestException() {
        MakeSurveyForm form = new MakeSurveyForm();
        form.setMovieIds(java.util.Arrays.asList(1L, 2L, 3L));

        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        account.setIsMakeSurvey(false);

        Movie movie1 = new Movie();
        movie1.setId(1L);
        Movie movie2 = new Movie();
        movie2.setId(2L);
        Movie movie3 = new Movie();
        movie3.setId(3L);

        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));
        when(movieRepository.findAllById(form.getMovieIds())).thenReturn(java.util.Arrays.asList(movie1, movie2, movie3));
        when(userMovieRepository.findByUserIdAndType(1L, BaseConstant.USER_MOVIE_TYPE_SURVEY)).thenReturn(Collections.emptyList());
        org.mockito.Mockito.doThrow(new RuntimeException("feign down"))
                .when(feignAccountAuthService).updateMakeSurvey(any(), any());

        assertThatThrownBy(() -> movieController.makeSurvey(form))
                .isInstanceOf(BadRequestException.class);
    }

    // ---------- resetSurvey ----------

    @Test
    void resetSurvey_whenUserNotFound_throwsNotFoundException() {
        when(accountRepository.findByIdAndStatusAndKind(99L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieController.resetSurvey(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void resetSurvey_whenUserFound_resetsSurveyStatus() {
        Account account = new Account();
        account.setId(1L);
        account.setIsMakeSurvey(true);

        when(accountRepository.findByIdAndStatusAndKind(1L, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER))
                .thenReturn(Optional.of(account));

        ApiMessageDto<Void> response = movieController.resetSurvey(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(account.getIsMakeSurvey()).isFalse();
        verify(userMovieRepository, times(1)).deleteByUserIdAndType(1L, BaseConstant.USER_MOVIE_TYPE_SURVEY);
        verify(accountRepository, times(1)).save(account);
    }
}
