package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.chat.ChatDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.room.mqtt.TestChatForm;
import com.movie.api.mapper.ChatMapper;
import com.movie.api.service.RoomService;
import com.movie.api.storage.criteria.ChatCriteria;
import com.movie.api.storage.model.Chat;
import com.movie.api.storage.model.Room;
import com.movie.api.storage.repository.ChatRepository;
import com.movie.api.storage.repository.RoomRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatMapper chatMapper;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private ChatController chatController;

    // ---------- list ----------

    @Test
    void list_whenRoomIdMissing_throwsBadRequestException() {
        ChatCriteria criteria = new ChatCriteria();
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> chatController.list(criteria, pageable))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void list_whenRoomNotFound_throwsNotFoundException() {
        ChatCriteria criteria = new ChatCriteria();
        criteria.setRoomId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        when(roomRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatController.list(criteria, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ROOM_ERROR_NOT_FOUND);
    }

    @Test
    void list_whenRoomFound_returnsSuccessResponseWrappingList() {
        ChatCriteria criteria = new ChatCriteria();
        criteria.setRoomId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        Room room = new Room();
        room.setId(1L);
        when(roomRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(room));

        Chat chat = new Chat();
        chat.setId(1L);
        Page<Chat> page = new PageImpl<>(Collections.singletonList(chat));
        List<ChatDto> dtoList = Collections.singletonList(new ChatDto());

        when(chatRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(chatMapper.fromEntityToChatDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<ChatDto>>> response = chatController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(response.getData().getTotalElements()).isEqualTo(1);
        assertThat(criteria.getRoomId()).isEqualTo(1L);
    }

    // ---------- testChat ----------

    @Test
    void testChat_publishesChatMessageAndReturnsSuccess() {
        TestChatForm form = new TestChatForm();
        form.setRoomId(1L);
        form.setAccountId(2L);
        form.setContent("hello");

        ApiMessageDto<Void> response = chatController.testChat(form);

        assertThat(response.getResult()).isTrue();
        verify(roomService, times(1)).publishCreateChat(1L, 2L, "hello");
    }
}
