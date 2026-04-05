package com.movie.api.storage.criteria;

import com.movie.api.storage.model.VideoLibrary;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class VideoLibraryCriteria {

    private Long id;
    private String name;
    private Integer status;
    private Integer state;
    private Integer sourceType;
    private Long serverConfigId;
    private Long requiredId;

    public Specification<VideoLibrary> getSpecification() {
        return new Specification<VideoLibrary>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<VideoLibrary> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                if (getSourceType() != null) {
                    predicates.add(cb.equal(root.get("sourceType"), getSourceType()));
                }

                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }

                if (getServerConfigId() != null) {
                    predicates.add(cb.equal(root.get("serverConfig").get("id"), getServerConfigId()));
                }

                Predicate filters = cb.and(predicates.toArray(new Predicate[0]));

                if (getRequiredId() != null) {
                    Predicate required = cb.equal(root.get("id"), getRequiredId());
                    query.orderBy(
                            cb.desc(cb.selectCase()
                                    .when(required, 1)
                                    .otherwise(0)
                            )
                    );
                    return cb.or(filters, required);
                } else {
                    return filters;
                }
            }
        };
    }
}
