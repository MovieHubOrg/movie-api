package com.movie.api.constant;

import java.util.Set;

public class BaseConstant {
    public static final Integer ACCOUNT_KIND_ADMIN = 1;
    public static final Integer ACCOUNT_KIND_EMPLOYEE = 2;
    public static final Integer ACCOUNT_KIND_USER = 3;

    public static final int PLATFORM_IOS = 1;
    public static final int PLATFORM_ANDROID = 2;

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_LOCK = -1;
    public static final Integer STATUS_DELETE = -2;

    public static final Integer MOVIE_TYPE_SINGLE = 1;
    public static final Integer MOVIE_TYPE_SERIES = 2;

    public static final Integer MOVIE_ITEM_KIND_SEASON = 1;
    public static final Integer MOVIE_ITEM_KIND_EPISODE = 2;
    public static final Integer MOVIE_ITEM_KIND_TRAILER = 3;

    public static final Integer AGE_RATING_GENERAL = 1;   // G - General Audience
    public static final Integer AGE_RATING_PG = 2;        // PG - Parental Guidance
    public static final Integer AGE_RATING_PG13 = 3;      // PG-13 - Not under 13
    public static final Integer AGE_RATING_R = 4;         // R - Restricted (under 17 needs adult)
    public static final Integer AGE_RATING_NC17 = 5;      // NC-17 - No one 17 and under admitted
    public static final Integer AGE_RATING_18_PLUS = 6;   // 18+ - Local classification

    public static final String APP_ID_GENERATOR_NAME = "idGenerator";
    public static final String APP_ID_GENERATOR_STRATEGY = "com.movie.api.storage.id.IdGenerator";

    public static final String HEADER_CLIENT_TYPE = "X-Client-Type";
    public static final String HEADER_CLIENT_TYPE_WEB = "WEB";
    public static final String HEADER_X_API_KEY = "X-Api-Key";

    public static final String DOWNLOAD_MEDIA_API = "https://media-api.moviehub.biz/v1/file/download";

    public static final String USERNAME_PATTERN = "^(?=.{3,20}$)(?!.*[_.]{2})[a-zA-Z][a-zA-Z0-9_]*[a-zA-Z0-9]$";
    public static final String PHONE_PATTERN = "^0[35789][0-9]{8}$";
    public static final String EMAIL_PATTERN = "^(?!.*[.]{2,})[a-zA-Z0-9.%]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
    public static final String COLOR_PATTERN = "^#[0-9A-Fa-f]{6}$";

    public static final Integer GENDER_MALE = 1;
    public static final Integer GENDER_FEMALE = 2;
    public static final Integer GENDER_OTHER = 3;

    public static final Integer PERSON_KIND_ACTOR = 1;
    public static final Integer PERSON_KIND_DIRECTOR = 2;

    public static final Integer VIDEO_LIBRARY_STATE_PROCESSING = 0;
    public static final Integer VIDEO_LIBRARY_STATE_READY = 1;
    public static final Integer VIDEO_LIBRARY_STATE_ERROR = 2;

    // CMD VIDEO
    public static final String CMD_DONE_CONVERT_VIDEO = "CMD_DONE_CONVERT_VIDEO";
    public static final String CMD_CONVERT_VIDEO = "CMD_CONVERT_VIDEO";
    public static final String CMD_DELETE_VIDEO = "CMD_DELETE_VIDEO";
    public static final String CMD_UPDATE_SERVER_CONFIG = "CMD_UPDATE_SERVER_CONFIG";
    public static final String CMD_SEND_NOTIFICATION = "CMD_SEND_NOTIFICATION";
    public static final String CMD_NEW_MOVIE = "CMD_NEW_MOVIE";
    public static final String CMD_NEW_MOVIE_ITEM = "CMD_NEW_MOVIE_ITEM";
    public static final String CMD_REPLY_COMMENT = "CMD_REPLY_COMMENT";
    public static final String CMD_PARTICIPANT_LEFT = "CMD_PARTICIPANT_LEFT";
    public static final String CMD_END_ROOM = "CMD_END_ROOM";
    public static final String CMD_UPDATE_PARTICIPANT_COUNT = "CMD_UPDATE_PARTICIPANT_COUNT";
    public static final String CMD_CREATE_CHAT = "CMD_CREATE_CHAT";
    public static final String CMD_CLIENT_PING = "CMD_CLIENT_PING";

    public static final Set<String> ONE_SIGNAL_ALLOWED_CMD = Set.of(
            BaseConstant.CMD_NEW_MOVIE,
            BaseConstant.CMD_NEW_MOVIE_ITEM,
            BaseConstant.CMD_REPLY_COMMENT
    );

    public static final Integer NOTIFICATION_TARGET_TYPE_APP = 1;
    public static final Integer NOTIFICATION_TARGET_TYPE_ACCOUNT = 2;

    public static final Integer SEND_NOTIFICATION_FOR_ALL_USERS = 1;
    public static final Integer SEND_NOTIFICATION_FOR_INTERESTED_USERS = 2;

    public static final Integer NOTIFICATION_TYPE_CMS = 1;
    public static final Integer NOTIFICATION_TYPE_MOVIE = 2;
    public static final Integer NOTIFICATION_TYPE_COMMUNITY = 3;

    // Account Events
    public static final String ACCOUNT_EVENT_CREATED = "ACCOUNT_CREATED";
    public static final String ACCOUNT_EVENT_UPDATED = "ACCOUNT_UPDATED";
    public static final String ACCOUNT_EVENT_DELETED = "ACCOUNT_DELETED";
    public static final String ACCOUNT_EVENT_STATUS_CHANGED = "ACCOUNT_STATUS_CHANGED";

    public static final String APP_CMS = "cms";
    public static final String APP_MOVIE = "movie";

    public static final Boolean SIDEBAR_ACTIVE_TRUE = true;
    public static final boolean SIDEBAR_ACTIVE_FALSE = false;

    public static final Integer REACTION_TYPE_LIKE = 1;
    public static final Integer REACTION_TYPE_DISLIKE = 2;

    public static final Integer FAVOURITE_TYPE_MOVIE = 1;
    public static final Integer FAVOURITE_TYPE_PERSON = 2;

    public static final Integer SOURCE_TYPE_INTERNAL = 1;
    public static final Integer SOURCE_TYPE_EXTERNAL = 2;

    public static final Integer COLLECTION_TYPE_TOPIC = 1;
    public static final Integer COLLECTION_TYPE_SECTION = 2;

    public static final Integer JSON_TYPE_FILTER_MOVIE = 1;

    public static final String FILTER_MOVIE_SAMPLE_DATA = "{\"type\":1,\"ageRating\":1,\"language\":\"en-US\",\"country\":\"US\",\"isFeatured\":true,\"categoryIds\":[101,102,103],\"comingSoon\":true,\"limit\":10}";

    public static final Integer ACTION_DELETE_FROM_PLAYLIST = 0;
    public static final Integer ACTION_ADD_TO_PLAYLIST = 1;

    public static final Integer ACTION_DELETE= 0;
    public static final Integer ACTION_ADD = 1;

    public static final Integer MAX_PLAYLIST_PER_USER = 5;

    public static final Integer USER_MOVIE_TYPE_INTERESTED = 1;
    public static final Integer USER_MOVIE_TYPE_WATCHED = 2;

    public static final Integer ROOM_KIND_PRIVATE = 0;
    public static final Integer ROOM_KIND_PUBLIC = 1;

    public static final Integer ROOM_STATE_PENDING = 0;
    public static final Integer ROOM_STATE_RUNNING = 1;
    public static final Integer ROOM_STATE_ENDING = 2;

    public static final String ROOM_END = "ROOM_END";
    public static final String ROOM_TIMEOUT = "ROOM_TIMEOUT";
    public static final String HOST_LEFT = "HOST_LEFT";

    public static final Integer PARTICIPANT_ROLE_GUEST = 0;
    public static final Integer PARTICIPANT_ROLE_HOST = 1;

    public static final Integer PARTICIPANT_STATE_PENDING = 0;
    public static final Integer PARTICIPANT_STATE_JOIN = 1;
    public static final Integer PARTICIPANT_STATE_LEFT = 2;

    public static final int MQTT_QOS_LEVEL_0 = 0; // Fire and forget
    public static final int MQTT_QOS_LEVEL_1 = 1; // At least once
    public static final int MQTT_QOS_LEVEL_2 = 2; // Exactly once

    public static final String SETTING_KEY_LIVE_ROOM_EXTRA_ENDING_TIME = "live_room_extra_ending_time";

    private BaseConstant(){
        throw new IllegalStateException("Utility class");
    }

}
