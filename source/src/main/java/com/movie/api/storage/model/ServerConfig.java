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
@Table(name = DatabaseConstant.PREFIX_TABLE + "server_config")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ServerConfig extends Auditable<String> {
    @Column(name = "server_number", unique = true)
    private Integer serverNumber;

    private String name;

    private String hostname;

    private String ip;

    private Integer port;
}
