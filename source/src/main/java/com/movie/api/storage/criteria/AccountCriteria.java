package com.movie.api.storage.criteria;

import com.movie.api.constant.BaseConstant;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.model.Participant;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class AccountCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Integer kind;
    private String username;
    private Integer status;
    private String email;
    private String fullName;
    private String phone;
    private String keyword;
    private Long ignoreRoomId;

    public Specification<Account> getSpecification() {
        return new Specification<Account>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                query.distinct(true);

                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getKind() != null) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (StringUtils.hasText(getKeyword())) {
                    String keyword = "%" + getKeyword().trim().toLowerCase() + "%";
                    Predicate usernameLike = cb.like(cb.lower(root.get("username")), keyword);
                    Predicate emailLike = cb.like(cb.lower(root.get("email")), keyword);
                    Predicate fullNameLike = cb.like(cb.lower(root.get("fullName")), keyword);
                    predicates.add(cb.or(usernameLike, emailLike, fullNameLike));
                }
                if (getIgnoreRoomId() != null) {
                    Subquery<Long> participantSubquery = query.subquery(Long.class);
                    Root<Participant> participantRoot = participantSubquery.from(Participant.class);
                    participantSubquery.select(participantRoot.get("user").get("id"))
                            .where(
                                    cb.equal(participantRoot.get("room").get("id"), getIgnoreRoomId()),
                                    cb.equal(participantRoot.get("status"), BaseConstant.STATUS_ACTIVE)
                            );
                    predicates.add(cb.not(root.get("id").in(participantSubquery)));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
