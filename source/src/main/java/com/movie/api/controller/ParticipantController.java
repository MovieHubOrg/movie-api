package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.participant.ParticipantDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.exception.UnauthorizationException;
import com.movie.api.form.participant.CreateParticipantForm;
import com.movie.api.mapper.ParticipantMapper;
import com.movie.api.storage.criteria.ParticipantCriteria;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.model.Participant;
import com.movie.api.storage.model.Room;
import com.movie.api.storage.repository.AccountRepository;
import com.movie.api.storage.repository.ParticipantRepository;
import com.movie.api.storage.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/participant")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ParticipantController extends ABasicController {
    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateParticipantForm form) {
        Room room = roomRepository.findByIdAndStatus(form.getRoomId(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Room] not found", ErrorCode.ROOM_ERROR_NOT_FOUND));

        validateHostRoom(room);
        if (Objects.equals(room.getState(), BaseConstant.ROOM_STATE_ENDING)) {
            throw new BadRequestException("[Room] room state invalid", ErrorCode.ROOM_ERROR_INVALID_STATE);
        }

        List<Long> accountIds = form.getAccountIds().stream()
                .distinct()
                .collect(Collectors.toList());

        List<Account> accounts = accountRepository.findAllByIdInAndKindAndStatus(
                accountIds,
                BaseConstant.ACCOUNT_KIND_USER,
                BaseConstant.STATUS_ACTIVE
        );
        if (accounts.size() != accountIds.size()) {
            throw new NotFoundException("[User] one or more users not found", ErrorCode.USER_ERROR_NOT_FOUND);
        }

        Map<Long, Account> accountMap = accounts.stream()
                .collect(Collectors.toMap(Account::getId, account -> account));
        Map<Long, Participant> existingParticipantMap = participantRepository
                .findAllByRoomIdAndUserIdIn(room.getId(), accountIds)
                .stream()
                .collect(Collectors.toMap(participant -> participant.getUser().getId(), participant -> participant));

        List<Participant> participants = new ArrayList<>();
        for (Long accountId : accountIds) {
            Account account = accountMap.get(accountId);
            Participant participant = existingParticipantMap.get(accountId);
            if (participant == null) {
                participant = new Participant();
                participant.setRoom(room);
                participant.setUser(account);
                participant.setRole(BaseConstant.PARTICIPANT_ROLE_GUEST);
            }
            participants.add(participant);
        }
        participantRepository.saveAll(participants);
        return makeSuccessResponse("Create participant success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ParticipantDto> get(@PathVariable Long id) {
        Participant participant = participantRepository.findByIdAndStatus(id, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Participant] not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        validateHostRoom(participant.getRoom());
        return makeSuccessResponse(participantMapper.entityToParticipantDto(participant), "Get participant success.");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ParticipantDto>>> list(ParticipantCriteria criteria, Pageable pageable) {
        criteria.setHostId(getCurrentUser());
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        Page<Participant> participants = participantRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(
                makeResponseListDto(participants, participantMapper::fromEntityToParticipantDtoList),
                "List participant success"
        );
    }

    private void validateHostRoom(Room room) {
        if (!Objects.equals(room.getHost().getId(), getCurrentUser())) {
            throw new UnauthorizationException("Not allow");
        }
    }
}
