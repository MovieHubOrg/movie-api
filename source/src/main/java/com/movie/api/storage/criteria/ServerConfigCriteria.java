package com.movie.api.storage.criteria;

import com.movie.api.storage.model.ServerConfig;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class ServerConfigCriteria {
    private Long id;
    private Integer serverNumber;
    private String name;
    private String hostname;
    private Integer status;

    public Specification<ServerConfig> getSpecification() {
        return new Specification<ServerConfig>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<ServerConfig> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getServerNumber() != null) {
                    predicates.add(cb.equal(root.get("serverNumber"), getServerNumber()));
                }

                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }

                if (getHostname() != null) {
                    predicates.add(cb.like(cb.lower(root.get("hostname")), "%" + getHostname().toLowerCase() + "%"));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
