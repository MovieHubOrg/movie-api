package com.movie.api.storage.criteria;

import com.movie.api.constant.BaseConstant;
import com.movie.api.storage.model.Room;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class RoomCriteria {
    private Long id;
    private Integer kind;
    private Long movieItemId;
    private Long hostId;
    private Integer state;
    private Boolean sortState;

    public Specification<Room> getSpecification() {
        return new Specification<Room>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Room> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getKind() != null) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
                }

                if (getMovieItemId() != null) {
                    predicates.add(cb.equal(root.get("movieItem").get("id"), getMovieItemId()));
                }

                if (getHostId() != null) {
                    predicates.add(cb.equal(root.get("host").get("id"), getHostId()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                if (Boolean.TRUE.equals(getSortState())) {
                    Expression<Integer> prioritySort = cb.<Integer>selectCase()
                            .when(cb.equal(root.get("state"), BaseConstant.ROOM_STATE_RUNNING), 1)
                            .when(cb.equal(root.get("state"), BaseConstant.ROOM_STATE_PENDING), 2)
                            .when(cb.equal(root.get("state"), BaseConstant.ROOM_STATE_ENDING), 3)
                            .otherwise(4);
                    query.orderBy(
                            cb.asc(prioritySort),
                            cb.desc(root.get("createdDate"))
                    );
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
