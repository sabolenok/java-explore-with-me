package ru.practicum.explore_with_me.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stat, Integer> {
    List<Stat> findByTimestampBetweenOrderByAppAscUriAsc(LocalDateTime timestamp, LocalDateTime timestamp2);

    List<Stat> findByTimestampBetweenAndUriInOrderByAppAscUriAsc(LocalDateTime timestamp, LocalDateTime timestamp2,
                                                                 Collection<String> uri);
}
