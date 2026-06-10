package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "movie_similarity",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_movie_similarity_movie_similar",
                        columnNames = {"movie_id", "similar_movie_id"}
                )
        },
        indexes = {
                @Index(name = "idx_movie_similarity_movie_id", columnList = "movie_id"),
                @Index(name = "idx_movie_similarity_similar_movie_id", columnList = "similar_movie_id"),
                @Index(name = "idx_movie_similarity_score", columnList = "score")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class MovieSimilarity extends Auditable<String> {
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "similar_movie_id", nullable = false)
    private Long similarMovieId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "reason", length = 50)
    private String reason;

    @Column(name = "model_version", length = 50)
    private String modelVersion;
}
