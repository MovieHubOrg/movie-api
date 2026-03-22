package com.movie.api.storage.criteria;

import com.movie.api.storage.model.MovieItem;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MovieItemCriteria {

    private Long id;
    private String title;
    private Integer kind;
    private Integer excludeKind;
    private Integer status;
    private Long movieId;
    private Long parentId;
    private Date fromDate;
    private Date toDate;

    public Specification<MovieItem> getSpecification() {
        return new Specification<MovieItem>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<MovieItem> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (getTitle() != null) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + getTitle().toLowerCase() + "%"));
                }

                if (getKind() != null) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
                }

                if (getExcludeKind() != null) {
                    predicates.add(cb.notEqual(root.get("kind"), getExcludeKind()));
                }

                if (getMovieId() != null) {
                    predicates.add(cb.equal(root.get("movie").get("id"), getMovieId()));
                }

                if (getParentId() != null) {
                    predicates.add(cb.equal(root.get("parent").get("id"), getParentId()));
                }

                if (getFromDate() != null && getToDate() != null) {
                    predicates.add(cb.between(root.get("releaseDate"), getFromDate(), getToDate()));
                } else if (getFromDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("releaseDate"), getFromDate()));
                } else if (getToDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("releaseDate"), getToDate()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
