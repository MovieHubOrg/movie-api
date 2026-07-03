package com.movie.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.form.ErrorForm;
import com.movie.api.form.mqtt.BaseSendMsgForm;
import com.movie.api.form.room.mqtt.*;
import com.movie.api.service.mqtt.MqttOutboundService;
import com.movie.api.storage.model.Chat;
import com.movie.api.storage.model.Participant;
import com.movie.api.storage.model.Room;
import com.movie.api.storage.repository.ChatRepository;
import com.movie.api.storage.repository.ParticipantRepository;
import com.movie.api.storage.repository.RoomRepository;
import com.movie.api.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movie.api.dto.participant.ParticipantDto;
import com.movie.api.mapper.ParticipantMapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {
    private static final long HOST_INACTIVE_TIMEOUT_MILLIS = 60 * 1000L;

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FormValidation formValidation;
    @Autowired
    private ParticipantMapper participantMapper;
    @Autowired
    private MqttOutboundService mqttOutboundService;
    @Value("${mqtt.topic.room.prefix}")
    private String roomTopicPrefix;

    @Transactional
    public void handleRoomMessage(Long roomId, String payload) throws JsonProcessingException {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || !Objects.equals(room.getState(), BaseConstant.ROOM_STATE_RUNNING)) {
            log.warn("Ignore MQTT message because room {} does not exist or is inactive", roomId);
            return;
        }

        BaseSendMsgForm<JsonNode> messageForm;
        try {
            messageForm = objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.warn("Invalid MQTT payload for room {}", roomId, e);
            return;
        }

        if (messageForm.getCmd() == null) {
            log.warn("Ignore MQTT message for room {} because cmd is missing", roomId);
            return;
        }

        ApiMessageDto<List<ErrorForm>> validationResult = formValidation.validateForm(messageForm.getCmd(), messageForm.getData());
        if (validationResult != null) {
            log.warn("Invalid MQTT payload for room {} and cmd {}: {}", roomId, messageForm.getCmd(), validationResult.getData());
            return;
        }

        switch (messageForm.getCmd()) {
            case BaseConstant.CMD_PARTICIPANT_LEFT:
                ParticipantLeftForm participantLeftForm = objectMapper.treeToValue(messageForm.getData(), ParticipantLeftForm.class);
                handleParticipantLeft(room, participantLeftForm);
                break;
            case BaseConstant.CMD_CREATE_CHAT:
                CreateChatForm createChatForm = objectMapper.treeToValue(messageForm.getData(), CreateChatForm.class);
                handleCreateChat(room, createChatForm);
                break;
            case BaseConstant.CMD_CLIENT_PING:
                ClientPingForm clientPingForm = objectMapper.treeToValue(messageForm.getData(), ClientPingForm.class);
                handleClientPing(room, clientPingForm);
                break;
            default:
                log.warn("Unknown room MQTT cmd {} for room {}", messageForm.getCmd(), roomId);
        }
    }

    private void handleParticipantLeft(Room room, ParticipantLeftForm participantLeftForm) {
        Long accountId = ConvertUtils.convertStringToLong(participantLeftForm.getAccountId());
        if (accountId == null) {
            return;
        }
        Participant participant = participantRepository.findByRoomIdAndUserId(room.getId(), accountId).orElse(null);
        if (participant == null) {
            log.warn("Participant {} not found in room {}", accountId, room.getId());
            return;
        }
        if (Objects.equals(participant.getState(), BaseConstant.PARTICIPANT_STATE_LEFT)) {
            log.warn("Participant {} in room {} is already left", accountId, room.getId());
            return;
        }
        participant.setState(BaseConstant.PARTICIPANT_STATE_LEFT);
        participantRepository.save(participant);
        log.info("Participant {} left room {}", accountId, room.getId());

        boolean isHost = Objects.equals(room.getHost().getId(), accountId);
        if (isHost) {
            endRoom(room, BaseConstant.HOST_LEFT);
            return;
        }
        // Guest left → publish current viewer count
        publishCurrentViewerCount(room);
    }

    private void handleCreateChat(Room room, CreateChatForm createChatForm) {
        Long accountId = ConvertUtils.convertStringToLong(createChatForm.getAccountId());
        if (accountId == null) {
            return;
        }
        Participant participant = participantRepository.findByRoomIdAndUserId(room.getId(), accountId).orElse(null);
        if (participant == null) {
            log.warn("Ignore chat because participant {} not found in room {}", accountId, room.getId());
            return;
        }
        if (!Objects.equals(participant.getState(), BaseConstant.PARTICIPANT_STATE_JOIN)) {
            log.warn("Ignore chat because participant {} is not joined in room {}", accountId, room.getId());
            return;
        }
        if (participant.getUser() == null) {
            log.warn("Ignore chat because participant {} has no user in room {}", accountId, room.getId());
            return;
        }

        Chat chat = new Chat();
        chat.setRoom(room);
        chat.setUser(participant.getUser());
        chat.setContent(createChatForm.getContent());
        chatRepository.save(chat);
        log.info("Saved chat {} for room {} from account {}", chat.getId(), room.getId(), accountId);
    }

    private void handleClientPing(Room room, ClientPingForm clientPingForm) {
        Long accountId = ConvertUtils.convertStringToLong(clientPingForm.getAccountId());
        if (accountId == null) {
            return;
        }
        if (!Objects.equals(room.getHost().getId(), accountId)) {
            log.warn("Ignore client ping because account {} is not host of room {}", accountId, room.getId());
            return;
        }

        room.setLastTimeOnline(new Date());
        roomRepository.save(room);
        log.debug("Updated lastTimeOnline for room {} by host {}", room.getId(), accountId);
    }

    public int endTimedOutRunningRooms() {
        Date now = new Date();
        Date hostInactiveBefore = new Date(now.getTime() - HOST_INACTIVE_TIMEOUT_MILLIS);
        List<Room> roomsToEnd = roomRepository.findRunningRoomsToEnd(
                BaseConstant.ROOM_STATE_RUNNING,
                now,
                hostInactiveBefore
        );
        if (roomsToEnd.isEmpty()) {
            return 0;
        }

        int endedRooms = 0;
        log.info("Found {} running room(s) to end at {}", roomsToEnd.size(), now);
        for (Room room : roomsToEnd) {
            try {
                String reason = resolveEndRoomReason(room, now, hostInactiveBefore);
                if (endRoom(room, reason)) {
                    endedRooms++;
                }
            } catch (Exception e) {
                log.error("Failed to end running room {}", room.getId(), e);
            }
        }
        return endedRooms;
    }

    public int deleteExpiredPendingRooms() {
        Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        List<Room> expiredRooms = roomRepository.findExpiredPendingRooms(BaseConstant.ROOM_STATE_PENDING, oneDayAgo);
        if (expiredRooms.isEmpty()) {
            return 0;
        }
        List<Long> ids = expiredRooms.stream().map(Room::getId).collect(Collectors.toList());
        participantRepository.deleteByRoomIdIn(ids);
        roomRepository.deleteByIdIn(ids);
        return ids.size();
    }

    public boolean endRoom(Room room, String reason) {
        if (!BaseConstant.ROOM_STATE_RUNNING.equals(room.getState())) {
            log.info("Skip ending room {} because it is no longer running", room.getId());
            return false;
        }

        room.setState(BaseConstant.ROOM_STATE_ENDING);
        room.setEndTime(new Date());
        room.setReasonEnd(reason);
        roomRepository.save(room);
        log.info("Room {} ended with reason {}", room.getId(), reason);

        // 2. Update tất cả participant còn JOINED → LEFT
        participantRepository.updateStateByRoomIdAndState(
                room.getId(),
                BaseConstant.PARTICIPANT_STATE_JOIN,
                BaseConstant.PARTICIPANT_STATE_LEFT
        );

        EndRoomForm endRoomForm = new EndRoomForm();
        endRoomForm.setRoomId(String.valueOf(room.getId()));
        endRoomForm.setReason(reason);
        publishToRoom(room.getId(), BaseConstant.CMD_END_ROOM, endRoomForm);
        return true;
    }

    public void publishCurrentViewerCount(Room room) {
        List<Participant> joinedParticipants = participantRepository.findByRoomIdAndState(room.getId(), BaseConstant.PARTICIPANT_STATE_JOIN);
        List<ParticipantDto> participantDtos = participantMapper.fromEntityToParticipantDtoForRoomList(joinedParticipants);
        UpdateParticipantCountForm form = new UpdateParticipantCountForm();
        form.setRoomId(String.valueOf(room.getId()));
        form.setCurrentViewers(joinedParticipants.size());
        form.setParticipants(participantDtos);
        publishToRoom(room.getId(), BaseConstant.CMD_UPDATE_PARTICIPANT_COUNT, form);
        log.info("Room {} current viewers: {}", room.getId(), joinedParticipants.size());
    }

    public void publishParticipantLeft(Long roomId, Long accountId) {
        ParticipantLeftForm form = new ParticipantLeftForm();
        form.setAccountId(accountId.toString());
        publishToRoom(roomId, BaseConstant.CMD_PARTICIPANT_LEFT, form);
        log.info("Published participant left test message for room {} account {}", roomId, accountId);
    }

    public void publishCreateChat(Long roomId, Long accountId, String content) {
        CreateChatForm form = new CreateChatForm();
        form.setAccountId(accountId.toString());
        form.setContent(content);
        publishToRoom(roomId, BaseConstant.CMD_CREATE_CHAT, form);
        log.info("Published create chat test message for room {} account {}", roomId, accountId);
    }

    private String resolveEndRoomReason(Room room, Date now, Date hostInactiveBefore) {
        if (room.getEndTime() != null && !room.getEndTime().after(now)) {
            return BaseConstant.ROOM_TIMEOUT;
        }
        if (room.getLastTimeOnline() != null && !room.getLastTimeOnline().after(hostInactiveBefore)) {
            return BaseConstant.HOST_LEFT;
        }
        return BaseConstant.ROOM_TIMEOUT;
    }

    private <T> void publishToRoom(Long roomId, String cmd, T data) {
        String topic = roomTopicPrefix + "/" + roomId;
        mqttOutboundService.sendToClient(topic, cmd, data, BaseConstant.MQTT_QOS_LEVEL_2);
    }
}
