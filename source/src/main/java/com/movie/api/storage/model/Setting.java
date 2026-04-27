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
@Table(name = DatabaseConstant.PREFIX_TABLE + "setting")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Setting extends Auditable<String> {
    @Column(name = "group_name")
    private String groupName;

    @Column(name = "key_name", unique = true)
    private String keyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "data_type")
    private String dataType; // Integer, String, Boolean, Double, RichText, Select, Upload

    @Column(name = "value_data", columnDefinition = "text")
    private String valueData;

    @Column(name = "options", columnDefinition = "LONGTEXT")
    private String options;

    @Column(name = "is_system")
    private Boolean isSystem;
}
