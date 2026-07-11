package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "review")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Review extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account author;

    private Long movieId;

    private Integer rate;

    @Column(columnDefinition = "longtext")
    private String content;

    private Integer totalLike = 0;

    private Integer totalDislike = 0;

    @Column(name = "toxic_spans", columnDefinition = "TEXT")
    private String toxicSpans;

    @Column(name = "detect_version")
    private Integer detectVersion = 0;
}
