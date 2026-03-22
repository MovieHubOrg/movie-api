package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Favourite;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class FavouriteCriteria {

    private Long id;
    private Long userId;
    private Integer type;
    private Long movieId;
    private Long personId;
    private Integer status;

    public Specification<Favourite> getSpecification() {
        return new Specification<Favourite>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Favourite> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }

                if (getType() != null) {
                    predicates.add(cb.equal(root.get("type"), getType()));
                }

                if (getMovieId() != null) {
                    predicates.add(cb.equal(root.get("movie").get("id"), getMovieId()));
                }

                if (getPersonId() != null) {
                    predicates.add(cb.equal(root.get("person").get("id"), getPersonId()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
