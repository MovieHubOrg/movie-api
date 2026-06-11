package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "comment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Comment extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_item_id")
    private MovieItem movieItem;

    private Long movieId;

    @Column(columnDefinition = "longtext")
    private String content;

    private Integer totalLike = 0;

    private Integer totalDislike = 0;

    private Integer totalChildren = 0;

    private Boolean isPinned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account author;

    @Column(columnDefinition = "TEXT")
    private String authorInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Account replyTo;

    @Column(columnDefinition = "TEXT")
    private String replyToInfo;

    @Column(name = "toxic_spans", columnDefinition = "TEXT")
    private String toxicSpans;
}
