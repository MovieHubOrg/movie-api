package com.movie.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.form.ErrorForm;
import com.movie.api.form.mqtt.BaseSendMsgForm;
import com.movie.api.form.room.CreateChatForm;
import com.movie.api.form.room.EndRoomForm;
import com.movie.api.form.room.ParticipantLeftForm;
import com.movie.api.form.room.UpdateParticipantCountForm;
import com.movie.api.service.mqtt.MqttOutboundService;
import com.movie.api.storage.model.Chat;
import com.movie.api.storage.model.Participant;
import com.movie.api.storage.model.Room;
import com.movie.api.storage.repository.ChatRepository;
import com.movie.api.storage.repository.ParticipantRepository;
import com.movie.api.storage.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class RoomService {
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
            default:
                log.warn("Unknown room MQTT cmd {} for room {}", messageForm.getCmd(), roomId);
        }
    }

    private void handleParticipantLeft(Room room, ParticipantLeftForm participantLeftForm) {
        Long accountId = participantLeftForm.getAccountId();
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
            endRoom(room, "HOST_LEFT");
            return;
        }
        // Guest left → publish current viewer count
        publishCurrentViewerCount(room);
    }

    private void handleCreateChat(Room room, CreateChatForm createChatForm) {
        Long accountId = createChatForm.getAccountId();
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

    public int endExpiredRunningRooms() {
        Date now = new Date();
        List<Room> expiredRooms = roomRepository.findAllByStateAndEndTimeLessThanEqual(BaseConstant.ROOM_STATE_RUNNING, now);
        if (expiredRooms.isEmpty()) {
            return 0;
        }

        int endedRooms = 0;
        log.info("Found {} expired running room(s) at {}", expiredRooms.size(), now);
        for (Room room : expiredRooms) {
            try {
                if (endRoom(room, "ROOM_TIMEOUT")) {
                    endedRooms++;
                }
            } catch (Exception e) {
                log.error("Failed to end expired room {}", room.getId(), e);
            }
        }
        return endedRooms;
    }

    public boolean endRoom(Room room, String reason) {
        Date endedAt = new Date();
        int updated = roomRepository.updateStateAndEndTimeByIdAndState(
                room.getId(),
                BaseConstant.ROOM_STATE_RUNNING,
                BaseConstant.ROOM_STATE_ENDING,
                endedAt
        );
        if (updated == 0) {
            log.info("Skip ending room {} because it is no longer running", room.getId());
            return false;
        }

        room.setState(BaseConstant.ROOM_STATE_ENDING);
        room.setEndTime(endedAt);
        log.info("Room {} ended with reason {}", room.getId(), reason);

        // 2. Update tất cả participant còn JOINED → LEFT
        participantRepository.updateStateByRoomIdAndState(
                room.getId(),
                BaseConstant.PARTICIPANT_STATE_JOIN,
                BaseConstant.PARTICIPANT_STATE_LEFT
        );

        EndRoomForm endRoomForm = new EndRoomForm();
        endRoomForm.setRoomId(room.getId());
        endRoomForm.setReason(reason);
        publishToRoom(room.getId(), BaseConstant.CMD_END_ROOM, endRoomForm);
        return true;
    }

    public void publishCurrentViewerCount(Room room) {
        int currentViewers = participantRepository.countByRoomIdAndState(room.getId(), BaseConstant.PARTICIPANT_STATE_JOIN);
        UpdateParticipantCountForm form = new UpdateParticipantCountForm();
        form.setRoomId(room.getId());
        form.setCurrentViewers(currentViewers);
        publishToRoom(room.getId(), BaseConstant.CMD_UPDATE_PARTICIPANT_COUNT, form);
        log.info("Room {} current viewers: {}", room.getId(), currentViewers);
    }

    public void publishParticipantLeft(Long roomId, Long accountId) {
        ParticipantLeftForm form = new ParticipantLeftForm();
        form.setAccountId(accountId);
        publishToRoom(roomId, BaseConstant.CMD_PARTICIPANT_LEFT, form);
        log.info("Published participant left test message for room {} account {}", roomId, accountId);
    }

    public void publishCreateChat(Long roomId, Long accountId, String content) {
        CreateChatForm form = new CreateChatForm();
        form.setAccountId(accountId);
        form.setContent(content);
        publishToRoom(roomId, BaseConstant.CMD_CREATE_CHAT, form);
        log.info("Published create chat test message for room {} account {}", roomId, accountId);
    }

    private <T> void publishToRoom(Long roomId, String cmd, T data) {
        String topic = roomTopicPrefix + "/" + roomId;
        mqttOutboundService.sendToClient(topic, cmd, data, BaseConstant.MQTT_QOS_LEVEL_0);
    }
}
