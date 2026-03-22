package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "side_bar")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Sidebar extends Auditable<String> {
    @Column(columnDefinition = "longtext")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "web_thumbnail_url")
    private String webThumbnailUrl;

    @Column(name = "mobile_thumbnail_url")
    private String mobileThumbnailUrl;

    @Column(name = "main_color")
    private String mainColor;

    private Integer ordering;

    private Boolean active = false;
}
