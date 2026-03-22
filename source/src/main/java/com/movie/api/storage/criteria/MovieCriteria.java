package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Category;
import com.movie.api.storage.model.CollectionItem;
import com.movie.api.storage.model.Movie;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MovieCriteria {
    private Long id;
    private String title;
    private String originalTitle;
    private Integer type;
    private Integer ageRating;
    private Integer status;
    private String language;
    private String country;
    private Boolean isFeatured;
    private List<Long> categoryIds;
    private Long collectionId;
    private Integer releaseYear;
    private String keyword;
    private List<Long> excludeIds;
    private Boolean comingSoon;

    public Specification<Movie> getSpecification() {
        return new Specification<Movie>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                query.distinct(true);
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getExcludeIds() != null && !getExcludeIds().isEmpty()) {
                    predicates.add(cb.not(root.get("id").in(getExcludeIds())));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (getTitle() != null) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + getTitle().toLowerCase() + "%"));
                }

                if (getOriginalTitle() != null) {
                    predicates.add(cb.like(cb.lower(root.get("originalTitle")), "%" + getOriginalTitle().toLowerCase() + "%"));
                }

                if (getType() != null) {
                    predicates.add(cb.equal(root.get("type"), getType()));
                }

                if (getAgeRating() != null) {
                    predicates.add(cb.equal(root.get("ageRating"), getAgeRating()));
                }

                if (getLanguage() != null) {
                    predicates.add(cb.equal(root.get("language"), getLanguage()));
                }

                if (getCountry() != null) {
                    predicates.add(cb.equal(root.get("country"), getCountry()));
                }

                if (getIsFeatured() != null) {
                    predicates.add(cb.equal(root.get("isFeatured"), getIsFeatured()));
                }

                if (getCategoryIds() != null && !getCategoryIds().isEmpty()) {
                    Join<Movie, Category> categoryJoin = root.join("categories", JoinType.INNER);
                    predicates.add(categoryJoin.get("id").in(getCategoryIds()));
                }

                if (getCollectionId() != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<CollectionItem> collectionItemRoot = subquery.from(CollectionItem.class);
                    subquery.select(collectionItemRoot.get("movie").get("id"))
                            .where(cb.equal(collectionItemRoot.get("collection").get("id"), getCollectionId()));
                    predicates.add(cb.not(root.get("id").in(subquery)));
                }

                if (getReleaseYear() != null) {
                    Expression<Integer> yearExpr = cb.function("year", Integer.class, root.get("releaseDate"));
                    predicates.add(cb.equal(yearExpr, getReleaseYear()));
                }

                if (StringUtils.isNoneBlank(getKeyword())) {
                    String kw = "%" + getKeyword().toLowerCase() + "%";
                    Predicate titleLike = cb.like(cb.lower(root.get("title")), kw);
                    Predicate originalTitleLike = cb.like(cb.lower(root.get("originalTitle")), kw);
                    predicates.add(cb.or(titleLike, originalTitleLike));
                }

                if (Boolean.TRUE.equals(getComingSoon())) {
                    predicates.add(cb.greaterThan(root.get("releaseDate"), new Date()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
