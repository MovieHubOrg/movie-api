package com.movie.api.controller;

import com.movie.api.dto.ApiMessageDto;
import com.movie.api.form.sns.BaseSendSignalPayloadForm;
import com.movie.api.form.sns.SendSignalSnsForm;
import com.movie.api.service.SnsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/sns")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SnsController extends ABasicController {
    @Autowired
    private SnsService snsService;

    @PostMapping(value = "/send-signal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> sendSignal(@Valid @RequestBody SendSignalSnsForm form, BindingResult bindingResult) {
        BaseSendSignalPayloadForm<String> signalPayload = new BaseSendSignalPayloadForm<>();
        signalPayload.setCmd(form.getCmd());
        signalPayload.setSubCmd(form.getSubCmd());
        signalPayload.setData(form.getPayload());

        snsService.sendSignal(signalPayload, form.getUserKind());
        return makeSuccessResponse("Send signal successfully");
    }
}
