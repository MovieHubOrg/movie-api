package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "user_movie",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_watch_history_user_movie",
                        columnNames = {"user_id", "movie_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_movie_user_id", columnList = "user_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class UserMovie extends Auditable<String> {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "movie_id")
    private Long movieId;
}
