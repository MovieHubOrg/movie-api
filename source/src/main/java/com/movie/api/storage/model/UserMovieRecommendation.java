package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "user_movie_recommendation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_movie_recommendation_user_movie_model",
                        columnNames = {"user_id", "movie_id", "model_version"}
                )
        },
        indexes = {
                @Index(name = "idx_user_movie_recommendation_user_model_status", columnList = "user_id, model_version, status"),
                @Index(name = "idx_user_movie_recommendation_movie_id", columnList = "movie_id"),
                @Index(name = "idx_user_movie_recommendation_score", columnList = "score")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class UserMovieRecommendation extends Auditable<String> {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "model_version", length = 50, nullable = false)
    private String modelVersion;
}
