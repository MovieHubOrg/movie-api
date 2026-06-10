package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "user_movie",
        indexes = {
                @Index(name = "idx_user_movie_user_id", columnList = "user_id"),
                @Index(name = "idx_user_movie_movie_id", columnList = "movie_id"),
                @Index(name = "idx_user_movie_type", columnList = "type"),
                @Index(name = "idx_user_movie_user_movie", columnList = "user_id, movie_id"),
                @Index(name = "idx_user_movie_user_type", columnList = "user_id, type"),
                @Index(name = "idx_user_movie_source", columnList = "source")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class UserMovie extends Auditable<String> {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "movie_item_id")
    private Long movieItemId;

    @Column(name = "type")
    private Integer type;

    @Column(name = "value")
    private Double value;

    @Column(name = "source", length = 50)
    private String source;
}
