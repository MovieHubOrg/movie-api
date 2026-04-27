package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.room.RoomDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.exception.UnauthorizationException;
import com.movie.api.form.room.CreateRoomForm;
import com.movie.api.form.room.TestChatForm;
import com.movie.api.form.room.TestLeftParticipantForm;
import com.movie.api.mapper.RoomMapper;
import com.movie.api.service.RoomService;
import com.movie.api.storage.criteria.RoomCriteria;
import com.movie.api.storage.model.*;
import com.movie.api.storage.repository.*;
import com.movie.api.utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/room")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class RoomController extends ABasicController {
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 3;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovieItemRepository movieItemRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private RoomService roomService;

    @Value("${server.internal.password}")
    private String serverInternalPassword;

    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<RoomDto> check() {
        Room room = roomRepository.findFirstByHostIdAndState(getCurrentUser(), BaseConstant.ROOM_STATE_RUNNING).orElse(null);
        return makeSuccessResponse(roomMapper.entityToRoomDto(room), "Check success.");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateRoomForm form) {
        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[User] not found", ErrorCode.USER_ERROR_NOT_FOUND));

        MovieItem movieItem = movieItemRepository.findByIdAndStatus(form.getMovieItemId(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[MovieItem] not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));

        if (!isValidRoomMovieItem(movieItem)) {
            throw new BadRequestException("[MovieItem] invalid request", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
        }

        Date startTime = form.getStartTime();
        Date endTime = form.getEndTime() != null ? form.getEndTime() : DateUtils.addHours(startTime, 4);
        if (!endTime.after(startTime)) {
            throw new BadRequestException("[Room] endTime invalid", ErrorCode.ROOM_ERROR_INVALID_TIME);
        }

        Room room = roomMapper.fromCreateRoomFormToEntity(form);
        room.setCode(generateRoomCode());
        room.setMovieItem(movieItem);
        room.setHost(user);
        room.setEndTime(endTime);
        room.setState(BaseConstant.ROOM_STATE_PENDING);
        roomRepository.save(room);

        List<Account> accounts = new ArrayList<>();
        if (Objects.equals(form.getKind(), BaseConstant.ROOM_KIND_PRIVATE) && form.getAccountIds() != null && !form.getAccountIds().isEmpty()) {
            accounts = accountRepository.findAllByIdInAndKindAndStatus(form.getAccountIds(), BaseConstant.ACCOUNT_KIND_USER, BaseConstant.STATUS_ACTIVE);
        }

        boolean hasAccount = accounts.stream()
                .anyMatch(acc -> acc.getId().equals(user.getId()));
        if (!hasAccount) {
            accounts.add(user);
        }

        List<Participant> participants = new ArrayList<>();
        for (Account a : accounts) {
            Participant participant = new Participant();
            participant.setUser(a);
            participant.setRoom(room);
            if (Objects.equals(a.getId(), user.getId())) {
                participant.setRole(BaseConstant.PARTICIPANT_ROLE_HOST);
            } else {
                participant.setRole(BaseConstant.PARTICIPANT_ROLE_GUEST);
            }
            participant.setState(BaseConstant.PARTICIPANT_STATE_PENDING);
            participants.add(participant);
        }
        participantRepository.saveAll(participants);

        room.setParticipantCount(participants.size());
        roomRepository.save(room);
        return makeSuccessResponse("Create room success");
    }

    @PostMapping(value = "/start/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> start(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Room] not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        if (!Objects.equals(room.getHost().getId(), getCurrentUser())) {
            throw new UnauthorizationException("Not allow");
        }
        if (!Objects.equals(room.getState(), BaseConstant.ROOM_STATE_PENDING)) {
            throw new BadRequestException("[Room] room state invalid", ErrorCode.ROOM_ERROR_INVALID_STATE);
        }
        boolean hasRunningRoom = roomRepository.existsByHostIdAndState(getCurrentUser(), BaseConstant.ROOM_STATE_RUNNING);
        if (hasRunningRoom) {
            throw new BadRequestException("[Room] host already has a running room", ErrorCode.ROOM_ERROR_INVALID_ROOM);
        }

        Date now = new Date();
        if (now.before(room.getStartTime()) || !room.getEndTime().after(now)) {
            throw new BadRequestException("[Room] time invalid", ErrorCode.ROOM_ERROR_INVALID_TIME);
        }

        room.setStartTime(now);
        room.setState(BaseConstant.ROOM_STATE_RUNNING);
        roomRepository.save(room);
        return makeSuccessResponse("Start room success");
    }

    @PostMapping(value = "/end/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<RoomDto> end(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Room] not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        if (!Objects.equals(room.getHost().getId(), getCurrentUser())) {
            throw new UnauthorizationException("Not allow");
        }
        if (!Objects.equals(room.getState(), BaseConstant.ROOM_STATE_RUNNING)) {
            throw new BadRequestException("[Room] room state invalid", ErrorCode.ROOM_ERROR_INVALID_STATE);
        }
        roomService.endRoom(room, "ROOM_END");
        return makeSuccessResponse("End room success");
    }

    @PostMapping(value = "/join/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<RoomDto> join(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Room] not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        if (!Objects.equals(room.getState(), BaseConstant.ROOM_STATE_RUNNING)) {
            throw new BadRequestException("[Room] room state invalid", ErrorCode.ROOM_ERROR_INVALID_STATE);
        }

        Long currentUserId = getCurrentUser();
        boolean isHost = Objects.equals(room.getHost().getId(), currentUserId);

        Participant participant = participantRepository.findByRoomIdAndUserId(id, currentUserId).orElse(null);

        if (isHost) {
            if (participant == null) {
                throw new NotFoundException("[Participant] host participant not found", ErrorCode.PARTICIPANT_ERROR_NOT_FOUND);
            }
            if (Objects.equals(participant.getState(), BaseConstant.PARTICIPANT_STATE_JOIN)) {
                throw new BadRequestException("[Room] already joined", ErrorCode.ROOM_ERROR_ALREADY_JOINED);
            }
            participant.setState(BaseConstant.PARTICIPANT_STATE_JOIN);

        } else {
            boolean isHostJoined = participantRepository.existsByRoomIdAndRoleAndState(id, BaseConstant.PARTICIPANT_ROLE_HOST, BaseConstant.PARTICIPANT_STATE_JOIN);
            if (!isHostJoined) {
                throw new BadRequestException("[Room] host has not joined yet", ErrorCode.ROOM_ERROR_HOST_NOT_JOINED);
            }

            if (participant == null) {
                Account user = accountRepository.findById(currentUserId)
                        .orElseThrow(() -> new NotFoundException("[User] not found", ErrorCode.USER_ERROR_NOT_FOUND));
                participant = new Participant();
                participant.setRoom(room);
                participant.setUser(user);
                participant.setRole(BaseConstant.PARTICIPANT_ROLE_GUEST);
                participant.setState(BaseConstant.PARTICIPANT_STATE_JOIN);

                room.setParticipantCount(room.getParticipantCount() + 1);
                roomRepository.save(room);
            } else {
                if (Objects.equals(participant.getState(), BaseConstant.PARTICIPANT_STATE_JOIN)) {
                    throw new BadRequestException("[Room] already joined");
                }
                participant.setState(BaseConstant.PARTICIPANT_STATE_JOIN);
            }
        }
        participantRepository.save(participant);
        roomService.publishCurrentViewerCount(room);
        return makeSuccessResponse(roomMapper.entityToRoomDto(room), "Join room success.");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<RoomDto> get(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Room] Not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        return makeSuccessResponse(roomMapper.entityToRoomDto(room), "Get room success.");
    }

    @GetMapping(value = "/get-by-code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<RoomDto> get(@PathVariable String code) {
        Room room = roomRepository.findFirstByCodeAndStateNot(code, BaseConstant.ROOM_STATE_ENDING)
                .orElseThrow(() -> new NotFoundException("[Room] Not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        return makeSuccessResponse(roomMapper.entityToRoomDto(room), "Get room success.");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<RoomDto>>> list(RoomCriteria criteria, Pageable pageable) {
        criteria.setSortState(true);
        Page<Room> rooms = roomRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(rooms, roomMapper::fromEntityToRoomDtoList), "List room success");
    }

    @GetMapping(value = "/my-rooms", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<RoomDto>>> myRooms(RoomCriteria criteria, Pageable pageable) {
        criteria.setHostId(getCurrentUser());
        criteria.setSortState(true);
        Page<Room> rooms = roomRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(rooms, roomMapper::fromEntityToRoomDtoList), "List my rooms success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Room] not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        if (!isAdmin() || !Objects.equals(room.getHost().getId(), getCurrentUser())) {
            throw new UnauthorizationException("Not allow");
        }
        if (!Objects.equals(room.getState(), BaseConstant.ROOM_STATE_ENDING)) {
            throw new BadRequestException("[Room] room state invalid", ErrorCode.ROOM_ERROR_INVALID_STATE);
        }

        participantRepository.deleteByRoomId(id);
        chatRepository.deleteByRoomId(id);
        roomRepository.delete(room);
        return makeSuccessResponse("Delete room successfully");
    }

    @ApiIgnore
    @Transactional
    @DeleteMapping(value = "/reset-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> resetData() {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("[Room] Unauthorized");
        }
        chatRepository.deleteAllInBatch();
        participantRepository.deleteAllInBatch();
        roomRepository.deleteAllInBatch();
        return makeSuccessResponse("Reset room data success");
    }

    @ApiIgnore
    @PostMapping(value = "/test-left-participant", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> testLeftParticipant(@Valid @RequestBody TestLeftParticipantForm form) {
        roomService.publishParticipantLeft(form.getRoomId(), form.getAccountId());
        return makeSuccessResponse("Test participant left message sent");
    }

    private String generateRoomCode() {
        for (int i = 0; i < MAX_CODE_GENERATION_ATTEMPTS; i++) {
            String code = PasswordUtils.generateRoomCode();
            if (!roomRepository.existsByCodeAndStateNot(code, BaseConstant.ROOM_STATE_ENDING)) {
                return code;
            }
        }
        throw new BadRequestException("[Room] code existed", ErrorCode.ROOM_ERROR_CODE_EXISTED);
    }

    private boolean isValidRoomMovieItem(MovieItem movieItem) {
        if (Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_TRAILER)) {
            return false;
        }

        if (movieItem.getStatus() != BaseConstant.STATUS_ACTIVE) {
            log.warn("===> Invalid MovieItem with id {} is not active", movieItem.getId());
            return false;
        }

        if (movieItem.getVideo() == null) {
            log.warn("===> Invalid MovieItem with id {} has no video", movieItem.getId());
            return false;
        }

        Movie movie = movieItem.getMovie();
        if (movie == null || movie.getStatus() != BaseConstant.STATUS_ACTIVE) {
            log.warn("===> Invalid MovieItem with id {} has invalid movie", movieItem.getId());
            return false;
        }

        boolean isSingleWithSeason = Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SINGLE)
                && Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON);
        boolean isSeriesWithEpisode = Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SERIES)
                && Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_EPISODE);
        if (!isSingleWithSeason && !isSeriesWithEpisode) {
            log.warn("===> Invalid MovieItem id {} — movie type: {}, item kind: {}",
                    movieItem.getId(), movie.getType(), movieItem.getKind());
            return false;
        }
        return true;
    }
}
