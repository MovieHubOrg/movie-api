package com.movie.api.storage.repository;

import com.movie.api.storage.model.ServerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ServerConfigRepository extends JpaRepository<ServerConfig, Long>, JpaSpecificationExecutor<ServerConfig> {
    Optional<ServerConfig> findByIdAndStatus(Long id, Integer status);

    boolean existsByServerNumber(Integer serverNumber);

    boolean existsByHostname(String hostname);

    boolean existsByIpAndPort(String ip, Integer port);

    boolean existsByServerNumberAndIdNot(Integer serverNumber, Long id);

    boolean existsByHostnameAndIdNot(String hostname, Long id);

    boolean existsByIpAndPortAndIdNot(String ip, Integer port, Long id);
}
