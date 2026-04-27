package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "chat")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Chat extends Auditable<String> {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account user;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(columnDefinition = "TEXT")
    private String content;
}
