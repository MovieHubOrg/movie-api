package com.movie.api.storage.criteria;

import com.movie.api.storage.model.MoviePerson;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class MoviePersonCriteria {

    private Long id;
    private Long movieId;
    private Long personId;
    private Integer kind;
    private String keyword;

    public Specification<MoviePerson> getSpecification() {
        return new Specification<MoviePerson>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<MoviePerson> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getKind() != null) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
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
