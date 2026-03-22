package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "collection")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Collection extends Auditable<String> {
    private String name;

    private String color;

    private Integer ordering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id")
    private Style style;

    private Integer type;

    private String filter;

    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY)
    @OrderBy("ordering ASC")
    private List<CollectionItem> collectionItems = new ArrayList<>();
}
