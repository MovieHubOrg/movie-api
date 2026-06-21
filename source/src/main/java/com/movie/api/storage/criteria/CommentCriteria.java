package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Comment;
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
public class CommentCriteria {

    private Long id;
    private Long movieItemId;
    private Long movieId;
    private Long parentId;
    private Long authorId;
    private Boolean isPinned;
    private Integer status;
    private Boolean isParent;
    // sorting flags
    private Boolean newest;
    private Boolean topLiked;
    private Boolean topDisliked;

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

    /**
     * isPinned luôn ưu tiên đứng đầu (desc).
     * Sau đó tới tiêu chí phụ theo flag, mặc định là createdDate.
     * Direction của createdDate phụ thuộc parentId:
     *   - root comment (parentId == null) -> mới nhất trước
     *   - reply (parentId != null) -> cũ nhất trước (đúng thứ tự hội thoại)
     */
    public Sort getSort() {
        Sort.Direction dateDirection = (getParentId() == null) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort.Order secondaryOrder;
        if (Boolean.TRUE.equals(getTopLiked())) {
            secondaryOrder = Sort.Order.desc("totalLike");
        } else if (Boolean.TRUE.equals(getTopDisliked())) {
            secondaryOrder = Sort.Order.desc("totalDislike");
        } else {
            // mặc định, và cũng là trường hợp newest=true
            secondaryOrder = new Sort.Order(dateDirection, "createdDate");
        }

        // nếu sort theo totalLike/totalDislike, vẫn nên có createdDate làm tie-breaker
        if (!"createdDate".equals(secondaryOrder.getProperty())) {
            return Sort.by(Sort.Order.desc("isPinned"), secondaryOrder, new Sort.Order(dateDirection, "createdDate"));
        }

        return Sort.by(Sort.Order.desc("isPinned"), secondaryOrder);
    }
}
