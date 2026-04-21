package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Notification;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class NotificationCriteria {
    private Long accountId;
    private Integer type;
    private Boolean isRead;
    private Integer status;

    public Specification<Notification> getSpecification() {
        return new Specification<Notification>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Notification> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getAccountId() != null) {
                    predicates.add(cb.equal(root.get("account").get("id"), getAccountId()));
                }

                if (getType() != null) {
                    predicates.add(cb.equal(root.get("type"), getType()));
                }

                if (getIsRead() != null) {
                    predicates.add(cb.equal(root.get("isRead"), getIsRead()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
