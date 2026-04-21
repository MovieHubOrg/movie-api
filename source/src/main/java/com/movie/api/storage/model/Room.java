package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "room")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Room extends Auditable<String> {
    private String code;

    private Integer kind; // 0: private, 1: public

    @ManyToOne
    @JoinColumn(name = "movie_item_id")
    private MovieItem movieItem;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private Account host;

    private Date startTime;

    private Date endTime;

    private Integer state = 1; // 0: pending, 1: running, 2: ending

    private Integer participantCount = 0;
}
