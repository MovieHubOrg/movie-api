package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Review;
import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewCriteria {

    private Long id;
    private Long movieId;
    private Long authorId;
    private Integer rate;
    private Integer status;
    private Boolean newest;
    private Boolean topLiked;
    private Boolean topDisliked;

    public Specification<Review> getSpecification() {
        return new Specification<Review>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Review> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getMovieId() != null) {
                    predicates.add(cb.equal(root.get("movieId"), getMovieId()));
                }

                if (getAuthorId() != null) {
                    predicates.add(cb.equal(root.get("author").get("id"), getAuthorId()));
                }

                if (getRate() != null) {
                    predicates.add(cb.equal(root.get("rate"), getRate()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }

    public Sort getSort() {
        if (Boolean.TRUE.equals(newest)) {
            return Sort.by(Sort.Order.desc("createdDate"));
        }
        if (Boolean.TRUE.equals(topLiked)) {
            return Sort.by(Sort.Order.desc("totalLike"), Sort.Order.desc("createdDate"));
        }
        if (Boolean.TRUE.equals(topDisliked)) {
            return Sort.by(Sort.Order.desc("totalDislike"), Sort.Order.desc("createdDate"));
        }
        // default
        return Sort.by(Sort.Order.desc("rate"), Sort.Order.desc("createdDate"));
    }
}
