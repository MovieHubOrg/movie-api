package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "movie")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Movie extends Auditable<String> {
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    private String slug;

    @Column(columnDefinition = "longtext")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "image_title_url")
    private String imageTitleUrl;

    @Column(name = "release_date")
    private Date releaseDate;

    private Integer type; // 1: SINGLE, 2: SERIES

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    private String language; // eg. vi, en, ko, ...

    private String country;

    private Integer ageRating; // 1: G, 2: PG, 3: PG-13, 4: R, 5: NC-17, 6: 18+

    private Integer year;

    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    @JoinTable(
            name = DatabaseConstant.PREFIX_TABLE + "movie_category",
            joinColumns = @JoinColumn(name = "movie_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
    )
    private List<Category> categories = new ArrayList<>();

    // statistic
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @Column(name = "review_count")
    private Long reviewCount = 0L;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(columnDefinition = "LONGTEXT")
    private String metadata;
}
