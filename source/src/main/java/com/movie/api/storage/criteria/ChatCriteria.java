package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Chat;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatCriteria {
    private Long id;
    private Long roomId;
    private Long userId;
    private Integer status;

    public Specification<Chat> getSpecification() {
        return new Specification<Chat>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Chat> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getRoomId() != null) {
                    predicates.add(cb.equal(root.get("room").get("id"), getRoomId()));
                }

                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
