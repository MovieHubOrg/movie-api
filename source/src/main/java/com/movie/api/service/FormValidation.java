package com.movie.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.form.ErrorForm;
import com.movie.api.form.room.CreateChatForm;
import com.movie.api.form.room.ParticipantLeftForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FormValidation {
    @Autowired
    private ObjectMapper objectMapper;
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static final Map<String, Class<?>> FORM_MAPS = new HashMap<>() {{
        put(BaseConstant.CMD_PARTICIPANT_LEFT, ParticipantLeftForm.class);
        put(BaseConstant.CMD_CREATE_CHAT, CreateChatForm.class);
    }};

    private <T> List<ErrorForm> validate(T form) {
        return validator.validate(form).stream()
                .map(violation -> new ErrorForm(
                        violation.getPropertyPath().toString(),
                        violation.getMessage())
                )
                .collect(Collectors.toList());
    }

    public ApiMessageDto<List<ErrorForm>> validateForm(String cmd, Object data) {
        Class<?> form = FORM_MAPS.get(cmd);
        if (form == null) {
            return null;
        }
        List<ErrorForm> errors = validate(objectMapper.convertValue(data, (Class<?>) form));
        if (errors.isEmpty()) {
            return null;
        }
        ApiMessageDto<List<ErrorForm>> dto = new ApiMessageDto<>();
        dto.setResult(false);
        dto.setCode("ERROR-VALIDATION");
        dto.setData(errors);
        dto.setMessage("Invalid Form");
        return dto;
    }
}
