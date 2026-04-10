package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ListIdDto;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.playlist.PlaylistDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.playlist.ActionUpdatePlaylistForm;
import com.movie.api.form.playlist.CreatePlaylistForm;
import com.movie.api.form.playlist.UpdatePlaylistForm;
import com.movie.api.form.playlist.UpdatePlaylistItemForm;
import com.movie.api.mapper.PlaylistItemMapper;
import com.movie.api.mapper.PlaylistMapper;
import com.movie.api.storage.model.*;
import com.movie.api.storage.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/playlist")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class PlaylistController extends ABasicController {
    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PlaylistItemRepository playlistItemRepository;

    @Autowired
    private PlaylistItemMapper playlistItemMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<PlaylistDto> create(@Valid @RequestBody CreatePlaylistForm form) {
        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[User] not found", ErrorCode.USER_ERROR_NOT_FOUND));

        if (playlistRepository.countByUserId(user.getId()) >= BaseConstant.MAX_PLAYLIST_PER_USER) {
            throw new BadRequestException("[Playlist] Maximum playlist per user", ErrorCode.PLAYLIST_ERROR_MAX_PER_USER);
        }

        Playlist playlist = playlistMapper.fromCreatePlaylistFormToEntity(form);
        playlist.setUser(user);
        playlist = playlistRepository.save(playlist);
        return makeSuccessResponse(playlistMapper.entityToPlaylistDto(playlist), "Create playlist success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<PlaylistDto> get(@PathVariable("id") Long id) {
        Playlist playlist = playlistRepository.findByIdAndUserId(id, getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Playlist] Not found", ErrorCode.PLAYLIST_ERROR_NOT_FOUND));
        return makeSuccessResponse(playlistMapper.entityToPlaylistDto(playlist), "Get playlist success.");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<PlaylistDto>> list() {
        List<Playlist> playlists = playlistRepository.findByUserIdOrderByCreatedDateDesc(getCurrentUser());
        return makeSuccessResponse(playlistMapper.entityToPlaylistDtoList(playlists), "List playlist success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdatePlaylistForm form) {
        Playlist playlist = playlistRepository.findByIdAndUserId(form.getId(), getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Playlist] Not found", ErrorCode.PLAYLIST_ERROR_NOT_FOUND));

        playlistMapper.fromUpdatePlaylistFormToEntity(form, playlist);
        playlistRepository.save(playlist);
        return makeSuccessResponse("Update playlist success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Playlist playlist = playlistRepository.findByIdAndUserId(id, getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Playlist] Not found", ErrorCode.PLAYLIST_ERROR_NOT_FOUND));
        playlistItemRepository.deleteByPlaylistId(playlist.getId());
        playlistRepository.delete(playlist);
        return makeSuccessResponse("Delete playlist success");
    }

    @GetMapping(value = "/{id}/movies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<MovieDto>>> movies(@PathVariable("id") Long id, Pageable pageable) {
        Playlist playlist = playlistRepository.findByIdAndUserId(id, getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Playlist] Not found", ErrorCode.PLAYLIST_ERROR_NOT_FOUND));

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate")));
        Page<PlaylistItem> playlistItems = playlistItemRepository.findAllByPlaylistId(playlist.getId(), pageable);
        return makeSuccessResponse(makeResponseListDto(playlistItems, playlistItemMapper::playlistItemsToMovieDtos), "List movie success");
    }

    @GetMapping(value = "/list-by-movie/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ListIdDto> movies(@PathVariable("movieId") Long movieId) {
        List<Long> ids = playlistRepository.findByMovieIdAndUserId(movieId, getCurrentUser());
        return makeSuccessResponse(new ListIdDto(ids), "List movie success");
    }

    @PostMapping(value = "/update-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> updateItem(@Valid @RequestBody UpdatePlaylistItemForm form) {
        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[User] not found", ErrorCode.USER_ERROR_NOT_FOUND));

        Movie movie = movieRepository.findByIdAndStatus(form.getMovieId(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        List<Long> playlistIds = form.getActions().stream()
                .map(ActionUpdatePlaylistForm::getPlaylistId)
                .distinct()
                .collect(Collectors.toList());

        List<Playlist> playlists = playlistRepository.findAllByIdInAndUserId(playlistIds, user.getId());
        if (playlists.size() != playlistIds.size()) {
            throw new NotFoundException("[Playlist] One or more playlists not found", ErrorCode.PLAYLIST_ERROR_NOT_FOUND);
        }

        Map<Long, Playlist> playlistMap = playlists.stream()
                .collect(Collectors.toMap(Playlist::getId, playlist -> playlist));

        for (ActionUpdatePlaylistForm action : form.getActions()) {
            Long playlistId = action.getPlaylistId();
            Playlist playlist = playlistMap.get(playlistId);

            if (Objects.equals(action.getAction(), BaseConstant.ACTION_DELETE_FROM_PLAYLIST)) {
                removeMovieFromPlaylist(playlist, movie);
            } else {
                addMovieToPlaylist(playlist, movie);
            }
        }
        return makeSuccessResponse("Update playlist success");
    }

    @DeleteMapping(value = "/remove-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> removeItem(@RequestParam("playlistId") Long playlistId, @RequestParam("movieId") Long movieId) {
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Playlist] Not found", ErrorCode.PLAYLIST_ERROR_NOT_FOUND));
        Movie movie = movieRepository.findByIdAndStatus(movieId, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
        removeMovieFromPlaylist(playlist, movie);
        return makeSuccessResponse("Delete movie success");
    }

    private void addMovieToPlaylist(Playlist playlist, Movie movie) {
        // Check if already exists
        boolean exists = playlistItemRepository.existsByPlaylistIdAndMovieId(
                playlist.getId(), movie.getId()
        );

        if (exists) {
            log.warn("Movie {} already exists in playlist {}", movie.getId(), playlist.getId());
            return;
        }

        PlaylistItem item = new PlaylistItem();
        item.setPlaylist(playlist);
        item.setMovie(movie);
        playlistItemRepository.save(item);

        // Update totalMovie
        playlistRepository.updateTotalMovie(playlist.getId(), playlist.getTotalMovie() + 1);

        log.info("Added movie {} to playlist {}", movie.getId(), playlist.getId());
    }

    private void removeMovieFromPlaylist(Playlist playlist, Movie movie) {
        // Check if exists
        Optional<PlaylistItem> itemOpt = playlistItemRepository
                .findByPlaylistIdAndMovieId(playlist.getId(), movie.getId());

        if (itemOpt.isEmpty()) {
            log.warn("Movie {} not found in playlist {}", movie.getId(), playlist.getId());
            return;
        }

        // Delete playlist item
        playlistItemRepository.deleteByPlaylistIdAndMovieId(playlist.getId(), movie.getId());

        int totalMove = playlist.getTotalMovie() > 0 ? playlist.getTotalMovie() - 1 : 0;
        // Update totalMovie
        playlistRepository.updateTotalMovie(playlist.getId(), totalMove);

        log.info("Removed movie {} from playlist {}", movie.getId(), playlist.getId());
    }
}
