package com.movie.api.storage.repository;

import com.movie.api.storage.model.Movie;
import com.movie.api.dto.statistic.StatisticTopMovieDto;
import com.movie.api.dto.statistic.StatisticGroupDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    Optional<Movie> findByIdAndStatus(Long id, Integer status);

    List<Movie> findAllByIdInAndStatus(List<Long> ids, Integer status);

    boolean existsByCategories_Id(Long categoryId);

    /**
     * Find suggestion movie by scoring algorithm
     * Score calculation:
     * - Same category: +10 points per match
     * - Same country: +5 points
     * - Same language: +3 points
     * - Same type: +2 points
     * - High view count: normalized bonus
     * - Recent release: time decay bonus
     */
    @Query(value = "SELECT DISTINCT m.*, " +
            "       ( " +
            "           (SELECT COUNT(*) * 10 " +
            "            FROM db_movie_category mc1 " +
            "            WHERE mc1.movie_id = m.id " +
            "            AND mc1.category_id IN ( " +
            "                SELECT mc2.category_id " +
            "                FROM db_movie_category mc2 " +
            "                WHERE mc2.movie_id = :movieId " +
            "            ) " +
            "           ) + " +
            "           (CASE WHEN m.country = :country THEN 5 ELSE 0 END) + " +
            "           (CASE WHEN m.language = :language THEN 3 ELSE 0 END) + " +
            "           (CASE WHEN m.type = :type THEN 2 ELSE 0 END) + " +
            "           (LEAST(m.view_count / 1000.0, 5)) + " +
            "           (CASE " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN 3 " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 1 YEAR) THEN 2 " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 2 YEAR) THEN 1 " +
            "               ELSE 0 " +
            "           END) " +
            "       ) AS relevance_score " +
            "FROM db_movie m " +
            "WHERE m.id != :movieId " +
            "AND m.status = 1 " +
            "AND EXISTS ( " +
            "    SELECT 1 FROM db_movie_category mc " +
            "    WHERE mc.movie_id = m.id " +
            "    AND mc.category_id IN :categoryIds " +
            ") " +
            "ORDER BY relevance_score DESC, m.view_count DESC, m.created_date DESC " +
            "LIMIT :#{#pageable.pageSize}", nativeQuery = true
    )
    List<Movie> findSuggestion(
            @Param("movieId") Long movieId,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("country") String country,
            @Param("language") String language,
            @Param("type") Integer type,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT m.*, " +
            "       ( " +
            "           (SELECT COUNT(DISTINCT mc.category_id) * 10 " +
            "            FROM db_movie_category mc " +
            "            WHERE mc.movie_id = m.id " +
            "            AND mc.category_id IN ( " +
            "                SELECT DISTINCT source_mc.category_id " +
            "                FROM db_movie_category source_mc " +
            "                WHERE source_mc.movie_id IN (:sourceMovieIds) " +
            "            ) " +
            "           ) + " +
            "           (CASE WHEN m.country IN (:countries) THEN 5 ELSE 0 END) + " +
            "           (LEAST(m.view_count / 1000.0, 5)) + " +
            "           (CASE " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN 3 " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 1 YEAR) THEN 2 " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 2 YEAR) THEN 1 " +
            "               ELSE 0 " +
            "           END) " +
            "       ) AS relevance_score " +
            "FROM db_movie m " +
            "WHERE m.status = 1 " +
            "AND m.id NOT IN (:excludedMovieIds) " +
            "AND ( " +
            "    EXISTS ( " +
            "        SELECT 1 FROM db_movie_category mc " +
            "        WHERE mc.movie_id = m.id " +
            "        AND mc.category_id IN ( " +
            "            SELECT DISTINCT source_mc.category_id " +
            "            FROM db_movie_category source_mc " +
            "            WHERE source_mc.movie_id IN (:sourceMovieIds) " +
            "        ) " +
            "    ) " +
            "    OR m.country IN (:countries) " +
            ") " +
            "ORDER BY relevance_score DESC, m.view_count DESC, m.created_date DESC " +
            "LIMIT :#{#pageable.pageSize}", nativeQuery = true)
    List<Movie> findRecentWatchedRecommendation(
            @Param("sourceMovieIds") List<Long> sourceMovieIds,
            @Param("countries") List<String> countries,
            @Param("excludedMovieIds") List<Long> excludedMovieIds,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT m.*, " +
            "       ( " +
            "           (CASE " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN 3 " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 1 YEAR) THEN 2 " +
            "               WHEN m.release_date >= DATE_SUB(NOW(), INTERVAL 2 YEAR) THEN 1 " +
            "               ELSE 0 " +
            "           END) " +
            "       ) AS relevance_score " +
            "FROM db_movie m " +
            "JOIN db_movie_category mc ON mc.movie_id = m.id " +
            "WHERE m.status = 1 " +
            "AND mc.category_id = :categoryId " +
            "AND m.id NOT IN (:excludedMovieIds) " +
            "ORDER BY relevance_score DESC, m.view_count DESC, m.created_date DESC " +
            "LIMIT :#{#pageable.pageSize}", nativeQuery = true)
    List<Movie> findRecommendationByCategory(
            @Param("categoryId") Long categoryId,
            @Param("excludedMovieIds") List<Long> excludedMovieIds,
            Pageable pageable);

    @Transactional
    @Modifying
    @Query("update Movie m set m.viewCount = (" +
            "   select coalesce(sum(wh.timesWatched), 0) " +
            "   from WatchHistory wh " +
            "   where wh.movie.id = m.id and wh.movieItem is null" +
            ")")
    void updateViewCount();

    @Query("select count(m) from Movie m " +
            "where m.status = :status " +
            "and (:type is null or m.type = :type) " +
            "and (:fromDate is null or m.createdDate >= :fromDate) " +
            "and (:toDate is null or m.createdDate <= :toDate)")
    Long countStatisticMovies(@Param("status") Integer status,
                              @Param("type") Integer type,
                              @Param("fromDate") Date fromDate,
                              @Param("toDate") Date toDate);

    @Query("select coalesce(sum(m.viewCount), 0) from Movie m where m.status = :status")
    Long sumViewCountByStatus(@Param("status") Integer status);

    @Query("select coalesce(avg(m.averageRating), 0) from Movie m " +
            "where m.status = :status " +
            "and (:fromDate is null or m.createdDate >= :fromDate) " +
            "and (:toDate is null or m.createdDate <= :toDate)")
    Double averageRatingByStatusAndCreatedDateBetween(@Param("status") Integer status,
                                                      @Param("fromDate") Date fromDate,
                                                      @Param("toDate") Date toDate);

    @Query("select new com.movie.api.dto.statistic.StatisticTopMovieDto(" +
            "m.id, m.title, m.thumbnailUrl, m.viewCount, m.commentCount, m.reviewCount, m.averageRating) " +
            "from Movie m " +
            "where m.status = :status " +
            "and (:fromDate is null or m.createdDate >= :fromDate) " +
            "and (:toDate is null or m.createdDate <= :toDate)")
    Page<StatisticTopMovieDto> findTopMovieStatistics(@Param("status") Integer status,
                                                      @Param("fromDate") Date fromDate,
                                                      @Param("toDate") Date toDate,
                                                      Pageable pageable);

    @Query("select new com.movie.api.dto.statistic.StatisticGroupDto(m.type, count(m)) " +
            "from Movie m where m.status = :status group by m.type order by count(m) desc")
    List<StatisticGroupDto> groupByType(@Param("status") Integer status);

    @Query("select new com.movie.api.dto.statistic.StatisticGroupDto(coalesce(m.country, 'Unknown'), count(m)) " +
            "from Movie m where m.status = :status group by m.country order by count(m) desc")
    List<StatisticGroupDto> groupByCountry(@Param("status") Integer status);

    @Query("select new com.movie.api.dto.statistic.StatisticGroupDto(coalesce(m.language, 'Unknown'), count(m)) " +
            "from Movie m where m.status = :status group by m.language order by count(m) desc")
    List<StatisticGroupDto> groupByLanguage(@Param("status") Integer status);

    @Query("select new com.movie.api.dto.statistic.StatisticGroupDto(m.ageRating, count(m)) " +
            "from Movie m where m.status = :status group by m.ageRating order by count(m) desc")
    List<StatisticGroupDto> groupByAgeRating(@Param("status") Integer status);
}
