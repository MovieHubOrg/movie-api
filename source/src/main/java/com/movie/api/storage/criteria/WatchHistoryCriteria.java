package com.movie.api.storage.criteria;

import com.movie.api.storage.model.WatchHistory;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class WatchHistoryCriteria {
    private Long userId;
    private Long movieId;
    private Boolean isCompleted;
    private Integer status;

    public Specification<WatchHistory> getSpecification() {
        return new Specification<WatchHistory>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<WatchHistory> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }

                if (getMovieId() != null) {
                    predicates.add(cb.equal(root.get("movie").get("id"), getMovieId()));
                }

                if (getIsCompleted() != null) {
                    predicates.add(cb.equal(root.get("isCompleted"), getIsCompleted()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
