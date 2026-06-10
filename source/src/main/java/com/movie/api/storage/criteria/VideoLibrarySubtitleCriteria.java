package com.movie.api.storage.criteria;

import com.movie.api.storage.model.VideoLibrarySubtitle;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class VideoLibrarySubtitleCriteria {
    private Long videoLibraryId;
    private Integer state;
    private String label;
    private String language;

    public Specification<VideoLibrarySubtitle> getSpecification() {
        return new Specification<VideoLibrarySubtitle>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<VideoLibrarySubtitle> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (getVideoLibraryId() != null) {
                    predicates.add(cb.equal(root.get("videoLibrary").get("id"), getVideoLibraryId()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                if (getLabel() != null) {
                    predicates.add(cb.like(cb.lower(root.get("label")), "%" + getLabel().toLowerCase() + "%"));
                }

                if (getLanguage() != null) {
                    predicates.add(cb.like(cb.lower(root.get("language")), "%" + getLanguage().toLowerCase() + "%"));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
