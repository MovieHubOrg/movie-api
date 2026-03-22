package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Collection;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class CollectionCriteria {
    private String name;
    private Long styleId;
    private Integer type;
    private Boolean randomData;
    private Integer status;

    public Specification<Collection> getSpecification() {
        return new Specification<Collection>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Collection> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }

                if (getStyleId() != null) {
                    predicates.add(cb.equal(root.get("style").get("id"), getStyleId()));
                }

                if (getType() != null) {
                    predicates.add(cb.equal(root.get("type"), getType()));
                }

                if (getRandomData() != null) {
                    predicates.add(cb.equal(root.get("randomData"), getRandomData()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
