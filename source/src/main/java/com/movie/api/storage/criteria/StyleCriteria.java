package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Style;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class StyleCriteria {
    private Long id;
    private String name;
    private Boolean isDefault;

    public Specification<Style> getSpecification() {
        return new Specification<Style>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Style> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }

                if (getIsDefault() != null) {
                    predicates.add(cb.equal(root.get("isDefault"), getIsDefault()));
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
