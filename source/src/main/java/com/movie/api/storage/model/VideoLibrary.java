package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "video_library")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class VideoLibrary extends Auditable<String> {
    private String name;

    @Column(columnDefinition = "longtext")
    private String description;

    @Column(name = "source_type")
    private Integer sourceType; // 1: INTERNAL, 2: EXTERNAL

    @Column(name = "content")
    private String content;

    @Column(name = "relative_content_path")
    private String relativeContentPath;

    @Column(name = "sprite_url")
    private String spriteUrl;

    @Column(name = "vtt_url")
    private String vttUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private Long duration;

    @Column(name = "intro_start")
    private Long introStart;

    @Column(name = "intro_end")
    private Long introEnd;

    @Column(name = "outro_start")
    private Long outroStart;

    private Integer state; // 0: PROCESSING, 1: READY, 2: ERROR

    @Column(name = "reason")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_config_id")
    private ServerConfig serverConfig;
}
