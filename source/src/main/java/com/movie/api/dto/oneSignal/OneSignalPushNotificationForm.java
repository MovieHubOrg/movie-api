package com.movie.api.dto.oneSignal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;

@Data
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OneSignalPushNotificationForm {
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("contents")
    private Content contents;
    @JsonProperty("headings")
    private Content headings;
    @JsonProperty("include_aliases")
    private IncludeAliases includeAliases;
    @JsonProperty("target_channel")
    private String targetChannel;
    @JsonProperty("data")
    private AdditionalData data;
    @JsonProperty("big_picture")
    private String bigPicture;
}
