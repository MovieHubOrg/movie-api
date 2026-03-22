package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "person")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Person extends Auditable<String> {
    private String name;

    @Column(name = "other_name")
    private String otherName;

    @Column(name = "avatar_path")
    private String avatarPath;

    @Column(columnDefinition = "longtext")
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = DatabaseConstant.PREFIX_TABLE + "person_kind", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "kind")
    private List<Integer> kinds = new ArrayList<>();

    private Integer gender;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    private String country;
}
