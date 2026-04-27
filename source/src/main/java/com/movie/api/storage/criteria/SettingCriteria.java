package com.movie.api.storage.criteria;

import com.movie.api.storage.model.Setting;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SettingCriteria {
    private Long id;
    private String groupName;
    private String keyName;
    private String valueData;
    private String dataType;
    private Boolean isSystem;
    private Integer status;
    private String requiredId;

    public Specification<Setting> getCriteria() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (getId() != null) {
                predicates.add(cb.equal(root.get("id"), getId()));
            }
            if (StringUtils.isNotBlank(getGroupName())) {
                predicates.add(cb.like(cb.lower(root.get("groupName")), "%" + getGroupName().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(getKeyName())) {
                predicates.add(cb.equal(root.get("keyName"), getKeyName()));
            }
            if (StringUtils.isNotBlank(getValueData())) {
                predicates.add(cb.like(cb.lower(root.get("valueData")), "%" + getValueData().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(getDataType())) {
                predicates.add(cb.like(cb.lower(root.get("dataType")), "%" + getDataType().toLowerCase() + "%"));
            }
            if (getIsSystem() != null) {
                predicates.add(cb.equal(root.get("isSystem"), getIsSystem()));
            }
            if (getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), getStatus()));
            }

            Predicate filters = cb.and(predicates.toArray(new Predicate[0]));

            if (getRequiredId() != null) {
                Predicate required = cb.equal(root.get("id"), getRequiredId());
                query.orderBy(
                        cb.desc(cb.selectCase()
                                .when(required, 1)
                                .otherwise(0)
                        )
                );
                return cb.or(filters, required);
            } else {
                return filters;
            }
        };
    }
}
