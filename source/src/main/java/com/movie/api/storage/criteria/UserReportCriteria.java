package com.movie.api.storage.criteria;

import com.movie.api.storage.model.UserReport;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserReportCriteria {
    private Long id;
    private Long objectId;
    private Long authorId;
    private Integer type;
    private Integer status;

    public Specification<UserReport> getSpecification() {
        return new Specification<UserReport>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<UserReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getObjectId() != null) {
                    predicates.add(cb.equal(root.get("objectId"), getObjectId()));
                }

                if (getAuthorId() != null) {
                    predicates.add(cb.equal(root.get("author").get("id"), getAuthorId()));
                }

                if (getType() != null) {
                    predicates.add(cb.equal(root.get("type"), getType()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
