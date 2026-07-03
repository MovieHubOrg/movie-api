package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Participant;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class ParticipantCriteria {
    private Long id;
    private Long roomId;
    private Integer role;
    private Integer state;
    private String keyword;

    public Specification<Participant> getSpecification() {
        return new Specification<Participant>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Participant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getRoomId() != null) {
                    predicates.add(cb.equal(root.get("room").get("id"), getRoomId()));
                }

                if (getRole() != null) {
                    predicates.add(cb.equal(root.get("role"), getRole()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                if (StringUtils.hasText(getKeyword())) {
                    String keyword = "%" + getKeyword().trim().toLowerCase() + "%";
                    Predicate usernameLike = cb.like(cb.lower(root.get("user").get("username")), keyword);
                    Predicate emailLike = cb.like(cb.lower(root.get("user").get("email")), keyword);
                    Predicate fullNameLike = cb.like(cb.lower(root.get("user").get("fullName")), keyword);
                    predicates.add(cb.or(usernameLike, emailLike, fullNameLike));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
