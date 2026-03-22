package com.movie.api.storage.criteria;

import com.movie.api.storage.model.AppVersion;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class AppVersionCriteria {
    private Long id;
    private Integer code;
    private String name;
    private Boolean forceUpdate;
    private Boolean isLatest;

    public Specification<AppVersion> getSpecification() {
        return new Specification<AppVersion>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<AppVersion> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getCode() != null) {
                    predicates.add(cb.equal(root.get("code"), getCode()));
                }

                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }

                if (getForceUpdate() != null) {
                    predicates.add(cb.equal(root.get("forceUpdate"), getForceUpdate()));
                }

                if (getIsLatest() != null) {
                    predicates.add(cb.equal(root.get("isLatest"), getIsLatest()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
