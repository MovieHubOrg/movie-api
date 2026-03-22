package com.movie.api.storage.criteria;

import com.movie.api.storage.model.CollectionItem;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class CollectionItemCriteria {
    private Long collectionId;

    public Specification<CollectionItem> getSpecification() {
        return new Specification<CollectionItem>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<CollectionItem> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getCollectionId() != null) {
                    predicates.add(cb.equal(root.get("collection").get("id"), getCollectionId()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
