package br.ufpb.caixacerto.movement;

import br.ufpb.caixacerto.common.enums.MovementNature;
import br.ufpb.caixacerto.common.enums.MovementType;
import br.ufpb.caixacerto.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    // Basic queries
    List<Movement> findByUserOrderByDateDescIdDesc(User user);

    Page<Movement> findByUserOrderByDateDescIdDesc(User user, Pageable pageable);

    // Filtered queries
    List<Movement> findByUserAndTypeOrderByDateDescIdDesc(User user, MovementType type);

    List<Movement> findByUserAndNatureAndTypeOrderByDateDescIdDesc(
            User user, MovementNature nature, MovementType type);

    // Date range queries
    @Query("SELECT m FROM Movement m WHERE m.user = :user AND m.date BETWEEN :start AND :end ORDER BY m.date DESC, m.id DESC")
    List<Movement> findByUserAndDateBetween(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT m FROM Movement m WHERE m.user = :user AND m.date BETWEEN :start AND :end ORDER BY m.date DESC, m.id DESC")
    Page<Movement> findByUserAndDateBetween(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    // Month queries
    @Query("SELECT m FROM Movement m WHERE m.user = :user AND m.date >= :start AND m.date <= :end ORDER BY m.date DESC, m.id DESC")
    List<Movement> findByUserAndMonth(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    // Aggregations for summary
    @Query("SELECT SUM(m.amountCents) FROM Movement m WHERE m.user = :user AND m.nature = :nature AND m.type = :type AND m.date BETWEEN :start AND :end")
    Long sumAmountByUserNatureTypeAndDateBetween(
            @Param("user") User user,
            @Param("nature") MovementNature nature,
            @Param("type") MovementType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    // Count for pagination
    long countByUser(User user);

    // Delete all user movements (used when deleting user)
    void deleteByUser(User user);
}