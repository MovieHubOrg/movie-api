package com.movie.api.storage.criteria;

import com.movie.api.storage.model.MoviePerson;
import com.movie.api.storage.model.Person;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class PersonCriteria {

    private Long id;
    private String name;
    private String otherName;
    private Integer gender;
    private Integer kind;
    private Integer status;
    private String country;
    private Long movieId;

    public Specification<Person> getSpecification() {
        return new Specification<Person>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }

                if (getOtherName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("otherName")), "%" + getOtherName().toLowerCase() + "%"));
                }

                if (getGender() != null) {
                    predicates.add(cb.equal(root.get("gender"), getGender()));
                }

                if (getKind() != null) {
                    Join<Person, Integer> kindJoin = root.join("kinds");
                    predicates.add(cb.equal(kindJoin, kind));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (getCountry() != null) {
                    predicates.add(cb.like(cb.lower(root.get("country")), "%" + getCountry().toLowerCase() + "%"));
                }

                if (getMovieId() != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<MoviePerson> mpRoot = subquery.from(MoviePerson.class);
                    subquery.select(mpRoot.get("person").get("id"))
                            .where(cb.equal(mpRoot.get("movie").get("id"), getMovieId()));
                    predicates.add(root.get("id").in(subquery));
                }

                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
