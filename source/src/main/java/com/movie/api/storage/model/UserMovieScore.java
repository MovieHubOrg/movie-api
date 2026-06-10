package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "user_movie_score",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_movie_score_user_movie",
                        columnNames = {"user_id", "movie_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_movie_score_user_id", columnList = "user_id"),
                @Index(name = "idx_user_movie_score_movie_id", columnList = "movie_id"),
                @Index(name = "idx_user_movie_score_score", columnList = "score")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class UserMovieScore extends Auditable<String> {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "last_event_date")
    private Long lastEventDate;

    @Column(name = "model_version", length = 50)
    private String modelVersion;
}
