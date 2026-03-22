package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Sidebar;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class SidebarCriteria {

    private Long id;
    private Boolean active;

    public Specification<Sidebar> getSpecification() {
        return new Specification<Sidebar>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Sidebar> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getActive() != null) {
                    predicates.add(cb.equal(root.get("active"), getActive()));
                }

                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
