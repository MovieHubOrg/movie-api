package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "style")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Style extends Auditable<String> {
    private Integer type;

    private String name;

    @Column(columnDefinition = "longtext")
    private String description;

    private String imageMobileUrl;

    private String imageWebUrl;

    private Boolean isDefault = false;
}
