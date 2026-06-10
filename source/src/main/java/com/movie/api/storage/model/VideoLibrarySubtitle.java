package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "video_library_subtitle")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class VideoLibrarySubtitle extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_library_id", nullable = false)
    private VideoLibrary videoLibrary;

    @Column(nullable = false)
    private String language;

    private String label;

    @Column(name = "file_url")
    private String fileUrl;

    private Integer state; // 0: PROCESSING, 1: READY, 2: ERROR

    @Column(name = "is_default")
    private Boolean isDefault = false;
}
