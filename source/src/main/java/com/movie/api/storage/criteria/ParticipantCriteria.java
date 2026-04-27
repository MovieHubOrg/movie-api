package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Participant;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

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
    private Long userId;
    private Long hostId;
    private Integer role;
    private Integer state;
    private Integer status;

    public Specification<Participant> getSpecification() {
        return new Specification<Participant>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Participant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (getRoomId() != null) {
                    predicates.add(cb.equal(root.get("room").get("id"), getRoomId()));
                }

                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }

                if (getHostId() != null) {
                    predicates.add(cb.equal(root.get("room").get("host").get("id"), getHostId()));
                }

                if (getRole() != null) {
                    predicates.add(cb.equal(root.get("role"), getRole()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
