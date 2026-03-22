package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Comment;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentCriteria {

    private Long id;
    private Long movieItemId;
    private Long movieId;
    private Long parentId;
    private Long authorId;
    private Boolean isPinned;
    private Integer status;
    private Boolean isParent;

    public Specification<Comment> getSpecification() {
        return new Specification<Comment>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getMovieItemId() != null) {
                    predicates.add(cb.equal(root.get("movieItem").get("id"), getMovieItemId()));
                }

                if (getMovieId() != null) {
                    predicates.add(cb.equal(root.get("movieId"), getMovieId()));
                }

                if (getParentId() != null) {
                    predicates.add(cb.equal(root.get("parent").get("id"), getParentId()));
                }

                if (getAuthorId() != null) {
                    predicates.add(cb.equal(root.get("author").get("id"), getAuthorId()));
                }

                if (getIsPinned() != null) {
                    predicates.add(cb.equal(root.get("isPinned"), getIsPinned()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (Boolean.TRUE.equals(getIsParent())) {
                    predicates.add(cb.isNull(root.get("parent")));
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
