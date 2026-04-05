package com.movie.api.dto;


import lombok.Data;

@Data
public class ErrorCode {
    /**
     * Starting error code Service Category
     */
    public static final String CATEGORY_ERROR_NOT_FOUND = "ERROR-CATEGORY-ERROR-0000";
    public static final String CATEGORY_ERROR_NAME_EXISTED = "ERROR-CATEGORY-ERROR-0002";
    public static final String CATEGORY_ERROR_HAS_MOVIE = "ERROR-CATEGORY-ERROR-0003";

    /**
     * Starting error code Employee
     */
    public static final String ACCOUNT_ERROR_NOT_FOUND = "ERROR-ACCOUNT-ERROR-0000";

    /**
     * Starting error code Survey
     */
    public static final String SURVEY_ERROR_MIN_MOVIES = "ERROR-SURVEY-ERROR-0000";

    /**
     * Starting error code Employee
     */
    public static final String EMPLOYEE_ERROR_NOT_FOUND = "ERROR-EMPLOYEE-ERROR-0000";
    public static final String EMPLOYEE_ERROR_USERNAME_EXISTED = "ERROR-EMPLOYEE-ERROR-0002";
    public static final String EMPLOYEE_ERROR_PHONE_EXISTED = "ERROR-EMPLOYEE-ERROR-0003";
    public static final String EMPLOYEE_ERROR_EMAIL_EXISTED = "ERROR-EMPLOYEE-ERROR-0004";
    public static final String EMPLOYEE_ERROR_WRONG_PASSWORD = "ERROR-EMPLOYEE-ERROR-0005";
    public static final String EMPLOYEE_ERROR_NEW_PASSWORD_SAME_OLD_PASSWORD = "ERROR-EMPLOYEE-ERROR-0006";

    /**
     * Starting error code User
     */
    public static final String USER_ERROR_NOT_FOUND = "ERROR-USER-ERROR-0000";
    public static final String USER_ERROR_USERNAME_EXISTED = "ERROR-USER-ERROR-0002";
    public static final String USER_ERROR_PHONE_EXISTED = "ERROR-USER-ERROR-0003";
    public static final String USER_ERROR_EMAIL_EXISTED = "ERROR-USER-ERROR-0004";
    public static final String USER_ERROR_WRONG_PASSWORD = "ERROR-USER-ERROR-0005";
    public static final String USER_ERROR_NEW_PASSWORD_SAME_OLD_PASSWORD = "ERROR-USER-ERROR-0006";
    public static final String USER_ERROR_OTP_INVALID = "ERROR-USER-ERROR-0007";
    public static final String USER_ERROR_RESEND_OTP_LIMIT = "ERROR-USER-ERROR-0008";
    public static final String USER_ERROR_CONFIRM_PASSWORD_INVALID = "ERROR-USER-ERROR-0009";

    /**
     * Starting error code Group
     */
    public static final String GROUP_ERROR_NOT_FOUND = "ERROR-GROUP-ERROR-0001";
    public static final String GROUP_ERROR_NAME_EXISTED = "ERROR-GROUP-ERROR-0002";

    /**
     * Starting error code Permission
     */
    public static final String PERMISSION_ERROR_NOT_FOUND = "ERROR-PERMISSION-ERROR-0001";
    public static final String PERMISSION_ERROR_NAME_EXISTED = "ERROR-PERMISSION-ERROR-0002";
    public static final String PERMISSION_ERROR_CODE_EXISTED = "ERROR-PERMISSION-ERROR-0003";

    /**
     * Starting error code Video Library
     */
    public static final String VIDEO_LIBRARY_ERROR_NOT_FOUND = "ERROR-VIDEO-LIBRARY-ERROR-0000";
    public static final String VIDEO_LIBRARY_ERROR_NAME_EXISTED = "ERROR-VIDEO-LIBRARY-ERROR-0002";
    public static final String VIDEO_LIBRARY_ERROR_MOVIE_ITEM_EXISTED = "ERROR-VIDEO-LIBRARY-ERROR-0003";
    public static final String VIDEO_LIBRARY_ERROR_DURATION_INVALID = "ERROR-VIDEO-LIBRARY-ERROR-0004";

    /**
     * Starting error code Movie
     */
    public static final String MOVIE_ERROR_NOT_FOUND = "ERROR-MOVIE-ERROR-0000";
    public static final String MOVIE_ERROR_SLUG_EXISTED = "ERROR-MOVIE-ERROR-0002";
    public static final String MOVIE_ERROR_HAS_ITEM = "ERROR-MOVIE-ERROR-0003";

    /**
     * Starting error code Movie Item
     */
    public static final String MOVIE_ITEM_ERROR_NOT_FOUND = "ERROR-MOVIE-ITEM-ERROR-0000";
    public static final String MOVIE_ITEM_ERROR_PARENT_REQUIRED = "ERROR-MOVIE-ITEM-ERROR-0002";
    public static final String MOVIE_ITEM_ERROR_VIDEO_REQUIRED = "ERROR-MOVIE-ITEM-ERROR-0003";
    public static final String MOVIE_ITEM_ERROR_KIND_INVALID = "ERROR-MOVIE-ITEM-ERROR-0004";
    public static final String MOVIE_ITEM_ERROR_INVALID_REQUEST = "ERROR-MOVIE-ITEM-ERROR-0005";
    public static final String MOVIE_ITEM_ERROR_LABEL_EXISTED = "ERROR-MOVIE-ITEM-ERROR-0006";
    public static final String MOVIE_ITEM_ERROR_INVALID_TOTAL_EPISODES = "ERROR-MOVIE-ITEM-ERROR-0007";

    /**
     * Starting error code Person
     */
    public static final String PERSON_ERROR_NOT_FOUND = "ERROR-PERSON-ERROR-0000";
    public static final String PERSON_ERROR_MOVIE_PERSON_EXISTED = "ERROR-PERSON-ERROR-0001";
    public static final String PERSON_ERROR_NOT_HAVE_KIND = "ERROR-PERSON-ERROR-0002";

    /**
     * Starting error code Movie Person
     */
    public static final String MOVIE_PERSON_ERROR_NOT_FOUND = "ERROR-MOVIE-PERSON-ERROR-0000";
    public static final String MOVIE_PERSON_ERROR_KIND_INVALID = "ERROR-MOVIE-PERSON-ERROR-0001";
    public static final String MOVIE_PERSON_ERROR_INVALID_REQUEST = "ERROR-MOVIE-PERSON-ERROR-0002";

    /**
     * Starting error code Sidebar
     */
    public static final String SIDEBAR_ERROR_NOT_FOUND = "ERROR-SIDEBAR-ERROR-0000";
    public static final String SIDEBAR_ERROR_MOVIE_EXISTED = "ERROR-SIDEBAR-ERROR-0001";

    /**
     * Starting error code Comment
     */
    public static final String COMMENT_ERROR_NOT_FOUND = "ERROR-COMMENT-ERROR-0000";
    public static final String COMMENT_ERROR_PARENT_INVALID = "ERROR-COMMENT-ERROR-0001";
    public static final String COMMENT_ERROR_REPLY_INVALID = "ERROR-COMMENT-ERROR-0002";
    public static final String COMMENT_ERROR_REPLY_NOT_FOUND = "ERROR-COMMENT-ERROR-0003";

    /**
     * Starting error code Review
     */
    public static final String REVIEW_ERROR_NOT_FOUND = "ERROR-REVIEW-ERROR-0000";
    public static final String REVIEW_ERROR_EXISTED = "ERROR-REVIEW-ERROR-0001";

    /**
     * Starting error code Favourite
     */
    public static final String FAVOURITE_ERROR_NOT_FOUND = "ERROR-FAVOURITE-ERROR-0000";

    /**
     * Sns error code
     */
    public static final String SNS_ERROR_APP_CONFIG = "ERROR-SNS-0000";
    public static final String SNS_ERROR_GET_CLIENT_TOKEN = "ERROR-SNS-0001";

    /**
     * Media error code
     */
    public static final String MEDIA_ERROR_DELETE_FILE = "ERROR-MEDIA-0000";

    /**
     * AppVersion error code
     */
    public static final String APP_VERSION_ERROR_NOT_FOUND = "ERROR-APP-VERSION-0000";
    public static final String APP_VERSION_ERROR_NAME_EXISTED = "ERROR-APP-VERSION-0001";
    public static final String APP_VERSION_ERROR_NOT_HAVE_LATEST_VERSION = "ERROR-APP-VERSION-0002";

    /**
     * Collection error code
     */
    public static final String COLLECTION_ERROR_NOT_FOUND = "ERROR-COLLECTION-0000";
    public static final String COLLECTION_ERROR_NAME_EXISTED = "ERROR-COLLECTION-0001";

    /**
     * Collection item error code
     */
    public static final String COLLECTION_ITEM_ERROR_NOT_FOUND = "ERROR-COLLECTION-ITEM-0000";
    public static final String COLLECTION_ITEM_ERROR_MOVIE_EXISTED = "ERROR-COLLECTION-ITEM-0001";
    public static final String COLLECTION_ITEM_ERROR_MAX_ITEM = "ERROR-COLLECTION-ITEM-0002";

    /**
     * Style error code
     */
    public static final String STYLE_ERROR_NOT_FOUND = "ERROR-STYLE-0000";
    public static final String STYLE_ERROR_TYPE_EXISTED = "ERROR-STYLE-0001";
    public static final String STYLE_ERROR_TYPE_NOT_HAVE_DEFAULT = "ERROR-STYLE-0002";

    /**
     * Playlist error code
     */
    public static final String PLAYLIST_ERROR_NOT_FOUND = "ERROR-PLAYLIST-0000";
    public static final String PLAYLIST_ERROR_MAX_PER_USER = "ERROR-PLAYLIST-0001";

    /**
     * ServerConfig error code
     */
    public static final String SERVER_CONFIG_ERROR_NOT_FOUND = "ERROR-SERVER-CONFIG-0000";
    public static final String SERVER_CONFIG_ERROR_SERVER_NUMBER_EXISTED = "ERROR-SERVER-CONFIG-0001";
    public static final String SERVER_CONFIG_ERROR_HOSTNAME_EXISTED = "ERROR-SERVER-CONFIG-0002";
    public static final String SERVER_CONFIG_ERROR_IP_PORT_EXISTED = "ERROR-SERVER-CONFIG-0003";
    public static final String SERVER_CONFIG_ERROR_UNAUTHORIZED = "ERROR-SERVER-CONFIG-0004";
    public static final String SERVER_CONFIG_ERROR_USED_BY_VIDEO_LIBRARY = "ERROR-SERVER-CONFIG-0005";
}
