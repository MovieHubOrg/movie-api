package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.chat.ChatDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.room.TestChatForm;
import com.movie.api.mapper.ChatMapper;
import com.movie.api.service.RoomService;
import com.movie.api.storage.criteria.ChatCriteria;
import com.movie.api.storage.model.Chat;
import com.movie.api.storage.model.Room;
import com.movie.api.storage.repository.ChatRepository;
import com.movie.api.storage.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ChatController extends ABasicController {
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ChatDto>>> list(ChatCriteria criteria, Pageable pageable) {
        if (criteria.getRoomId() == null) {
            throw new BadRequestException("roomId is required");
        }
        Room room = roomRepository.findByIdAndStatus(criteria.getRoomId(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Room] not found", ErrorCode.ROOM_ERROR_NOT_FOUND));
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.asc("createdDate"))
        );
        criteria.setRoomId(room.getId());
        Page<Chat> chats = chatRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(chats, chatMapper::fromEntityToChatDtoList), "List chat success");
    }

    @ApiIgnore
    @PostMapping(value = "/test-chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> testChat(@Valid @RequestBody TestChatForm form) {
        roomService.publishCreateChat(form.getRoomId(), form.getAccountId(), form.getContent());
        return makeSuccessResponse("Test chat message sent");
    }
}
